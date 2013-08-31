package org.summercool.hsf.util;

import java.util.Random;

/**
 * 
 * @author 简道
 */
public class RandomUtil {
	private static Random random = new Random();

	public static int nextInt() {
		return random.nextInt();
	}

	public static int nextInt(int n) {
		return random.nextInt(n);
	}
}
