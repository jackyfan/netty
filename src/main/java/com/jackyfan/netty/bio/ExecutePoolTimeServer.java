package com.jackyfan.netty.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * <p>
 * Description: 伪异步IO，通过线程池和阻塞队列实现，但输入流的readLine方法和输出流的write方法还是阻塞的。
 * </p>
 * 
 * @date 2015年3月12日
 * @author Jacky.Fan
 * @version 1.0
 */
public class ExecutePoolTimeServer {

	public static void main(String[] args) {
		int port = 8080;
		if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}

		}
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
			System.out.println("The time server is start with port:" + port);
			Socket socket = null;
			TimeServerHandlerExecutePool executor = new TimeServerHandlerExecutePool(50, 1000);
			while (true) {
				socket = server.accept();
				executor.execute(new TimeServerHandler(socket));
				// new Thread(new TimeServerHandler(socket)).start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (server != null) {
				try {
					System.out.println("The time server close");
					server.close();
					server = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

}
