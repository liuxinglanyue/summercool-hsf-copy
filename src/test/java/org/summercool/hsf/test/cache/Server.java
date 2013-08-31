package org.summercool.hsf.test.cache;

import org.summercool.hsf.netty.decoder.LengthBasedDecoder;
import org.summercool.hsf.netty.encoder.LengthBasedEncoder;
import org.summercool.hsf.netty.handler.downstream.CompressionDownstreamHandler;
import org.summercool.hsf.netty.handler.downstream.SerializeDownstreamHandler;
import org.summercool.hsf.netty.handler.upstream.DecompressionUpstreamHandler;
import org.summercool.hsf.netty.handler.upstream.DeserializeUpstreamHandler;
import org.summercool.hsf.netty.service.HsfAcceptor;
import org.summercool.hsf.netty.service.HsfAcceptorImpl;
import org.summercool.hsf.test.listener.TestChannelListener;
import org.summercool.hsf.test.service.TestServiceImpl;

/**
 * @Title: Server.java
 * @Package org.summercool.hsf.test.ehcache
 * @Description: TODO(添加描述)
 * @author Administrator
 * @date 2012-3-19 下午10:48:42
 * @version V1.0
 */
public class Server {
	public static void main(String[] args) {
		HsfAcceptor acceptor = new HsfAcceptorImpl();
		acceptor.getHandlers().clear();
		CacheSerializer serializer = new CacheSerializer();
		
		acceptor.getHandlers().put("encode", new LengthBasedEncoder());
		acceptor.getHandlers().put("compress", new CompressionDownstreamHandler());
		acceptor.getHandlers().put("serialize", new SerializeDownstreamHandler(serializer));

		acceptor.getHandlers().put("decode", new LengthBasedDecoder());
		acceptor.getHandlers().put("decompress", new DecompressionUpstreamHandler());
		acceptor.getHandlers().put("deserialize", new DeserializeUpstreamHandler(serializer));
		
		acceptor.registerService(new ByteCacheServiceImpl());
		acceptor.bind(8082);
	}
}
