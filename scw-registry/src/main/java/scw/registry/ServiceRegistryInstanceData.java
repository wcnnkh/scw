package scw.registry;

import java.io.Serializable;
import java.net.InetSocketAddress;

import scw.mapper.MapperUtils;
import scw.util.attribute.SimpleAttributes;

public class ServiceRegistryInstanceData extends SimpleAttributes<String, String> implements Serializable {
	private static final long serialVersionUID = 1L;
	private InetSocketAddress serviceAddress;

	public ServiceRegistryInstanceData() {
	}

	public ServiceRegistryInstanceData(ServiceRegistryInstanceData serviceRegistryInstanceData) {
		super(serviceRegistryInstanceData);
		this.serviceAddress = serviceRegistryInstanceData.serviceAddress;
	}

	public InetSocketAddress getServiceAddress() {
		return serviceAddress;
	}

	public void setServiceAddress(InetSocketAddress serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

	@Override
	public String toString() {
		return MapperUtils.getMapper().toString(this);
	}
}
