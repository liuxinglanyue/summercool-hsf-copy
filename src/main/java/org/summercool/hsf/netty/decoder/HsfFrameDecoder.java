package org.summercool.hsf.netty.decoder;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

/**
 * @Title: HsfFrameDecoder.java
 * @Package org.summercool.hsf.netty.decoder
 * @Description: Hsf框架实现的FrameDecoder，用以替换Netty自带的FrameDecoder
 * @author 简道
 * @date 2011-11-15 下午4:20:18
 * @version V1.0
 */
public abstract class HsfFrameDecoder extends SimpleChannelUpstreamHandler {

	private final boolean unfold;
	private ConcurrentHashMap<Integer, ChannelBuffer> cumulation = new ConcurrentHashMap<Integer, ChannelBuffer>();

	protected HsfFrameDecoder() {
		this(false);
	}

	protected HsfFrameDecoder(boolean unfold) {
		this.unfold = unfold;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

		Object m = e.getMessage();
		if (!(m instanceof ChannelBuffer)) {
			ctx.sendUpstream(e);
			return;
		}

		ChannelBuffer input = (ChannelBuffer) m;
		if (!input.readable()) {
			return;
		}

		ChannelBuffer cumulation = cumulation(ctx);
		if (cumulation.readable()) {
			cumulation.discardReadBytes();
			cumulation.writeBytes(input);
			callDecode(ctx, e.getChannel(), cumulation, e.getRemoteAddress());
		} else {
			callDecode(ctx, e.getChannel(), input, e.getRemoteAddress());
			if (input.readable()) {
				cumulation.writeBytes(input);
			}
		}
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		cleanup(ctx, e);
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		cleanup(ctx, e);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		ctx.sendUpstream(e);
	}

	/**
	 * Decodes the received packets so far into a frame.
	 * 
	 * @param ctx
	 *        the context of this handler
	 * @param channel
	 *        the current channel
	 * @param buffer
	 *        the cumulative buffer of received packets so far. Note that the buffer might be empty, which means you
	 *        should not make an assumption that the buffer contains at least one byte in your decoder implementation.
	 * 
	 * @return the decoded frame if a full frame was received and decoded. {@code null} if there's not enough data in
	 *         the buffer to decode a frame.
	 */
	protected abstract Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception;

	/**
	 * Decodes the received data so far into a frame when the channel is disconnected.
	 * 
	 * @param ctx
	 *        the context of this handler
	 * @param channel
	 *        the current channel
	 * @param buffer
	 *        the cumulative buffer of received packets so far. Note that the buffer might be empty, which means you
	 *        should not make an assumption that the buffer contains at least one byte in your decoder implementation.
	 * 
	 * @return the decoded frame if a full frame was received and decoded. {@code null} if there's not enough data in
	 *         the buffer to decode a frame.
	 */
	protected Object decodeLast(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
		return decode(ctx, channel, buffer);
	}

	private void callDecode(ChannelHandlerContext context, Channel channel, ChannelBuffer cumulation,
			SocketAddress remoteAddress) throws Exception {

		while (cumulation.readable()) {
			int oldReaderIndex = cumulation.readerIndex();
			Object frame = decode(context, channel, cumulation);
			if (frame == null) {
				if (oldReaderIndex == cumulation.readerIndex()) {
					// Seems like more data is required.
					// Let us wait for the next notification.
					break;
				} else {
					// Previous data has been discarded.
					// Probably it is reading on.
					continue;
				}
			} else if (oldReaderIndex == cumulation.readerIndex()) {
				throw new IllegalStateException("decode() method must read at least one byte "
						+ "if it returned a frame (caused by: " + getClass() + ")");
			}

			unfoldAndFireMessageReceived(context, remoteAddress, frame);
		}
	}

	private void unfoldAndFireMessageReceived(ChannelHandlerContext context, SocketAddress remoteAddress, Object result) {
		if (unfold) {
			if (result instanceof Object[]) {
				for (Object r : (Object[]) result) {
					Channels.fireMessageReceived(context, r, remoteAddress);
				}
			} else if (result instanceof Iterable<?>) {
				for (Object r : (Iterable<?>) result) {
					Channels.fireMessageReceived(context, r, remoteAddress);
				}
			} else {
				Channels.fireMessageReceived(context, result, remoteAddress);
			}
		} else {
			Channels.fireMessageReceived(context, result, remoteAddress);
		}
	}

	private void cleanup(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		try {
			ChannelBuffer buffer = this.cumulation.remove(ctx.getChannel());
			if (buffer == null) {
				return;
			}

			if (buffer.readable()) {
				// Make sure all frames are read before notifying a closed channel.
				callDecode(ctx, ctx.getChannel(), buffer, null);
			}

			// Call decodeLast() finally. Please note that decodeLast() is
			// called even if there's nothing more to read from the buffer to
			// notify a user that the connection was closed explicitly.
			Object partialFrame = decodeLast(ctx, ctx.getChannel(), buffer);
			if (partialFrame != null) {
				unfoldAndFireMessageReceived(ctx, null, partialFrame);
			}
		} finally {
			ctx.sendUpstream(e);
		}
	}

	private ChannelBuffer cumulation(ChannelHandlerContext ctx) {
		ChannelBuffer buffer = this.cumulation.get(ctx.getChannel().getId());

		if (buffer == null) {
			synchronized (ctx.getChannel()) {
				buffer = this.cumulation.get(ctx.getChannel().getId());

				if (buffer == null) {
					buffer = ChannelBuffers.dynamicBuffer(ctx.getChannel().getConfig().getBufferFactory());
					this.cumulation.put(ctx.getChannel().getId(), buffer);
					
					// remove buffer when channel closed.
					ctx.getChannel().getCloseFuture().addListener(new ChannelFutureListener() {
						@Override
						public void operationComplete(ChannelFuture future) throws Exception {
							cumulation.remove(future.getChannel().getId());
						}
					});
				}
			}
		}

		return buffer;
	}
}