
/*
 * 한명의 클라이언트와 통신하기 위한 기능을 모아놓은 클래스
 */

package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
	Socket socket;
	
	public Client(Socket socket) 
	{
		this.socket = socket;   //초기화
		receive();
	}
	
	//클라이언로부터 메시지를 전달받는 메소드
	public void receive()
	{
		// 하나의 스레드 만들 때 Runnable객체 많이 이용
		Runnable thread = new Runnable() 
		{
			// Runnable 안엔 run함수 있어야함
			// run함수엔 하나의 스레드가 어떤 모듈로서 동작할것인지 정의
			@Override
			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream();
						byte[] buffer = new byte[512];
						int length = in.read(buffer);
						while(length == -1) throw new IOException();
						System.out.println("[메시지 수신 성공]" 
						+ socket.getRemoteSocketAddress()
						+ ": " + Thread.currentThread().getName());
						String message = new String(buffer,0,length,"UTF-8");
						for(Client client : Main.clients)
						{
							client.send(message);
						}
					}
				} catch(Exception e) {
					try {
						System.out.println("[메시지 수신 오류]"
						+ socket.getRemoteSocketAddress()
						+ ": " + Thread.currentThread().getName());
					}catch(Exception e2) {
					   e.printStackTrace();
					}
				}
			}
			
		};
		Main.threadPool.submit(thread);   // 스레드풀에 만들어진 스레드 등록
	}
	
	//클라이언트에게 메시지를 전송하는 메소드
	public void send(String message)
	{
		Runnable thread = new Runnable() {

			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch(Exception e) {
					try {
						System.out.println("[메시지 송신 오류]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						Main.clients.remove(Client.this);
						socket.close();
					}catch(Exception e2) {
						   e.printStackTrace();
						}
				}
				
			}
			
		};
		Main.threadPool.submit(thread);
	}
}
