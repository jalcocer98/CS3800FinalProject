/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CS3800FinalProject;

/**
 *
 * @author Jason
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientThread implements Runnable {

    private Socket socket;
    private ChatServer server;
    private String uuid = null;
    private Message msg = null;
    private static ObjectInputStream in = null;
    private static ObjectOutputStream out = null;

    public ClientThread(ChatServer server, String uuid, Socket socket) {
        this.uuid = uuid;
        this.server = server;
        this.socket = socket;
    }

    private ObjectOutputStream getOutputStream() {
        return out;
    }

    @Override
    public void run() {
        try {
            in = new ObjectInputStream(socket.getInputStream());
            while (!socket.isClosed()) {
                msg = (Message) in.readObject();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void clientAction(Message msg) {
        Message clientMsg1 = null;
        Message clientMsg2 = null;
        
        switch(msg.getType()) {
            case Message.NEW_CONNECTION:
                clientMsg1 = new Message(Message.WELCOME, "Welcome " + msg.getUser().getName());
                server.getClientMap().put(msg.getUser().getUUID(), this);
                clientMsg2 = new Message(Message.USER_JOINED, msg.getUser() + " has joined the chat");
                inform(msg,clientMsg1, clientMsg2);
                break;
            case Message.BROADCAST_MSG:
                clientMsg2 = new Message(Message.NEW_MESSAGE, msg.getPayLoad());
                inform(msg, clientMsg1, clientMsg2);
                break;
            case Message.CLOSE_CONNECTION: 
                clientMsg1 = new Message(Message.GOODBYE, "Goodbye"  + msg.getUser().getName());
                clientMsg2 = new Message(Message.USER_JOINED, msg.getUser() + " has left the chat");
                inform(msg, clientMsg1, clientMsg2);
                server.getClientMap().remove(msg.getUser().getUUID());
                break;
        }
    }

    public void inform(Message msg, Message clientMsg1, Message clientMsg2) {       
        server.getClientMap().forEach((uuid, clientThread) -> {
            ObjectOutputStream clientOutput = clientThread.getOutputStream();

            try {
                if (uuid.equals(msg.getUser().getUUID()) && clientMsg1 != null) {
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
    }
}
