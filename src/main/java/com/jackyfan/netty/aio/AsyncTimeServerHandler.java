package com.jackyfan.netty.aio;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

public class AsyncTimeServerHandler implements Runnable {
	private int port;
	private CountDownLatch latch;
	AsynchronousServerSocketChannel assc;

	public AsyncTimeServerHandler(int port) {
		this.port = port;
	}

	public void run() {

	}

}
