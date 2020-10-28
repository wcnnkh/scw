package scw.memcached;

import scw.data.DataTemplete;
import scw.data.cas.CASOperations;

public interface Memcached extends DataTemplete {
	CASOperations getCASOperations();
}
