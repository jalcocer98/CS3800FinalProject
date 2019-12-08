//package CS3800FinalProject;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;


/**
 * MessageClient
 */
public class MessageClient implements ActionListener {

    private JTextField messageField;
    private JTextArea messageHistory;
    private Socket connection;  
    private ObjectInputStream fromServer;
    private ObjectOutputStream toServer;
    private User user;
    private Message msg;

    public MessageClient(){
        JFrame frame = new JFrame("Messenger");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        messageField = new JTextField(30);
        messageField.addActionListener(this);
        //messageField.setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
        frame.add(messageField, BorderLayout.SOUTH);

        messageHistory = new JTextArea("");
        messageHistory.setLineWrap(true);
        messageHistory.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageHistory);
        frame.add(scrollPane, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        JMenu action = new JMenu("Action");
        action.setMnemonic('a');
        menuBar.add(action);

        // JMenuItem seeUsers = new JMenuItem("See Current Users");
        // seeUsers.setMnemonic('s');
        // action.add(seeUsers);
        
        action.addSeparator();
        
        JMenuItem exit = new JMenuItem("Exit");
        exit.setMnemonic('x');
        exit.addActionListener(ae -> {
            try{
            this.msg.setType(Message.CLOSE_CONNECTION);
                        this.msg.setPayLoad("Requesting disconnect.");
                        this.toServer.writeObject(this.msg);
                        this.toServer.flush();
                        this.toServer.reset();
            }catch(IOException e){
                e.printStackTrace();
            }
		});
        action.add(exit);

        JMenu help = new JMenu("Help");
        help.setMnemonic('h');
        menuBar.add(help);

        JMenuItem viewHelp = new JMenuItem("View Help");
        viewHelp.setMnemonic('h');
        viewHelp.addActionListener(ae -> {
			JOptionPane.showMessageDialog(frame, "Enter a message into the message field and press "
											+ "enter to send it to all other users in the chat. Enter just . to exit the chat and close the program", "Help",
											JOptionPane.INFORMATION_MESSAGE);
		});
        help.add(viewHelp);

        JMenuItem about = new JMenuItem("About");
        about.setMnemonic('a');
        about.addActionListener(ae -> {
			JOptionPane.showMessageDialog(frame, "Chat system project using TCP sockets for our CS3800 course", "About", JOptionPane.INFORMATION_MESSAGE);
		});
        help.add(about);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(frame, 
                    "Are you sure you want to leave the chat and exit the program?", "Leave Chat?", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    
                    try{
                        msg.setType(Message.CLOSE_CONNECTION);
                        msg.setPayLoad("Requesting disconnect.");
                        toServer.writeObject(msg);
                        toServer.flush();
                        toServer.reset();
                        
                        // clientActive = false;
                        // fromServer.close();
                        // toServer.close();
                        // connection.close();
                        // System.exit(0);
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        });

        frame.setJMenuBar(menuBar);        
        frame.setVisible(true);
        
        String name = null;
        name = JOptionPane.showInputDialog(frame, "Please enter your user name", "Username");

        if(name == null){
            System.exit(0);
        }

        try{
            this.user = new User(String.valueOf(UUID.randomUUID()), name);
            this.connection = new Socket("localhost", 5000);
            this.toServer = new ObjectOutputStream(this.connection.getOutputStream());
            this.fromServer = new ObjectInputStream(this.connection.getInputStream());

            this.msg = new Message(Message.NEW_CONNECTION, this.user, "", "Connection request");
            this.toServer.writeObject(this.msg);
            this.toServer.flush();
            this.toServer.reset();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    public void actionPerformed(ActionEvent ae){
        try{
        if(messageField.getText().trim().equals("")){

        }
        else if(messageField.getText().equals(".")){
            this.msg.setType(Message.CLOSE_CONNECTION);
            this.msg.setPayLoad("Requesting disconnect.");
            this.toServer.writeObject(this.msg);
            this.toServer.flush();
            this.toServer.reset();

            // this.clientActive = false;
            // this.fromServer.close();
            // this.toServer.close();
            // this.connection.close();
            //System.exit(0);
        }
        else{
            this.msg.setType(Message.BROADCAST_MSG);
            this.msg.setPayLoad(messageField.getText().trim());
            this.toServer.writeObject(this.msg);
            this.toServer.flush();
            this.toServer.reset();

            messageField.setText("");
        }
        }catch(Exception ex){
        
        }
    }
    public ObjectInputStream getFromServer() {
        return fromServer;
    }
    public ObjectOutputStream getToServer() {
        return toServer;
    }
    public Socket getConnection() {
        return connection;
    }
    public JTextArea getMessageHistory() {
        return messageHistory;
    }
    public JTextField getMessageField() {
        return messageField;
    }
    public void setMessageFieldValue(String value){
        this.messageField.setText(value);
    }
    public void killClient(){
        System.exit(0);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
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
                Message msg = (Message) fromServer.readObject();
                switch(msg.getType()) {
                    case Message.WELCOME:
                        List<Message> history = msg.getMessageHistory();
                        Iterator<Message> iterator = history.iterator();
                        while(iterator.hasNext()){
                            Message historyMsg = iterator.next();
                            if(historyMsg.getType() == Message.NEW_MESSAGE){
                                this.client.getMessageHistory().append(historyMsg.getTimestamp() + " " +
                                                                    historyMsg.getUser().getName() + ": " + 
                                                                    historyMsg.getPayLoad() + "\n");
                            }
                            else{
                                this.client.getMessageHistory().append(historyMsg.getTimestamp() + " " + 
                                historyMsg.getPayLoad() + "\n");
                            }
                        }
                        this.client.getMessageHistory().append(msg.getTimestamp() + " " + 
                                                                msg.getPayLoad() + "\n");
                        break;
                    case Message.USER_JOINED:
                    case Message.USER_LEFT:
                        this.client.getMessageHistory().append(msg.getTimestamp() + " " + 
                                                                msg.getPayLoad() + "\n");
                        break;
                    case Message.NEW_MESSAGE:
                        this.client.getMessageHistory().append(msg.getTimestamp() + " " +
                                                                msg.getUser().getName() + ": " + 
                                                                msg.getPayLoad() + "\n");
                        break;
                    case Message.GOODBYE:
                        this.client.setMessageFieldValue(".");
                        this.client.getFromServer().close();
                        this.client.getToServer().close();
                        this.client.getConnection().close();
                        break;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        this.client.killClient();
    }
}