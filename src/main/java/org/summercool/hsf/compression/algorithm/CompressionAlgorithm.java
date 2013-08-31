package org.summercool.hsf.compression.algorithm;

import java.io.IOException;

/**
 * @ClassName: CompressionAlgorithm
 * @Description: 压缩算法接口
 * @author 简道
 * @date 2011-9-29 下午2:02:10
 */
public interface CompressionAlgorithm {
	/**
	 * @Title: compress
	 * @Description: 压缩
	 * @author 简道
	 * @param buffer
	 *            待压缩内容
	 * @throws IOException
	 *             设定文件
	 * @return byte[] 返回类型
	 */
	byte[] compress(byte[] buffer) throws IOException;

	/**
	 * @Title: decompress
	 * @Description:解压
	 * @author 简道
	 * @param buffer
	 *            待解压内容
	 * @throws IOException
	 *             设定文件
	 * @return byte[] 返回类型
	 */
	byte[] decompress(byte[] buffer) throws IOException;
}
