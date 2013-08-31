package org.summercool.hsf.test.kryo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.summercool.hsf.serializer.KryoSerializer;

public class KryoTest{
	private static KryoSerializer kryoSerializer = new KryoSerializer();
	private static String str = "大家都有过复制一个大文件时，久久等待却不见结束，明明很着急却不能取消的情况吧——一旦取消，一切都要从头开始！大家都有过复制一个大文件时，久久等待却不见结束，明明很着急却不能取消的情况吧——一旦取消，一切都要从头开始！大家都有过复制一个大文件时大家都有过复制一个大文件时";  
	
	public static void main(String[] args) throws InterruptedException {
		long beginTime = System.currentTimeMillis();
		
		ExecutorService executor = Executors.newFixedThreadPool(100);
		for (int i = 0; i < 1000000; i++) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					kryoSerializer.serialize(str);
				}
			});
		}
		executor.shutdown();
		executor.awaitTermination(10, TimeUnit.DAYS);
		
		long endTime = System.currentTimeMillis();
		System.out.println("cost " + (endTime - beginTime));
	}
}