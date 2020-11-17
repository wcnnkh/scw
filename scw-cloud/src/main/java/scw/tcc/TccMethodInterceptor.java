package scw.tcc;

import scw.aop.MethodInterceptor;
import scw.aop.MethodInterceptorAccept;
import scw.aop.MethodInterceptorChain;
import scw.aop.MethodInvoker;
import scw.complete.Complete;
import scw.core.instance.NoArgsInstanceFactory;
import scw.core.instance.annotation.Configuration;
import scw.lang.NotSupportedException;
import scw.tcc.annotation.Tcc;
import scw.transaction.DefaultTransactionLifeCycle;
import scw.transaction.TransactionManager;

@Configuration(order = Integer.MAX_VALUE)
public class TccMethodInterceptor implements MethodInterceptor, MethodInterceptorAccept{
	private NoArgsInstanceFactory instanceFactory;

	public TccMethodInterceptor(NoArgsInstanceFactory instanceFactory) {
		this.instanceFactory = instanceFactory;
	}
	
	public boolean isAccept(MethodInvoker invoker, Object[] args) {
		return invoker.getMethod().getAnnotation(Tcc.class) != null;
	}
	
	public Object intercept(MethodInvoker invoker, Object[] args, MethodInterceptorChain filterChain) throws Throwable {
		final Tcc tcc = invoker.getMethod().getAnnotation(Tcc.class);
		if (tcc == null) {
			return invoker.invoke(args);
		}

		if (!instanceFactory.isInstance(tcc.service())) {
			throw new NotSupportedException("not support tcc: " + invoker.getMethod().toString());
		}

		if (!TransactionManager.hasTransaction()) {
			throw new NotSupportedException("not exist transaction");
		}

		Object result = invoker.invoke(args);
		final TccService tccService = instanceFactory.getInstance(tcc.service());
		final Stage confirm = tccService.createConfirm(invoker, args, result, tcc);
		final Stage cancel = tccService.createCancel(invoker, args, result, tcc);
		if (confirm == null && cancel == null) {
			throw new TccException("confirm or cancel At least one: " + invoker.getMethod().toString());
		}

		if ((confirm != null && !confirm.isActive()) || (cancel != null && !cancel.isActive())) {
			throw new TccException("tcc definition error:" + invoker.getMethod());
		}

		// 先注册一个取消任务，以防止最坏的情况发生，那样还可以回滚,但是如果存在confirm的情况下还会执行confirm，所以应该在业务中判断如果已经cancel了那么confirm无效
		final Complete cancelComplete = cancel == null ? null : tccService.registerComplete(cancel);
		final Complete confirmComplete = confirm == null ? null : tccService.registerComplete(confirm);
		TransactionManager.transactionLifeCycle(new DefaultTransactionLifeCycle(){
			@Override
			public void afterRollback() {
				if(confirmComplete != null){
					confirmComplete.cancel();
				}
				
				if (cancelComplete != null) {
					cancelComplete.run();
				}
			}
			
			@Override
			public void afterCommit() {
				if (cancelComplete != null) {
					cancelComplete.cancel();
				}
				
				if(confirmComplete != null){
					confirmComplete.run();
				}
			}
		});
		return result;
	}

}
