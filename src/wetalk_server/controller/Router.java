package wetalk_server.controller;

import com.google.gson.reflect.TypeToken;
import wetalk_server.utils.Global;
import wetalk_server.utils.Json;

import java.util.HashMap;

public class Router {
    public static void castRequest(MainController.ChatConnection connection, HashMap<String, String> requestData) {
        String jsonData = requestData.getOrDefault("data", null);
        HashMap<String, String> mapData = null;
        if(jsonData != null) {
            mapData = Json.getInstance().fromJson(jsonData, new TypeToken<HashMap<String, String>>() {}.getType());
        }

        HashMap<String, String> mapResponse;
        if(connection.requestName.equals(Global.getInstance().getProperty("getLatestDataPrefix"))) {
            mapResponse = UserController.getLatestData(connection);
        } else if(connection.requestName.equals(Global.getInstance().getProperty("loginPrefix"))) {
            mapResponse = UserController.login(connection, mapData);
        } else if(connection.requestName.equals(Global.getInstance().getProperty("registerPrefix"))) {
            mapResponse = UserController.register(connection, mapData);
        } else if(connection.requestName.equals(Global.getInstance().getProperty("logoutPrefix"))) {
            mapResponse= UserController.logout(connection);
        } else if(connection.requestName.equals(Global.getInstance().getProperty("sendMessagePrefix"))) {
            mapResponse = UserController.sendMessage(connection, mapData);
        } else if(connection.requestName.equals(Global.getInstance().getProperty("addFriendPrefix"))) {
            mapResponse = UserController.addFriend(connection, mapData);
        } else if(connection.requestName.equals(Global.getInstance().getProperty("getFriendListPrefix"))) {
            mapResponse = UserController.getFriendList(connection);
        } else if(connection.requestName.equals(Global.getInstance().getProperty("acceptFriendPrefix"))) {
            mapResponse = UserController.acceptFriend(connection, mapData);
        } else if(connection.requestName.equals(Global.getInstance().getProperty("rejectFriendPrefix"))) {
            mapResponse = UserController.rejectFriend(connection, mapData);
        } else if(connection.requestName.equals(Global.getInstance().getProperty("deleteFriendPrefix"))) {
            mapResponse = UserController.deleteFriend(connection, mapData);
        } else {
            HashMap<String, String> mapResponseData = new HashMap<>();
            mapResponseData.put("message", "Unsupported request: " + connection.requestName);
            mapResponse = new HashMap<>();
            mapResponse.put("status", Global.getInstance().getProperty("failPrefix"));
            mapResponse.put("data", Json.getInstance().toJson(mapResponseData));
        }
        mapResponse.put("response", connection.requestName);
        String data = Json.getInstance().toJson(mapResponse);
        connection.sendData(data);
    }
}
