/*
 * �Ѹ��� Ŭ���̾�Ʈ�� ����ϱ� ���� ����� ��Ƴ��� Ŭ����
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
      this.socket = sock;   //�ʱ�ȭ
      me = this;
      msg = new MessagePacker();
      msg.SetEndianType(ByteOrder.BIG_ENDIAN);
      receive();//();
   }
   
   //Ŭ���̾�κ��� �޽����� ���޹޴� �޼ҵ�
   public void receive()//()
   {
      // �ϳ��� ������ ���� �� Runnable��ü ���� �̿�
      Runnable thread = new Runnable() 
      {
         // Runnable �ȿ� run�Լ� �־����
         // run�Լ��� �ϳ��� �����尡 � ���μ� �����Ұ����� ����
         @Override
         public void run() {
            try {
               while(true) {
                  InputStream in = socket.getInputStream();
                  byte[] buffer = new byte[1025];
                  //byte[] reBuf = new byte[1025];
                  int length = in.read(buffer);
                  while(length == -1) throw new IOException();
                  System.out.println("[�޽��� ���� ����]" 
                  + socket.getRemoteSocketAddress()
                  + ": " + Thread.currentThread().getName());
                  
                  System.out.println(buffer.length);
                  msg = new MessagePacker(buffer);
                  byte protocol = msg.getProtocol();
                                    
                  MessagePacker reMsg = new MessagePacker();
   
                  switch(protocol)
                  {
                  case MessageProtocol.LOGIN:{
                     //���� ������
                     reMsg.SetProtocol(MessageProtocol.LOGIN_SUCC);
                     byte[] message = reMsg.Finish();
                        send(message);
                     break;
                   }
                  case MessageProtocol.ROOM1:{
                     //Ŭ���̾�Ʈ �뿡 ����
                     RoomManager.rooms.get(0).enterRoom(me,0);
                     msg.SetProtocol(MessageProtocol.ROOMENTER);
                     byte[] message = msg.Finish();
                     send(message);
                     break;
                   }
                  case MessageProtocol.ROOM2:{
                     //Ŭ���̾�Ʈ �뿡 ����
                     RoomManager.rooms.get(1).enterRoom(me,1);
                     reMsg.SetProtocol(MessageProtocol.ROOMENTER);
                     byte[] message = reMsg.Finish();
                     send(message);
                     break;
                   }
                  case MessageProtocol.ROOM3:{
                     //Ŭ���̾�Ʈ �뿡 ����
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
                     System.out.println("CHAT_" + (roomNum + 1)+ "���濡 ���½��ϴ�.");
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
                  System.out.println("[�޽��� ���� ����]"
                  + socket.getRemoteSocketAddress()
                  + ": " + Thread.currentThread().getName() + e);
               }catch(Exception e2) {
                  e.printStackTrace();
               }
            }
         }
         
      };
      Main.threadPool.submit(thread);   // ������Ǯ�� ������� ������ ���
   }
   
   //Ŭ���̾�Ʈ���� �޽����� �����ϴ� �޼ҵ�
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
                  System.out.println("[�޽��� �۽� ����]"
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
      System.out.println((room.getRoomNum()+1)+"���� ����");
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