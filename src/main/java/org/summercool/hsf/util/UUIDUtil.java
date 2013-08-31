package org.summercool.hsf.util;

import java.util.UUID;

/**
 * @ClassName: UUIDUtil
 * @Description: UUID辅助类
 * @author 简道
 * @date 2011-9-29 下午1:33:58
 * 
 */
public class UUIDUtil {
	public static String random() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}
