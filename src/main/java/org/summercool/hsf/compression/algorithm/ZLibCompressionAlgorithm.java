package org.summercool.hsf.compression.algorithm;

import java.io.IOException;

import org.summercool.hsf.util.ZLibUtils;

/**
 * @Title: ZLibCompressionAlgorithm.java
 * @Package org.summercool.hsf.compression
 * @Description: ZLib压缩算法实现
 * @author 简道
 * @date 2011-9-16 下午4:06:02
 * @version V1.0
 */
public class ZLibCompressionAlgorithm implements CompressionAlgorithm {

	public byte[] compress(byte[] buffer) throws IOException {
		return ZLibUtils.compress(buffer);
	}

	public byte[] decompress(byte[] buffer) throws IOException {
		return ZLibUtils.decompress(buffer);
	}
}
