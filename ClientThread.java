//package CS3800FinalProject;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jason
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientThread implements Runnable {

    private Socket socket;
    private ChatServer server;
    private Message msg = null;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;

    public ClientThread(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    public ObjectOutputStream getOutputStream() {
        return this.out;
    }

    @Override
    public void run() {
        try {
            this.in = new ObjectInputStream(socket.getInputStream());
            this.out = new ObjectOutputStream(socket.getOutputStream());
            while (!socket.isClosed()) {
                System.out.println("reading in from: " + in);
                msg = (Message) in.readObject();
                clientAction(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void clientAction(Message msg) {
        Message toSender = null;
        Message toOthers = null;
        
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        
        switch(msg.getType()) {
            case Message.NEW_CONNECTION:
                toSender = new Message(Message.WELCOME, msg.getUser(), timestamp.getTime(), "Welcome " + msg.getUser().getName());
                server.getClientMap().put(msg.getUser().getUUID(), this);
                toOthers = new Message(Message.USER_JOINED, msg.getUser(), timestamp.getTime(), msg.getUser() + " has joined the chat");
                inform(msg,toSender, toOthers);
                break;
            case Message.BROADCAST_MSG:
                System.out.println("broadcasting message");
                toSender = new Message(Message.NEW_MESSAGE, msg.getUser(), timestamp.getTime(), msg.getPayLoad());
                toOthers = new Message(Message.NEW_MESSAGE, msg.getUser(), timestamp.getTime(), msg.getPayLoad());
                server.getMessageHistory().add(msg);
                toSender.setMessageHistory(server.getMessageHistory());
                toOthers.setMessageHistory(server.getMessageHistory());
                inform(msg, toSender, toOthers);
                break;
            case Message.CLOSE_CONNECTION: 
                toSender = new Message(Message.GOODBYE, msg.getUser(), timestamp.getTime(), "Goodbye"  + msg.getUser().getName());
                server.getClientMap().remove(msg.getUser().getUUID());
                toOthers = new Message(Message.USER_LEFT, msg.getUser(), timestamp.getTime(), msg.getUser() + " has left the chat");
                server.getMessageHistory().add(toOthers);
                toOthers.setMessageHistory(server.getMessageHistory());
                inform(msg, toSender, toOthers);
                break;
        }
    }

    public synchronized void inform(Message msg, Message clientMsg1, Message clientMsg2) {       
        server.getClientMap().forEach((uuid, clientThread) -> {
            System.out.println("client thread:\t" + clientThread);
            System.out.println("returned output stream:\t" + clientThread.getOutputStream());
            ObjectOutputStream clientOutput = clientThread.getOutputStream();
            System.out.println("output stream:\t" + clientOutput);
            try {
                if (uuid.equals(msg.getUser().getUUID())) {
                    System.out.println("sending message to broadcaster");
                    clientOutput.writeObject(clientMsg1);
                } else {
                    System.out.println("sending message to other");
                    clientOutput.writeObject(clientMsg2);
                }
                clientOutput.flush();
                clientOutput.reset();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }
    
    
    // I don't think we need this anymore since we have the method as syncrhonized
//    public void sortMessages(List<Message> msgList) {
//        Collections.sort(msgList, new Comparator<Message>() {
//             @Override
//             public int compare(Message o1, Message o2) {
//                 return Long.compare(((Message)o1).getTimestamp(), ((Message)o2).getTimestamp());
//             }
//         });
//    }
}
