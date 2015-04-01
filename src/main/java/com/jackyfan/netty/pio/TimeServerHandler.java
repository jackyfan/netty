package com.jackyfan.netty.pio;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class TimeServerHandler extends ChannelHandlerAdapter {
	private static final String ORDER = "QUERY TIME ORDER";
	private static final String BAD_ORDER = "BAD ORDER";
	private int counter;
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		counter++;
		ByteBuf buf = (ByteBuf) msg;
		byte[] req = new byte[buf.readableBytes()];
		buf.readBytes(req);
		String body = new String(req, "UTF-8");
		System.out.println("The time server receive order: " + body+";counter:"+counter);
		String currentTime;
		if (ORDER.equalsIgnoreCase(body)) {
			currentTime = "" + System.currentTimeMillis();
		} else {
			currentTime = BAD_ORDER;
		}
		currentTime += System.getProperty("line.separator");
		ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
		ctx.writeAndFlush(resp);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		//将队列的消息写入到SocketChannel中发送到对方
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		//发送异常时关闭ctx
		ctx.close();
	}
}
