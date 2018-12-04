package io.transport.common.utils;

import java.math.BigDecimal;

/**
 * Mathematical computing tool.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2018年6月2日
 * @since
 */
public class Maths {

	/**
	 * 提供精确加法计算的add方法
	 * 
	 * @param val1
	 *            被加数
	 * @param val2
	 *            加数
	 * @return 两个参数的和
	 */
	public static double add(double val1, double val2) {
		BigDecimal b1 = new BigDecimal(Double.toString(val1));
		BigDecimal b2 = new BigDecimal(Double.toString(val2));
		return b1.add(b2).doubleValue();
	}

	/**
	 * 提供精确减法运算的sub方法
	 * 
	 * @param val1
	 *            被减数
	 * @param val2
	 *            减数
	 * @return 两个参数的差
	 */
	public static double sub(double val1, double val2) {
		BigDecimal b1 = new BigDecimal(Double.toString(val1));
		BigDecimal b2 = new BigDecimal(Double.toString(val2));
		return b1.subtract(b2).doubleValue();
	}

	/**
	 * 提供精确乘法运算的mul方法
	 * 
	 * @param val1
	 *            被乘数
	 * @param val2
	 *            乘数
	 * @return 两个参数的积
	 */
	public static double mul(double val1, double val2) {
		BigDecimal b1 = new BigDecimal(Double.toString(val1));
		BigDecimal b2 = new BigDecimal(Double.toString(val2));
		return b1.multiply(b2).doubleValue();
	}

	/**
	 * 提供精确乘法运算的mul方法
	 * 
	 * @param val1
	 *            被乘数
	 * @param val2
	 *            乘数
	 * @param scale
	 *            精确范围
	 * @return 两个参数的积
	 */
	public static double mul(double val1, double val2, int scale) {
		BigDecimal b1 = new BigDecimal(Double.toString(val1));
		BigDecimal b2 = new BigDecimal(Double.toString(val2));
		return b1.multiply(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 提供精确的除法运算方法div
	 * 
	 * @param val1
	 *            被除数
	 * @param val2
	 *            除数
	 * @param scale
	 *            精确范围
	 * @return 两个参数的商
	 * @throws IllegalAccessException
	 */
	public static double div(double val1, double val2, int scale) throws IllegalAccessException {
		// 如果精确范围小于0，抛出异常信息
		if (scale < 0) {
			throw new IllegalAccessException("精确度不能小于0");
		}
		BigDecimal b1 = new BigDecimal(Double.toString(val1));
		BigDecimal b2 = new BigDecimal(Double.toString(val2));
		return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 四舍五入取精度
	 * 
	 * @param value
	 * @param scale
	 * @return
	 */
	public static double scaleUp(Double value, int scale) {
		if (value == null)
			return 0;

		return new BigDecimal(value).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static void main(String[] args) {
		System.out.println(scaleUp(647.0246, 2));
	}
}
