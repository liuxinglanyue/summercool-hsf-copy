package org.summercool.hsf.compression.strategy;

import java.io.IOException;

/**
 * @ClassName: CompressionStrategy
 * @Description: 压缩策略接口
 * @author 简道
 * @date 2011-9-16 下午4:09:46
 */
public interface CompressionStrategy {
	/**
	 * @Title: compress
	 * @Description: 根据策略压缩
	 * @author 简道
	 * @param buffer
	 *            待压缩内容
	 * @throws IOException
	 *             设定文件
	 * @return CompressionResult 返回类型
	 */
	CompressionResult compress(byte[] buffer) throws IOException;

	/**
	 * @Title: decompress
	 * @Description: 解压
	 * @author 简道
	 * @param buffer
	 *            待解压内容
	 * @throws IOException
	 *             设定文件
	 * @return byte[] 返回类型
	 */
	byte[] decompress(byte[] buffer) throws IOException;
}
