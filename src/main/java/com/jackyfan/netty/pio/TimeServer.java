package com.jackyfan.netty.pio;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class TimeServer {
	public void bind(int port) throws Exception {
		// 创建用于接收客户端的TCP连接的线程组
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		// 创建用于处理I/O相关的读写操作的线程组
		EventLoopGroup workGroup = new NioEventLoopGroup();
		try {
			// 创建NIO服务端的辅助启动类
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024)
					.childHandler(new ChildChannelHandler());
			// 调用同步阻塞方法sync等待绑定操作完成
			ChannelFuture f = b.bind(port).sync();
			// 调用阻塞方法等待服务器链路关闭后main方法退出
			f.channel().closeFuture().sync();
		} finally {
			// 关闭线程组，释放相关资源
			bossGroup.shutdownGracefully();
			workGroup.shutdownGracefully();
		}
	}

	private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel channel) throws Exception {
			// 添加LineBasedFrameDecoder解码器，作用：以换行符为结束标志
			// 添加StringDecoder解码器 作用：将接收对象换成字符串
			channel.pipeline().addLast(new LineBasedFrameDecoder(1024)).addLast(new StringDecoder())
					.addLast(new TimeServerHandler());
		}

	}

	public static void main(String[] args) throws Exception {
		int port = 8080;
		if (args != null && args.length > 0) {
			port = Integer.valueOf(args[0]);
		}
		new TimeServer().bind(port);
	}
}
