package org.summercool.hsf.compression.strategy;

/**
 * @ClassName: CompressionResult
 * @Description: 压缩结果
 * @author 简道
 * @date 2011-9-29 下午2:03:44
 */
public class CompressionResult {
	/**
	 * @Fields isCompressed : 是否压缩
	 */
	boolean isCompressed;
	/**
	 * @Fields buffer : 内容
	 */
	byte[] buffer;

	public boolean isCompressed() {
		return isCompressed;
	}

	public void setCompressed(boolean isCompressed) {
		this.isCompressed = isCompressed;
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	public CompressionResult() {

	}

	public CompressionResult(boolean isCompressed, byte[] buffer) {
		this.isCompressed = isCompressed;
		this.buffer = buffer;
	}
}
