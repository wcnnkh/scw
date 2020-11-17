package scw.tcc;

import java.lang.reflect.Method;

import scw.aop.MethodInvoker;
import scw.beans.BeanFactory;
import scw.beans.BeanUtils;
import scw.beans.RuntimeBean;
import scw.complete.Complete;
import scw.complete.CompleteService;
import scw.core.instance.annotation.Configuration;
import scw.core.utils.StringUtils;
import scw.tcc.annotation.Tcc;

@Configuration(order = Integer.MIN_VALUE, value = TccService.class)
public class DefaultTccService implements TccService {
	private BeanFactory beanFactory;
	private CompleteService completeService;

	public DefaultTccService(BeanFactory beanFactory, CompleteService completeService) {
		this.beanFactory = beanFactory;
		this.completeService = completeService;
	}

	private Stage createStage(TryInfo tryInfo, Tcc tcc, String stageName, String beanName) {
		return new Stage(beanFactory, beanName, tryInfo,
				stageName);
	}

	public Stage createConfirm(TryInfo tryInfo, Method tryMethod, Tcc tcc, String beanName) {
		if (StringUtils.isEmpty(tcc.confirm())) {
			return null;
		}

		return createStage(tryInfo, tcc, tcc.confirm(), beanName);
	}

	public Stage createConfirm(MethodInvoker invoker, Object[] args, Object tryResult, Tcc tcc) {
		if (StringUtils.isEmpty(tcc.confirm())) {
			return null;
		}
		
		RuntimeBean runtimeBean = BeanUtils.getRuntimeBean(invoker.getInstance());
		if(runtimeBean == null){
			return null;
		}

		return createStage(new TryInfo(invoker, args, tryResult), tcc, tcc.confirm(), runtimeBean.getBeanDefinition().getId());
	}

	public Stage createCancel(MethodInvoker invoker, Object[] args, Object tryResult, Tcc tcc) {
		if (StringUtils.isEmpty(tcc.cancel())) {
			return null;
		}
		
		RuntimeBean runtimeBean = BeanUtils.getRuntimeBean(invoker.getInstance());
		if(runtimeBean == null){
			return null;
		}

		return createStage(new TryInfo(invoker, args, tryResult), tcc, tcc.cancel(), runtimeBean.getBeanDefinition().getId());
	}

	public Complete registerComplete(Stage stage) throws Exception {
		return completeService.register(stage);
	}
}
