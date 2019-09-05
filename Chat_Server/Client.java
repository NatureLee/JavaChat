/*
 * 한명의 클라이언트와 통신하기 위한 기능을 모아놓은 클래스
 */

package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteOrder;

import MsgPacker.*;
import javafx.application.Platform;

public class Client {
   
   Socket socket;
   Client me;
   private ChatRoom room;
   int roomNum;
   
   int i =0;
   
   MessagePacker msg;
   
   public Client(Socket sock) 
   {
      this.socket = sock;   //초기화
      me = this;
      msg = new MessagePacker();
      msg.SetEndianType(ByteOrder.BIG_ENDIAN);
      receive();//();
   }
   
   //클라이언로부터 메시지를 전달받는 메소드
   public void receive()//()
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
                  byte[] buffer = new byte[1025];
                  //byte[] reBuf = new byte[1025];
                  int length = in.read(buffer);
                  while(length == -1) throw new IOException();
                  System.out.println("[메시지 수신 성공]" 
                  + socket.getRemoteSocketAddress()
                  + ": " + Thread.currentThread().getName());
                  
                  System.out.println(buffer.length);
                  msg = new MessagePacker(buffer);
                  byte protocol = msg.getProtocol();
                                    
                  MessagePacker reMsg = new MessagePacker();
   
                  switch(protocol)
                  {
                  case MessageProtocol.LOGIN:{
                     //데베 맞으면
                     reMsg.SetProtocol(MessageProtocol.LOGIN_SUCC);
                     byte[] message = reMsg.Finish();
                        send(message);
                     break;
                   }
                  case MessageProtocol.ROOM1:{
                     //클라이언트 룸에 입장
                     RoomManager.rooms.get(0).enterRoom(me,0);
                     msg.SetProtocol(MessageProtocol.ROOMENTER);
                     byte[] message = msg.Finish();
                     send(message);
                     break;
                   }
                  case MessageProtocol.ROOM2:{
                     //클라이언트 룸에 입장
                     RoomManager.rooms.get(1).enterRoom(me,1);
                     reMsg.SetProtocol(MessageProtocol.ROOMENTER);
                     byte[] message = reMsg.Finish();
                     send(message);
                     break;
                   }
                  case MessageProtocol.ROOM3:{
                     //클라이언트 룸에 입장
                     RoomManager.rooms.get(2).enterRoom(me,2);
                     reMsg.SetProtocol(MessageProtocol.ROOMENTER);
                     byte[] message = reMsg.Finish();
                     for(Client client : RoomManager.rooms.get(2).getUserList())
                     {
                        client.send(message);
                     }
                     break;
                   }
                  case MessageProtocol.CHAT:{
                     MessagePacker chatMsg = new MessagePacker();
                     roomNum = room.getRoomNum();
                     System.out.println("CHAT_" + (roomNum + 1)+ "번방에 보냈습니다.");
                     chatMsg.SetProtocol(MessageProtocol.CHAT);
                     //chatMsg.add("hello\n");
                     chatMsg.add(msg.getString());

                     byte[] message = chatMsg.Finish();
                     for(Client client : RoomManager.rooms.get(roomNum).getUserList())
                     {
                        client.send(message);
                     }
                     break;
                   }

                  }
               }
            } catch(Exception e) {
               try {
                  System.out.println("[메시지 수신 오류]"
                  + socket.getRemoteSocketAddress()
                  + ": " + Thread.currentThread().getName() + e);
               }catch(Exception e2) {
                  e.printStackTrace();
               }
            }
         }
         
      };
      Main.threadPool.submit(thread);   // 스레드풀에 만들어진 스레드 등록
   }
   
   //클라이언트에게 메시지를 전송하는 메소드
   public void send(byte[] message)
   {
      Runnable thread = new Runnable() {

         @Override
         public void run() {
            try {
               OutputStream out = socket.getOutputStream();
               byte[] buffer = message;
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

    
    public void enterRoom(ChatRoom room) 
    {
      this.room = room;
      System.out.println((room.getRoomNum()+1)+"번방 입장");
   }

    public void exitRoom()
    {
        this.room = null;
    }

    public ChatRoom getRoom() {
        return room;
    }

    public void setRoom(ChatRoom room) {
        this.room = room;
    }

    public Socket getSock() {
        return socket;
    }

    public void setSock(Socket sock) {
        this.socket = sock;
    }
    
    public int getRoomNum() {
        return roomNum;
    }

    public void setRoomNum(int Num) {
        this.roomNum = Num;
    }
}