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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;    

public class ClientThread implements Runnable {

    private Socket socket;
    private ChatServer server;
    private Message msg = null;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    private boolean killThread = false;

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
            while (!this.killThread) {
                msg = (Message) in.readObject();
                clientAction(msg);
            }
            this.in.close();
            this.out.close();
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void clientAction(Message msg) {
        Message toSender = null;
        Message toOthers = null;
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy @ hh:mm a");  
        LocalDateTime timestamp;
        String timestampString;
        //Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        
        switch(msg.getType()) {
            case Message.NEW_CONNECTION:
                timestamp = LocalDateTime.now();
                timestampString = dtf.format(timestamp);
                msg.setTimestamp(timestampString);
                toSender = new Message(Message.WELCOME, msg.getUser(), timestampString, "Welcome " + msg.getUser().getName());
                toSender.setMessageHistory(server.getMessageHistory());
                server.getClientMap().put(msg.getUser().getUUID(), this);
                toOthers = new Message(Message.USER_JOINED, msg.getUser(), timestampString, msg.getUser().getName() + " has joined the chat");
                inform(msg,toSender, toOthers);
                server.getMessageHistory().add(toOthers);
                break;
            case Message.BROADCAST_MSG:
                timestamp = LocalDateTime.now();
                timestampString = dtf.format(timestamp);
                msg.setTimestamp(timestampString);
                toSender = new Message(Message.NEW_MESSAGE, msg.getUser(), timestampString, msg.getPayLoad());
                toOthers = new Message(Message.NEW_MESSAGE, msg.getUser(), timestampString, msg.getPayLoad());
                server.getMessageHistory().add(toOthers);
                toSender.setMessageHistory(server.getMessageHistory());
                toOthers.setMessageHistory(server.getMessageHistory());
                inform(msg, toSender, toOthers);
                break;
            case Message.CLOSE_CONNECTION: 
                this.killThread = true;
                timestamp = LocalDateTime.now();
                timestampString = dtf.format(timestamp);
                msg.setTimestamp(timestampString);
                toSender = new Message(Message.GOODBYE, msg.getUser(), timestampString, "Goodbye"  + msg.getUser().getName());
                //server.getClientMap().remove(msg.getUser().getUUID());
                toOthers = new Message(Message.USER_LEFT, msg.getUser(), timestampString, msg.getUser().getName() + " has left the chat");
                server.getMessageHistory().add(toOthers);
                toOthers.setMessageHistory(server.getMessageHistory());
                inform(msg, toSender, toOthers);
                break;
        }
    }

    public synchronized void inform(Message msg, Message clientMsg1, Message clientMsg2) {       
        server.getClientMap().forEach((uuid, clientThread) -> {
            ObjectOutputStream clientOutput = clientThread.getOutputStream();
            try {
                if (uuid.equals(msg.getUser().getUUID())) {
                    clientOutput.writeObject(clientMsg1);
                } else {
                    clientOutput.writeObject(clientMsg2);
                }
                clientOutput.flush();
                clientOutput.reset();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        if(this.killThread){
            server.getClientMap().remove(msg.getUser().getUUID());
        }
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
