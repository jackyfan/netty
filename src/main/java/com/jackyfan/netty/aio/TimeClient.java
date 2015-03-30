package com.jackyfan.netty.aio;

import com.jackyfan.netty.nio.TimeClientHandler;

public class TimeClient {

	public static void main(String[] args) {
		int port = 8080;
		if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}

		}
		int i = 10;
		while (i > 0) {
			i--;
			new Thread(new TimeClientHandler("127.0.0.1", port)).start();
		}

	}
}
