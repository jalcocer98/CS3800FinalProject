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
public class User implements Serializable {
    private String uuid = null;
    private String name = null;
    
    public User(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }
    
    public String getUUID(){
        return uuid;
    }
    
    public String getName(){
        return name;
    }
}
