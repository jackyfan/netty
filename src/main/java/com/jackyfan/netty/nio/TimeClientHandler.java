package com.jackyfan.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TimeClientHandler implements Runnable {
	private static final String ORDER = "QUERY TIME ORDER";
	private String host;
	private int port;
	private Selector selector;
	private SocketChannel socketChannel;
	private volatile boolean stop;

	public TimeClientHandler(String host, int port) {
		this.host = (host == null ? "127.0.0.1" : host);
		this.port = port;
		try {
			selector = Selector.open();
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void run() {
		try {
			doConnect();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
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
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		if (selector != null) {
			try {
				// 多路复用器关闭后，所有注册在上面的Channel和Pipe等资源都会自动注销并关闭。
				selector.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void handlerInput(SelectionKey key) throws IOException {
		if (key.isValid()) {
			SocketChannel sc = (SocketChannel) key.channel();
			// 判断是否连接成功，如果成功说明服务器返回了syn_ack消息
			if (key.isConnectable()) {
				// 判断是否完成连接
				if (sc.finishConnect()) {
					// 注册到selector
					sc.register(selector, SelectionKey.OP_READ);
					this.doWrite(sc);
				} else {
					// 连接失败，进程退出
					System.exit(1);
				}
			}
			// 判断是否可读
			if (key.isReadable()) {
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				int size = sc.read(readBuffer);
				if (size > 0) {
					readBuffer.flip();
					byte[] bytes = new byte[readBuffer.remaining()];
					readBuffer.get(bytes);
					String body = new String(bytes, "UTF-8");
					System.out.println("Now is:" + body);
					this.stop = true;
				} else if (size < 0) {
					key.cancel();
					sc.close();
				}
			}
		}
	}

	private void doConnect() throws IOException {
		// 如果可以直接连接成功，则将socketChannel注册到selector上，发送“读应答”请求消息
		/* 如果没有直接连接成功，说明服务器没有返回TCP握手应答消息，但并不代表连接失败。 */
		if (socketChannel.connect(new InetSocketAddress(host, port))) {
			socketChannel.register(selector, SelectionKey.OP_READ);
			this.doWrite(socketChannel);
		} else {
			// 如果失败，发送“连接”请求消息，当服务器返回TCP
			// syn_ack消息后，selector就能够轮询到这个socketChannel处于连接就绪状态
			socketChannel.register(selector, SelectionKey.OP_CONNECT);
		}
	}

	private void doWrite(SocketChannel sc) throws IOException {
		byte[] bytes = this.ORDER.getBytes();
		ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
		writeBuffer.put(bytes);
		writeBuffer.flip();
		sc.write(writeBuffer);
		if (!writeBuffer.hasRemaining()) {
			System.out.println("Send order to server succeed.");
		}
	}
}
