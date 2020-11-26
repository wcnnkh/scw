package scw.math;

import scw.core.instance.InstanceUtils;
import scw.math.gcd.DivisionAlgorithm;
import scw.math.gcd.GreatestCommonDivisor;

public final class MathUtils {
	private static final GreatestCommonDivisor GREATEST_COMMON_DIVISOR = InstanceUtils.loadService(GreatestCommonDivisor.class, DivisionAlgorithm.class.getName());
	
	private MathUtils() {
	};

	/**
	 * 计算最大公约数
	 * @return
	 */
	public static GreatestCommonDivisor getGreatestCommonDivisor() {
		return GREATEST_COMMON_DIVISOR;
	}


	/**
	 * 得到一组数是的最大值或最小值
	 * 
	 * @param isMax
	 * @param num
	 * @return
	 */
	public static long getMaxOrMinNum(boolean isMax, long... num) {
		long temp = num[0];
		for (long n : num) {
			if (n > temp && isMax) {
				temp = n;
			}
		}
		return temp;
	}
}
