package com.kenshin.healthguardian.Model;

import org.litepal.crud.DataSupport;
/**
 * Created by Kenshin on 2017/3/13.
 */

public class Msg extends DataSupport{
    private String sender;
    private String receiver;
    private String message;

    public Msg(){
    }
    public Msg(String sender, String receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
