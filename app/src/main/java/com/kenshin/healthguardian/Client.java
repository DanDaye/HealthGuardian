package com.kenshin.healthguardian;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Created by lenovo on 17/6/4.
 */

public class Client {
    private Socket socket;
    private OutputStream os;
    private InputStream is;
    private PrintStream ps;
    private BufferedReader reader;
    public Client(String hostname,int port) throws IOException {
        socket = new Socket(hostname,port);
        os = socket.getOutputStream();
        is = socket.getInputStream();
        ps = new PrintStream(os);
        reader = new BufferedReader(new InputStreamReader(is));
    }
    //发送报文并返回响应
    public void send(String content){
        ps.print(content);
    }
    public String receive(){
        String response;
        try {
            if((response = reader.readLine()) != null ){
                return response;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
