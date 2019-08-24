/*
 * Ŭ���̾�Ʈ�� �����带 ������ ���� �ʿ䰡 ���� ������ ������Ǯ ���ʿ� 
 * ������ �޽����� �����ϴ� ������ �Ѱ�, �޽����� ���޹޴� ������ �Ѱ��� �ʿ�
 */


package application;
	
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;


public class Main extends Application {
	
	Socket socket;
	TextArea textArea;
	Stage window;
	
	Scene logScene, chatScene;
	
	//Ŭ���̾�Ʈ ���α׷� ���� �޼ҵ�
	public void startClient(String IP, int port) {
		//������Ǯ �ʿ����� �ʱ� ������ Runnable�ƴ� Thread���
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP,port);
					receive();
				} catch(Exception e) {
					if(!socket.isClosed()) {
						stopClient();
						System.out.println("[���� ���� ����]");
						Platform.exit();   //���α׷� ����
					}
				}
			}
		};
		thread.start();  //������ ����
	}
	
	//Ŭ���̾�Ʈ ���α׷� ���� �޼ҵ�
	public void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//�����κ��� �޽����� ���޹޴� �޼ҵ�
	//�޼����� ��� ���� �� �ֵ��� while���� ���
    public void receive() {
		while(true) {
			try {
				InputStream in = socket.getInputStream();
				byte[] buffer = new byte[512];
				int length = in.read(buffer);
				if(length == -1) throw new IOException();   //length�� -1�̸� IOException�߻�
				String message = new String(buffer,0,length,"UTF-8");
				Platform.runLater(()->{
					textArea.appendText(message);   //textArea�� GUI����� �ϳ��� ȭ�鿡 �޼��� �������
				});
			} catch(Exception e) {
				stopClient();
				break;
			}
		}
	}
    
    //������ �޽����� �����ϴ� �޼ҵ�
    public void send(String message) {
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();   //�޼��� ���� ���� �˸�
				} catch(Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}
    
    //�α��� ��
    public void LoginScene() {
    	
    	window = new Stage();
        
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        
        Button loginBtn = new Button("login");
        grid.add(loginBtn, 0, 0);
        
        loginBtn.setOnAction(event -> {
           window.setScene(chatScene);
        });
        logScene = new Scene(grid, 760, 480);
        window.setScene(logScene);
    }
    
    // ä�þ�
    public void ChatScene() {
    	window = new Stage();
    	
    	BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		HBox hbox = new HBox();    //Border���� �ϳ��� ���̾ƿ� �� ��
		hbox.setSpacing(5);
		
		//UserName text
		TextField userName = new TextField();
		userName.setPrefWidth(150);
		userName.setPromptText("�г����� �Է��ϼ���.");
		HBox.setHgrow(userName, Priority.ALWAYS);
		
		//IP,port text
		TextField IPText = new TextField("127.0.0.1");
		TextField portText = new TextField("9876");
		portText.setPrefWidth(80);
		
		hbox.getChildren().addAll(userName,IPText,portText);  //textBox�� ���������� textField�߰�
		root.setTop(hbox);   //hbox�� �� ������
		
		textArea = new TextArea();  //�ʱ�ȭ ��������Ѵ� 
		textArea.setEditable(false);   //���� �Ұ�
		root.setCenter(textArea);     //���Ϳ��� TextArea (top���� hbox)
		
		//�Է�â
		TextField input = new TextField();
		input.setPrefWidth(Double.MAX_VALUE);
		input.setDisable(true);      //��Ȱ��ȭ
		
		//ó�� send (�̰� ���ϸ� ó�� send�� �ȵ�)
		input.setOnAction(event->{
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");  //����ĭ ����ְ�
			input.requestFocus();  //�ٽ� �޼��� ���� �� �ְ� focus����� ����
		});
		
		//������ ��ư
		Button sendButton = new Button("������");
		sendButton.setDisable(true);
		
		sendButton.setOnAction(event->{
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");  //����ĭ ����ְ�
			input.requestFocus();  //�ٽ� �޼��� ���� �� �ְ� focus�� �Է�â�� ����
		});
		
		//���� ��ư
		//connButton ������ Ŭ�� ����
		Button connButton = new Button("�����ϱ�");
		connButton.setOnAction(even->{
			if(connButton.getText().equals("�����ϱ�")) {
				//�⺻������ ��Ʈ�� 9876���� �ΰ� ����ڰ� port��ȣ �Է��ϸ� �� ��ȣ�� �ٲ� �� �ֵ��� �س���
				int port = 9876;
				try {
					port = Integer.parseInt(portText.getText());    //������ �ٲٱ�
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				startClient(IPText.getText(),port);
				//UI���� �ʿ��� �� ���
				Platform.runLater(()->{
					textArea.appendText("[ ä�ù� ���� ]\n");
				});
				connButton.setText("�����ϱ�");
				input.setDisable(false);
				sendButton.setDisable(false);
				input.requestFocus();
			} else {
				stopClient();
				Platform.runLater(()->{
					textArea.appendText("[ ä�ù� ���� ]\n");
				});
				connButton.setText("�����ϱ�");
				input.setDisable(true);
				sendButton.setDisable(true);
			}
		});
		
		BorderPane pane = new BorderPane();
		pane.setLeft(connButton);
		pane.setCenter(input);
		pane.setRight(sendButton);
		
		root.setBottom(pane);
		chatScene = new Scene(root,400,400);
		window.setTitle("[ ä�� Ŭ���̾�Ʈ ]");
		window.setScene(chatScene);
		window.setOnCloseRequest(event-> stopClient());    //������ ����
		
		connButton.requestFocus();	    	
    } 
    
    //������ ���α׷��� ���۽�Ű�� �޼ҵ�
	@Override
	public void start(Stage primaryStage) {
		// Draw Scene
	      ChatScene();
	      LoginScene();
	      
	      window.show();
	}
	
	//���α׷��� ������
	public static void main(String[] args) {
		launch(args);
	}
}
