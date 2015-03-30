package com.jackyfan.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class MultiplexerTimeServer implements Runnable {
	private static final String ORDER = "QUERY TIME ORDER";
	private static final String BAD_ORDER = "BAD ORDER";
	/* 多路复用器 */
	private Selector selector;
	/* server 通道 */
	private ServerSocketChannel serverChannel;

	private volatile boolean stop;

	public MultiplexerTimeServer(int port) {
		try {
			selector = Selector.open();
			serverChannel = ServerSocketChannel.open();
			/* 非阻塞 */
			serverChannel.configureBlocking(false);
			/* 绑定socket端口 */
			serverChannel.socket().bind(new InetSocketAddress(port), 1024);
			/* 将serverChannel注册到selector多路复用器 */
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("The time server is start in port:" + port);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	public void stop() {
		this.stop = true;
	}

	public void run() {
		while (!stop) {
			try {
				/* 设置睡眠（阻塞）时间1s */
				selector.select(1000);
				// 返回一个就绪状态的SelectionKey集合
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> it = selectedKeys.iterator();
				SelectionKey key = null;
				while (it.hasNext()) {
					key = it.next();
					it.remove();
					try {
						handlerInput(key);
					} catch (Exception e) {
						if (key != null) {
							key.cancel();
							if (key.channel() != null) {
								key.channel().close();
							}
						}
					}
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void handlerInput(SelectionKey key) throws IOException {
		/* 判断key是否有效 */
		if (key.isValid()) {
			/* 是否有新请求消息 */
			if (key.isAcceptable()) {
				//通过key获取ServerSocketChannel实例
				ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
				//接收客户端的连接请求，并创建SocketChannel实例，相当于完成TCP的三次握手
				SocketChannel sc = ssc.accept();
				sc.configureBlocking(false);
				sc.register(selector, SelectionKey.OP_READ);
			}
			/* 是否可读 */
			if (key.isReadable()) {
				SocketChannel sc = (SocketChannel) key.channel();
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				int readBytes = sc.read(readBuffer);
				if (readBytes > 0) {
					readBuffer.flip();
					byte[] bytes = new byte[readBuffer.remaining()];
					readBuffer.get(bytes);
					String body = new String(bytes, "UTF-8");
					System.out.println("The time server receive order:" + body);
					String currentTime;
					if (ORDER.equalsIgnoreCase(body)) {
						currentTime = "" + System.currentTimeMillis();
					} else {
						currentTime = BAD_ORDER;
					}
					doWrite(sc, currentTime);
				} else if (readBytes < 0) {
					key.cancel();
					sc.close();
				}
			}
		}
	}

	private void doWrite(SocketChannel channel, String resp) throws IOException {
		if (resp != null && resp.trim().length() > 0) {
			byte[] bytes = resp.getBytes();
			ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
			writeBuffer.put(bytes);
			writeBuffer.flip();
			channel.write(writeBuffer);
		}
	}

}
