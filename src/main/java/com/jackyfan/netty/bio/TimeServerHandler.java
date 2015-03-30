package com.jackyfan.netty.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

public class TimeServerHandler implements Runnable {
	private Socket socket;
	private final String ORDER = "QUERY TIME ORDER";
	private final String BAD_ORDER = "BAD ORDER";

	public TimeServerHandler(Socket socket) {
		this.socket = socket;
	}

	public void run() {
		BufferedReader in = null;
		PrintWriter out = null;

		try {
			in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			out = new PrintWriter(this.socket.getOutputStream(), true);
			String currentTime, body = null;
			while (true) {
				body = in.readLine();
				if (body == null)
					break;

				System.out.println("The time server receive order:" + body);
				if (ORDER.equalsIgnoreCase(body)) {
					currentTime = ""+System.currentTimeMillis();
				} else {
					currentTime = BAD_ORDER;
				}
				out.println(currentTime);

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			if (out != null) {
				out.close();
				out = null;
			}
			if (this.socket != null) {
				try {
					this.socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				this.socket = null;
			}

		}

	}
}
