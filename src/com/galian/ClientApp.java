package com.galian;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientApp {

	static Socket socket = null;
	private static boolean mRunning = false;

	public static void main(String[] args) {
		startClient();
	}

	private static void startClient() {
		try {
			socket = new Socket("127.0.0.1", 8888);
			mRunning = true;
			new Thread(new InThread(socket)).start();
			new Thread(new OutThread(socket)).start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static class InThread implements Runnable {
		Socket socket = null;

		public InThread(Socket s) {
			socket = s;
		}

		@Override
		public void run() {
			while (mRunning) {
				if (socket.isClosed()) {
					mRunning = false;
					break;
				}
				InputStream in = null;
				try {
					in = socket.getInputStream();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				InputStreamReader reader = new InputStreamReader(in);
				try {
					char[] buf = new char[1024000];
					int cnt = reader.read(buf);
					if (cnt > 0) {
						String msg = new String(buf, 0, cnt);
						System.out.println(msg);
					}
				} catch (IOException e) {
					String error = e.getMessage();
					if (!error.equalsIgnoreCase("Socket closed")) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	static class OutThread implements Runnable {
		Socket socket = null;

		public OutThread(Socket s) {
			socket = s;
		}

		@Override
		public void run() {
			System.out.println("Please type in something...");
			System.out.println("Type in 'exit' to exit.");
			System.out.println("Type in 'getattacksurface package_name' to get attack surface.");
			System.out.println();
			while (mRunning) {
				if (socket.isClosed()) {
					mRunning = false;
					break;
				}

				OutputStream out = null;
				try {
					out = socket.getOutputStream();
				} catch (IOException e2) {
					e2.printStackTrace();
				}

				InputStreamReader reader = new InputStreamReader(System.in);
				String msg = "";
				try {
					char[] buf = new char[10240];
					int cnt = reader.read(buf);
					if (cnt > 0) {
						msg = new String(buf, 0, cnt);
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				msg = msg.trim();// delete trailing linefeed
				if (msg.equalsIgnoreCase("exit")) {
					mRunning = false;
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
				/*
				 * input "getattacksurface package_name", get the attack surface of that package
				 */
				try {
					out.write(msg.getBytes());
					out.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
