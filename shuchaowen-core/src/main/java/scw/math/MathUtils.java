package scw.math;

import java.math.BigDecimal;

public final class MathUtils {
	private MathUtils() {
	};

	/**
	 * 求最大公约数
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static int greatestCommonDivisor(int a, int b) {
		int gongyue = 0;
		if (a < b) { // 交换a、b的值
			a = a + b;
			b = a - b;
			a = a - b;
		}
		if (a % b == 0) {
			gongyue = b;
		}
		while (a % b > 0) {
			a = a % b;
			if (a < b) {
				a = a + b;
				b = a - b;
				a = a - b;
			}
			if (a % b == 0) {
				gongyue = b;
			}
		}
		return gongyue;
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

	/**
	 * 求最大公约数 更相减损法
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static int gcd(int a, int b) {
		while (a != b) {
			if (a > b)
				a -= b;
			else
				b -= a;
		}
		return a;
	}

	/**
	 * 乘法
	 * 
	 * @param number1
	 * @param number2
	 * @return
	 */
	public static BigDecimal multiply(String number1, String number2) {
		BigDecimal bigDecimal = new BigDecimal(number1);
		return bigDecimal.multiply(new BigDecimal(number2));
	}

	/**
	 * 乘法
	 * 
	 * @param number1
	 * @param number2
	 * @return
	 */
	public static BigDecimal multiply(double number1, double number2) {
		return multiply(number1 + "", number2 + "");
	}
}