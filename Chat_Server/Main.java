
/*
  css���Ͽ��� GUI���� ����
 */

package application;
	
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;


public class Main extends Application {
	
    // ExecutorService�� �������� �����带 ȿ�������� �����ϰ� ���ִ� ��ǥ���� ���̺귯��
	// ������ �ڿ����� ���������� ������ ����ϱ� ���� ������Ǯ ���
	public static ExecutorService threadPool;
	public static Vector<Client> clients = new Vector<Client>();
	
	ServerSocket serverSocket;
	
	// ������ �������Ѽ� Ŭ���̾�Ʈ�� ������ ��ٸ��� �޼ҵ�
	public void startServer(String IP,int port)
	{
		try {
		serverSocket = new ServerSocket();    // �������� Ŭ�� ���� ���� ����
		serverSocket.bind(new InetSocketAddress(IP,port));   // �ּ� bind
		} catch(Exception e) {
			e.printStackTrace();
			if(!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}
		
		//Ŭ���̾�Ʈ�� ������ ������ ��� ��ٸ��� ������
		Runnable thread = new Runnable() {

			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket));   //Ŭ���̾�Ʈ �迭�� ���Ӱ� ������ Ŭ���̾�Ʈ �߰���Ŵ
						System.out.println("[Ŭ���̾�Ʈ ����]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
					} catch(Exception e) {
						e.printStackTrace();
						if(!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
				}
			}
		};
		threadPool = Executors.newCachedThreadPool();  //������Ǯ �ʱ�ȭ
		threadPool.submit(thread);
	}
	
	// ������ �۵��� ������Ű�� �޼ҵ�
	public void stopServer()
	{
		try {
			//���� �۵� ���� ��� ���� �ݱ�
			Iterator<Client> iterator = clients.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			// ���� ���� ��ü �ݱ�
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			//������ Ǯ �����ϱ�
			if(threadPool != null && !threadPool.isShutdown()) {
				threadPool.isShutdown();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// UI�� �����ϰ�, ���������� ���α׷��� ���۽�Ű�� �޼ҵ�
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();  //�ϳ��� ���̾ƿ����� �ٸ� ������ ��Ҹ� ����
		root.setPadding(new Insets(5));
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);   //���� �Ұ���
		textArea.setFont(new Font("��������", 15));
		root.setCenter(textArea);
		
		Button toggleButton = new Button("�����ϱ�");   //toggleButton�� ����ġ ������(���� �� �ְ� �س�)
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1,0,0,0));
		root.setBottom(toggleButton);
		
		String IP = "127.0.0.1";
		int port = 9876;
		
		toggleButton.setOnAction(event->{
			if(toggleButton.getText().equals("�����ϱ�")) {
				startServer(IP,port);
				Platform.runLater(()->{     //javafx�� ��ư�� �������� �ٷ� �ؽ�Ʈ�� ���� �ϸ� �ȵ�-> runLAter�Լ� ���
					String message = String.format("[���� ����]\n",IP,port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");   //��۹�ư�� �����ϱ�� �ٲ�
				});
			} else {
				stopServer();
				Platform.runLater(()->{     //javafx�� ��ư�� �������� �ٷ� �ؽ�Ʈ�� ���� �ϸ� �ȵ�-> runLAter�Լ� ���
					String message = String.format("[���� ����]\n",IP,port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");
				});
			}
		});
		
		Scene scene = new Scene(root, 400, 400);
		primaryStage.setTitle("[ä�� ����]");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	// ���α׷��� ������
	public static void main(String[] args) {
		launch(args);
	}
}