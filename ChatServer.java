/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CS3800FinalProject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author Jason
 */
public class ChatServer {

    private static final int portNumber = 5000;

    private int serverPort;
    private Map<String, ClientThread> clientMap;
//    private List<ClientThread> clients;

    public static void main(String[] args){
        ChatServer server = new ChatServer(portNumber);
        server.startServer();
    }

    public ChatServer(int portNumber){
        this.serverPort = portNumber;
    }

//    public List<ClientThread> getClients(){
//        return clients;
//    }
    
    public Map<String, ClientThread> getClientMap(){
        return clientMap;
    }

    private void startServer(){
//        clients = new ArrayList<ClientThread>();
        clientMap = new HashMap();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(serverPort);
            acceptClients(serverSocket);
        } catch (IOException e){
            System.err.println("Could not listen on port: "+serverPort);
            System.exit(1);
        }
    }

    private void acceptClients(ServerSocket serverSocket){
        while(true){
            try {
                Socket socket = serverSocket.accept();
                System.out.println("accepts : " + socket.getRemoteSocketAddress());
                UUID clientUUID = UUID.randomUUID();
                ClientThread client = new ClientThread(this, clientUUID.toString(), socket);
                Thread thread = new Thread(client);
                thread.start();
//                clients.add(client);
//                clientMap.put(clientUUID.toString(),client);
                
            } catch (IOException ex){
                System.out.println("Accept failed on : "+serverPort);
            }
        }
    }
}