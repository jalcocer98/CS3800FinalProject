/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CS3800FinalProject;

import java.io.Serializable;

/**
 *
 * @author Jason
 */
public class Message implements Serializable {

    public static final int NEW_CONNECTION = 0;
    public static final int BROADCAST_MSG = 1;
    public static final int CLOSE_CONNECTION = 2;
    public static final int WELCOME = 3;
    public static final int USER_JOINED = 4;
    public static final int NEW_MESSAGE = 5;
    public static final int GOODBYE = 6;
    public static final int USER_LEFT = 7;

    private int type;
    private User user = null;
    private String payload = null;

    public Message(int type, String payload) {
        this(type, null, payload);
    }

    public Message(int type, User user, String payload) {
        this.type = type;
        this.user = user;
        this.payload = payload;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPayLoad() {
        return this.payload;
    }

    public void setPayLoad(String payload) {
        this.payload = payload;
    }
}
