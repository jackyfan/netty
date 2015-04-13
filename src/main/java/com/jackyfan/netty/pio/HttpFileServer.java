package com.jackyfan.netty.pio;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpFileServer {
	public void run(final int port, final String url) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							/**
							 * 添加多个解码器<br/>
							 * 1.请求消息解码器 <br/>
							 * 2.HttpDecoder HttpObjectAggregator聚合器解码器
							 * ：将多条消息转换成单一的FullHttpRequest或FullHttpResponse
							 * ，因为HttpDecoder会在每个HTTP消息中会生成多个消息对象<br/>
							 * (1)HttpRequest/HttpResponse<br/>
							 * (2)HttpContent<br/>
							 * (3)LastHttpContent<br/>
							 * 3.HttpResponseEncoder 响应编码器 4.ChunkedWriteHandler
							 * 支持异步发送大的码流（大文件），但不会占用过多内存，防止Java内存溢出。
							 * 5.HttpFileServerHandler 自定义处理器
							 */

							ch.pipeline().addLast("http-decoder", new HttpRequestDecoder())
									.addLast("http-aggregator", new HttpObjectAggregator(65536))
									.addLast("http-encoder", new HttpResponseEncoder())
									.addLast("http-chunked", new ChunkedWriteHandler())
									.addLast("fileServerHandler", new HttpFileServerHandler(url));

						}

					});
			ChannelFuture future = b.bind(port).sync();
			System.out.println("Http file server start");
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			bossGroup.shutdownGracefully();
			workGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		int port = 8080;
		String url = "src/com/jackyfan/netty";
		new HttpFileServer().run(port, url);

	}

}
