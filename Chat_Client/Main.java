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
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

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
    	GridPane logGrid = new GridPane();
    	logGrid.setAlignment(Pos.CENTER);
    	logGrid.setVgap(10);
    	logGrid.setHgap(10);
    	logGrid.setPadding(new Insets(10));
    	
    	Text topTxt = new Text("Login");
    	topTxt.setFont(Font.font("���� ���",FontWeight.LIGHT,30));
    	logGrid.add(topTxt, 0, 0);   	
        
    	// id
    	Label idLb = new Label("ID");
    	TextField idField = new TextField();
    	idField.setPromptText("ID");
      	logGrid.add(idLb, 0, 3);
    	logGrid.add(idField, 0, 4);
    	
    	//pw
    	Label pwLb = new Label("Password");
    	PasswordField pwField = new PasswordField();
    	pwField.setPromptText("Password");
      	logGrid.add(pwLb, 0, 5);
    	logGrid.add(pwField, 0, 6);
    	
    	// buttons
    	BorderPane btnPane = new BorderPane();
   
    	Button logBtn = new Button("Login");
      	Button signBtn = new Button("Sign Up");
      	btnPane.setLeft(logBtn);
      	btnPane.setCenter(signBtn);      	
      	
    	logGrid.add(btnPane, 0, 8);;
    	
    	logBtn.setOnAction(event->{
    		window.setScene(chatScene);
    	});
    	
    	signBtn.setOnAction(event->{
    		window.setScene(signupScene);
    	});
    	
        logScene = new Scene(logGrid, 760, 480);
        window.setScene(logScene);
    }

    //ȸ������ ��
    public void SignUpScene() {
    	
    	window = new Stage();
    	GridPane suGrid = new GridPane();
    	suGrid.setAlignment(Pos.CENTER);
    	suGrid.setVgap(10);
    	suGrid.setHgap(10);
    	suGrid.setPadding(new Insets(10));
    	
    	Text topTxt = new Text("Sign Up");
    	topTxt.setFont(Font.font("���� ���",FontWeight.LIGHT,30));
    	suGrid.add(topTxt, 0, 0);   	
        
    	//id
    	Label idLb = new Label("ID ����");
    	TextField idField = new TextField();
    	idField.setPromptText("ID");
    	suGrid.add(idLb, 0, 3);
    	suGrid.add(idField, 0, 4);
    	
    	//pw
    	Label pwLb = new Label("Password ����");
    	TextField pwField = new TextField();
    	pwField.setPromptText("Password");
    	suGrid.add(pwLb, 0, 5);
    	suGrid.add(pwField, 0, 6);
    	
    	// buttons
    	BorderPane btnPane = new BorderPane();
    	   
    	Button backBtn = new Button("Back");
      	Button signBtn = new Button("Sign Up");
      	btnPane.setCenter(signBtn);
      	btnPane.setRight(backBtn);      	
      	
      	suGrid.add(btnPane, 0, 8);
    
    	backBtn.setOnAction(event->{
    		window.setScene(logScene);
    	});
    	
        signupScene = new Scene(suGrid, 760, 480);
        window.setScene(signupScene);
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
		Text nick = new Text("NickName : ");
		userName.setPrefWidth(150);
		userName.setPromptText("�г����� �Է��ϼ���.");
		HBox.setHgrow(userName, Priority.ALWAYS);
		
		//IP,port text
	  /*TextField IPText = new TextField("127.0.0.1");
		TextField portText = new TextField("9876");
		portText.setPrefWidth(80);*/
		
		hbox.getChildren().addAll(nick,userName);//,IPText,portText);  //textBox�� ���������� textField�߰�
		root.setTop(hbox);   //hbox�� �� ������
		
		textArea = new TextArea();  //�ʱ�ȭ ��������Ѵ� 
		textArea.setEditable(false);   //���� �Ұ�
		root.setCenter(textArea);     //���Ϳ��� TextArea (top���� hbox)
		
		//�Է�â
		TextField input = new TextField();
		input.setPrefWidth(Double.MAX_VALUE);
		//input.setDisable(true);      //��Ȱ��ȭ
		
		//ó�� send (�̰� ���ϸ� ó�� send�� �ȵ�)
		input.setOnAction(event->{
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");  //����ĭ ����ְ�
			input.requestFocus();  //�ٽ� �޼��� ���� �� �ְ� focus����� ����
		});
		
		//������ ��ư
		Button sendButton = new Button("������");
		//sendButton.setDisable(true);
		
		sendButton.setOnAction(event->{
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");  //����ĭ ����ְ�
			input.requestFocus();  //�ٽ� �޼��� ���� �� �ְ� focus�� �Է�â�� ����
		});
		
		
		BorderPane pane = new BorderPane();
		pane.setCenter(input);
		pane.setRight(sendButton);
		
		root.setBottom(pane);
		chatScene = new Scene(root,400,400);
		window.setTitle("[ ä�� Ŭ���̾�Ʈ ]");
		window.setScene(chatScene);
		window.setOnCloseRequest(event-> stopClient());    //������ ����
		
		input.requestFocus();
    } 
    
    //������ ���α׷��� ���۽�Ű�� �޼ҵ�
	@Override
	public void start(Stage primaryStage) {
                        startClient(new String("127.0.0.1"),9876);  //�������ڸ��� ���� ����
		// Draw Scene
	      ChatScene();
                  SignUpScene();
	      LoginScene();
	      
	      window.show();
	}
	
	//���α׷��� ������
	public static void main(String[] args) {
		launch(args);
	}
}
