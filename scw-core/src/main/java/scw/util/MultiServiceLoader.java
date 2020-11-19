package scw.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import scw.core.utils.CollectionUtils;

public class MultiServiceLoader<S> implements ServiceLoader<S>{
	private Collection<ServiceLoader<S>> serviceLoaders;
	
	public MultiServiceLoader(ServiceLoader<S> ...serviceLoaders){
		this(Arrays.asList(serviceLoaders));
	}
	
	public MultiServiceLoader(Collection<ServiceLoader<S>> serviceLoaders){
		this.serviceLoaders = CollectionUtils.isEmpty(serviceLoaders)? null:serviceLoaders;
	}
	
	public void reload() {
		if(serviceLoaders == null){
			return ;
		}
		
		for(ServiceLoader<S> serviceLoader : serviceLoaders){
			if(serviceLoader == null){
				continue;
			}
			
			serviceLoader.reload();
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Iterator<S> iterator() {
		return new MultiIterable(serviceLoaders).iterator();
	}
}
