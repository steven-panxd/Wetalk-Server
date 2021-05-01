package wetalk_server.controller;

import com.esotericsoftware.kryonet.Connection;
import com.google.gson.reflect.TypeToken;
import wetalk_server.utils.Global;
import wetalk_server.utils.Json;

import java.util.HashMap;


// spawn a new thread for each connection
public class MyClient extends Thread {
    private final Connection conn;
    private final String request;

    public MyClient(Connection conn, String request) {
       this.conn = conn;
       this.request = request;
    }

    public void sendDataToClient(String message) {
        conn.sendTCP(message);
    }

    @Override
    public void run() {
        String[] fields = request.split(":", -1);
        if(fields[0].equals(Global.getInstance().getProperty("registerPrefix"))) {  // data = "<register>:username:password"
            UserController.register(this, fields[1], fields[2]);
        } else if (fields[0].equals(Global.getInstance().getProperty("loginPrefix"))) {  // data = "<login>:username:password"
            UserController.login(this, fields[1], fields[2]);
        } else if (fields[0].equals(Global.getInstance().getProperty("sendMessagePrefix"))) {  // data = "<sendMessage>:accessToken:receiverID:content"
            UserController.sendMessage(this, fields[1], fields[2], fields[3], fields[4]);
        } else if (fields[0].equals(Global.getInstance().getProperty("getFriendListPrefix"))) {  // data = "<getFriendList>:accessToken"
            UserController.getFriendList(this, fields[1]);
        } else if (fields[0].equals(Global.getInstance().getProperty("addFriendPrefix"))) {
            UserController.addFriend(this, fields[1], fields[2]);
        } else if (fields[0].equals(Global.getInstance().getProperty("getLatestMessagePrefix"))) {
            UserController.getLatestMessage(this, fields[1]);
        } else if (fields[0].equals(Global.getInstance().getProperty("acceptAddFriendPrefix"))) {
            UserController.acceptAddFriend(this, fields[1], fields[2]);
        } else if (fields[0].equals(Global.getInstance().getProperty("rejectAddFriendPrefix"))) {
            UserController.rejectAddFriend(this, fields[1], fields[2]);
        } else {
            HashMap<String, String> objRequest = Json.getInstance().fromJson(request, new TypeToken<HashMap<String,String>>(){}.getType());
            if(objRequest.get("operation").equals(Global.getInstance().getProperty("logoutPrefix"))) {
                UserController.logout(this, objRequest.get("accessToken"));
            } else {
                System.out.println(request);
                this.sendDataToClient(Global.getInstance().getProperty("failPrefix") + ":" + "Server Router Error! (" + request + ")");
            }
        }
    }
}
