/*
 * 클라이언트는 스레드를 여러개 돌릴 필요가 없기 때문에 스레드풀 노필요 
 * 서버로 메시지를 전송하는 스레드 한개, 메시지를 전달받는 스레드 한개가 필요
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
	
	//클라이언트 프로그램 동작 메소드
	public void startClient(String IP, int port) {
		//스레드풀 필요하지 않기 때문에 Runnable아닌 Thread사용
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP,port);
					receive();
				} catch(Exception e) {
					if(!socket.isClosed()) {
						stopClient();
						System.out.println("[서버 접속 실패]");
						Platform.exit();   //프로그램 종료
					}
				}
			}
		};
		thread.start();  //스레드 실행
	}
	
	//클라이언트 프로그램 종료 메소드
	public void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//서버로부터 메시지를 전달받는 메소드
	//메세지를 계속 받을 수 있도록 while루프 사용
    public void receive() {
		while(true) {
			try {
				InputStream in = socket.getInputStream();
				byte[] buffer = new byte[512];
				int length = in.read(buffer);
				if(length == -1) throw new IOException();   //length가 -1이면 IOException발생
				String message = new String(buffer,0,length,"UTF-8");
				Platform.runLater(()->{
					textArea.appendText(message);   //textArea는 GUI요소중 하나로 화면에 메세지 출력해줌
				});
			} catch(Exception e) {
				stopClient();
				break;
			}
		}
	}
    
    //서버로 메시지를 전송하는 메소드
    public void send(String message) {
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();   //메세지 전송 끝을 알림
				} catch(Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}
    
    //로그인 씬
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
    
    // 채팅씬
    public void ChatScene() {
    	window = new Stage();
    	
    	BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		HBox hbox = new HBox();    //Border위에 하나의 레이아웃 더 줌
		hbox.setSpacing(5);
		
		//UserName text
		TextField userName = new TextField();
		userName.setPrefWidth(150);
		userName.setPromptText("닉네임을 입력하세요.");
		HBox.setHgrow(userName, Priority.ALWAYS);
		
		//IP,port text
		TextField IPText = new TextField("127.0.0.1");
		TextField portText = new TextField("9876");
		portText.setPrefWidth(80);
		
		hbox.getChildren().addAll(userName,IPText,portText);  //textBox에 실질적으로 textField추가
		root.setTop(hbox);   //hbox를 젤 위에둠
		
		textArea = new TextArea();  //초기화 시켜줘야한대 
		textArea.setEditable(false);   //수정 불가
		root.setCenter(textArea);     //센터에는 TextArea (top에는 hbox)
		
		//입력창
		TextField input = new TextField();
		input.setPrefWidth(Double.MAX_VALUE);
		input.setDisable(true);      //비활성화
		
		//처음 send (이거 안하면 처음 send가 안됨)
		input.setOnAction(event->{
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");  //전송칸 비워주고
			input.requestFocus();  //다시 메세지 보낼 수 있게 focus여기따 맞춤
		});
		
		//보내기 버튼
		Button sendButton = new Button("보내기");
		sendButton.setDisable(true);
		
		sendButton.setOnAction(event->{
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");  //전송칸 비워주고
			input.requestFocus();  //다시 메세지 보낼 수 있게 focus를 입력창에 맞춤
		});
		
		//연결 버튼
		//connButton 누르면 클라 시작
		Button connButton = new Button("접속하기");
		connButton.setOnAction(even->{
			if(connButton.getText().equals("접속하기")) {
				//기본적으로 포트는 9876으로 두고 사용자가 port번호 입력하면 그 번호로 바뀔 수 있도록 해놓음
				int port = 9876;
				try {
					port = Integer.parseInt(portText.getText());    //정수로 바꾸기
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				startClient(IPText.getText(),port);
				//UI변경 필요할 때 사용
				Platform.runLater(()->{
					textArea.appendText("[ 채팅방 접속 ]\n");
				});
				connButton.setText("종료하기");
				input.setDisable(false);
				sendButton.setDisable(false);
				input.requestFocus();
			} else {
				stopClient();
				Platform.runLater(()->{
					textArea.appendText("[ 채팅방 퇴장 ]\n");
				});
				connButton.setText("접속하기");
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
		window.setTitle("[ 채팅 클라이언트 ]");
		window.setScene(chatScene);
		window.setOnCloseRequest(event-> stopClient());    //닫으면 실행
		
		connButton.requestFocus();	    	
    } 
    
    //실제로 프로그램을 동작시키는 메소드
	@Override
	public void start(Stage primaryStage) {
		// Draw Scene
	      ChatScene();
	      LoginScene();
	      
	      window.show();
	}
	
	//프로그램의 진입점
	public static void main(String[] args) {
		launch(args);
	}
}
