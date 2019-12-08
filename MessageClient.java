//package CS3800FinalProject;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;
import java.sql.Timestamp;

import javax.swing.*;


/**
 * MessageClient
 */
public class MessageClient implements ActionListener {

    private JTextField messageField;
    private JTextArea messageHistory;
    private boolean clientActive;
    private Socket connection;  
    private ObjectInputStream fromServer;
    private ObjectOutputStream toServer;
    private User user;
    private Message msg;

    public MessageClient(){
        JFrame frame = new JFrame("Messenger");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        messageField = new JTextField(30);
        messageField.addActionListener(this);
        messageField.setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
        frame.add(messageField, BorderLayout.SOUTH);

        messageHistory = new JTextArea("");
        messageHistory.setLineWrap(true);
        messageHistory.setEditable(false);
        frame.add(messageHistory, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        JMenu action = new JMenu("Action");
        action.setMnemonic('a');
        menuBar.add(action);

        JMenuItem seeUsers = new JMenuItem("See Current Messengers");
        seeUsers.setMnemonic('s');
        action.add(seeUsers);
        
        action.addSeparator();
        
        JMenuItem exit = new JMenuItem("Exit");
        exit.setMnemonic('x');
        action.add(exit);

        JMenu help = new JMenu("Help");
        help.setMnemonic('h');
        menuBar.add(help);

        JMenuItem viewHelp = new JMenuItem("View Help");
        viewHelp.setMnemonic('h');
        help.add(viewHelp);

        JMenuItem about = new JMenuItem("About");
        about.setMnemonic('a');
        help.add(about);


        frame.setJMenuBar(menuBar);        
        frame.setVisible(true);
        String name = null;
        while(name == null){
            name = JOptionPane.showInputDialog(frame, "Please enter your user name", "Username");
        }

        try{
            this.user = new User(String.valueOf(UUID.randomUUID()), name);
            System.out.println("UUID: " + this.user.getUUID());
            this.connection = new Socket("localhost", 5000);
            this.toServer = new ObjectOutputStream(this.connection.getOutputStream());
            this.fromServer = new ObjectInputStream(this.connection.getInputStream());

            this.msg = new Message(Message.NEW_CONNECTION, this.user, 69, "Connection request");
            this.toServer.writeObject(this.msg);
            this.toServer.flush();
            this.toServer.reset();

        } catch(Exception e){
            e.printStackTrace();
        }
    }
    public void actionPerformed(ActionEvent ae){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        try{
        if(messageField.getText().equals(".")){
            this.msg.setType(Message.CLOSE_CONNECTION);
            //this.msg.setTimestamp(timestamp);
            this.msg.setPayLoad("Requesting disconnect.");
            this.toServer.writeObject(this.msg);
            this.toServer.flush();
            this.toServer.reset();
            System.out.println("client exited");
            System.exit(0);
        }
        else{
            this.msg.setType(Message.BROADCAST_MSG);
            //this.msg.setTimestamp(timestamp);
            this.msg.setPayLoad(messageField.getText());
            this.toServer.writeObject(this.msg);
            this.toServer.flush();
            this.toServer.reset();

            messageField.setText("");
        }
        }catch(Exception ex){
        
        }
    }
    /**
     * @return the fromServer
     */
    public ObjectInputStream getFromServer() {
        return fromServer;
    }
    /**
     * @return the connection
     */
    public Socket getConnection() {
        return connection;
    }
    /**
     * @return the messageHistory
     */
    public JTextArea getMessageHistory() {
        return messageHistory;
    }
    public JTextField getMessageField() {
        return messageField;
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                //Socket connection = new Socket("localhost", 8000);
                MessageClient client = new MessageClient();
                ServerInput input = new ServerInput(client);
                Thread inputThread = new Thread(input);
                inputThread.start();
            }
        });
    }
}
class ServerInput implements Runnable {
    private ObjectInputStream fromServer;
    private MessageClient client;

    public ServerInput(MessageClient client){
        this.client = client;
        try{
            fromServer = client.getFromServer();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void run(){
        while(!client.getMessageField().getText().equals(".")){
            try{
                System.out.println("reading from server");
                Message msg = (Message) fromServer.readObject();
                System.out.println("read in from server");
                switch(msg.getType()) {
                    case Message.WELCOME:
                    case Message.USER_JOINED:
                    case Message.USER_LEFT:
                        System.out.println("user info message");
                        this.client.getMessageHistory().append(msg.getTimestamp() + " " + 
                                                                msg.getPayLoad() + "\n");
                        break;
                    case Message.NEW_MESSAGE:
                        System.out.println("general message received");
                        this.client.getMessageHistory().append(msg.getTimestamp() + " " +
                                                                 ": " + 
                                                                msg.getPayLoad() + "\n");
                        break;
                    case Message.GOODBYE:
                        System.exit(0);
                        break;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}