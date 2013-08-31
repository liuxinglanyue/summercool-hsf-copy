package org.summercool.hsf.util;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @ClassName: ZLibUtils
 * @Description: ZLib辅助类
 * @author 简道
 * @date 2011-9-29 下午1:34:14
 *
 */
public abstract class ZLibUtils {

	/**
	 * 压缩
	 * 
	 * @param data
	 *        待压缩数据
	 * @return byte[] 压缩后的数据
	 */
	public static byte[] compress(byte[] data) {
		byte[] output = new byte[0];
		Deflater compresser = new Deflater();
		
		compresser.reset();
		compresser.setInput(data);
		compresser.finish();
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
		
		try {
			byte[] buf = new byte[1024];
			while (!compresser.finished()) {
				int i = compresser.deflate(buf);
				bos.write(buf, 0, i);
			}
			
			output = bos.toByteArray();
		} catch (Exception e) {
			output = data;
		}
		
		compresser.end();
		
		return output;
	}

	/**
	 * 解压缩
	 * 
	 * @param data
	 *        待压缩的数据
	 * @return byte[] 解压缩后的数据
	 */
	public static byte[] decompress(byte[] data) {
		byte[] output = new byte[0];
		Inflater decompresser = new Inflater();
		
		decompresser.reset();
		decompresser.setInput(data);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
		
		try {
			byte[] buf = new byte[1024];
			while (!decompresser.finished()) {
				int i = decompresser.inflate(buf);
				bos.write(buf, 0, i);
			}
			
			output = bos.toByteArray();
		} catch (Exception e) {
			output = data;
		}
		
		decompresser.end();
		
		return output;
	}
}
