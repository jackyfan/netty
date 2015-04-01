package com.jackyfan.netty.pio;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class TimeClientHandler extends ChannelHandlerAdapter {
	private static final String ORDER = "QUERY TIME ORDER" + System.getProperty("line.separator");

	private byte[] req;

	public TimeClientHandler() {
		req = ORDER.getBytes();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.print("exception:" + cause.getMessage());
		ctx.close();
	}

	/**
	 * 方法用途:服务器返回应答消息时调用该方法 <br>
	 * 实现步骤: <br>
	 * 
	 * @param ctx
	 * @param msg
	 * @throws Exception
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = (ByteBuf) msg;
		byte[] resp = new byte[buf.readableBytes()];
		buf.readBytes(resp);
		System.out.println("Now is:" + new String(resp, "UTF-8"));

	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	/**
	 * 方法用途: 和服务端链路建立成功后线程会调用channelActive<br>
	 * 实现步骤: <br>
	 * 
	 * @param ctx
	 * @throws Exception
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ByteBuf firstMessage;
		// 链路建立成功后发送查询请求给服务端
		for (int i = 0; i < 10; i++) {
			firstMessage = Unpooled.buffer(req.length);
			firstMessage.writeBytes(req);
			ctx.writeAndFlush(firstMessage);
		}

	}

}
