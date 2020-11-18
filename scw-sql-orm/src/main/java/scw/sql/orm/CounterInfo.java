package scw.sql.orm;

import java.io.Serializable;

public class CounterInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	private final double min;
	private final double max;
	
	public CounterInfo(double min, double max){
		this.min = min;
		this.max = max;
	}
	
	public double getMin() {
		return min;
	}
	public double getMax() {
		return max;
	}
}
