package org.summercool.hsf.compression.strategy;

import java.io.IOException;

import org.summercool.hsf.compression.algorithm.CompressionAlgorithm;
import org.summercool.hsf.compression.algorithm.ZLibCompressionAlgorithm;

/**
 * @Title: ThresholdCompressionStrategy.java
 * @Package org.summercool.hsf.compression.strategy
 * @Description: 阀值压缩代理类，超过阀值则压缩，阀值缺省为10240(10K), 缺省使用
 *               {@link ZLibCompressionAlgorithm}进行压缩
 * @author 简道
 * @date 2011-9-16 下午4:14:32
 * @version V1.0
 */
public class ThresholdCompressionStrategy implements CompressionStrategy {
	/**
	 * 阀值，缺省为10240，超过阀值则压缩
	 */
	int threshold;

	/**
	 * 压缩算法, 缺省使用{@link ZLibCompressionAlgorithm}
	 */
	CompressionAlgorithm compressionAlgorithm;

	public ThresholdCompressionStrategy() {
		this(10240);
	}

	public ThresholdCompressionStrategy(int threshold) {
		this(threshold, new ZLibCompressionAlgorithm());
	}

	public ThresholdCompressionStrategy(int threshold, CompressionAlgorithm compressionAlgorithm) {
		this.threshold = threshold;
		this.compressionAlgorithm = compressionAlgorithm;
	}

	public CompressionResult compress(byte[] buffer) throws IOException {
		CompressionResult result = new CompressionResult(false, buffer);
		if (buffer.length > threshold) {
			byte[] bytes = compressionAlgorithm.compress(buffer);

			if (bytes.length < buffer.length) {
				result.setBuffer(bytes);
				result.setCompressed(true);
			}
		}
		return result;
	}

	public byte[] decompress(byte[] buffer) throws IOException {
		return compressionAlgorithm.decompress(buffer);
	}

	public void setCompressionAlgorithm(CompressionAlgorithm compressionAlgorithm) {
		this.compressionAlgorithm = compressionAlgorithm;
	}

	public CompressionAlgorithm getCompressionAlgorithm() {
		return compressionAlgorithm;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}
}
