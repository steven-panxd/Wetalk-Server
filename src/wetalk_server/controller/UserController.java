package wetalk_server.controller;

import wetalk_server.model.Message;
import wetalk_server.model.User;
import wetalk_server.utils.Cache;
import wetalk_server.utils.Global;
import wetalk_server.utils.Json;
import wetalk_server.utils.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserController {

    public static HashMap<String, String> getGenericSucceedResponse(String data) {
        HashMap<String, String> response = new HashMap<>();
        response.put("status", Global.getInstance().getProperty("succeedPrefix"));
        response.put("data", data);
        return response;
    }

    public static HashMap<String, String> getGenericFailResponse(String message) {
        HashMap<String, String> response = new HashMap<>();
        HashMap<String, String> responseData = new HashMap<>();
        responseData.put("message", message);
        response.put("status", Global.getInstance().getProperty("failPrefix"));
        response.put("data", Json.getInstance().toJson(responseData));
        return response;
    }

    public static HashMap<String, String> register(MainController.ChatConnection conn, HashMap<String, String> requestData) {
        String username = requestData.getOrDefault("username", "");
        String password = requestData.getOrDefault("password", "");

        if(username.length() < 6 || username.length() > 32) {
            return UserController.getGenericFailResponse("Your username must be between 6-32 characters.");
        }

        if(password.length() < 6 || password.length() > 32) {
            return UserController.getGenericFailResponse("Your password must be between 6-32 characters.");
        }

        User user = User.getUserByUsername(username);
        if(user != null) {
            return UserController.getGenericFailResponse("The username has been taken, please use another one.");
        }

        User.register(username, password);
        HashMap<String, String> responseData = new HashMap<>();
        responseData.put("message", "Signed up successfully, please go back and sign in.");
        return UserController.getGenericSucceedResponse(Json.getInstance().toJson(responseData));
    }

    public static HashMap<String, String> login(MainController.ChatConnection conn, HashMap<String, String> requestData) {
        String username = requestData.get("username");
        String password = requestData.get("password");
        if(username == null || password == null) {
            return UserController.getGenericFailResponse("Please input username or password.");
        }

        User user = User.login(username, password);
        if(user == null) {
            return UserController.getGenericFailResponse("Username or password is wrong.");
        }

        String cacheKey = Global.getInstance().getProperty("loginUsersPrefix");
        if(Cache.getInstance().sIsMember(cacheKey, String.valueOf(user.getID()))) {
            return UserController.getGenericFailResponse("You've already signed in on another client.");
        }

        String accessToken = Token.getInstance().getUserToken(String.valueOf(user.getID()));
        Cache.getInstance().sAdd(cacheKey, String.valueOf(user.getID()));
        HashMap<String, String> responseData = new HashMap<>();
        responseData.put("accessToken", accessToken);
        responseData.put("id", String.valueOf(user.getID()));
        return UserController.getGenericSucceedResponse(Json.getInstance().toJson(responseData));
    }

    public static HashMap<String, String> logout(MainController.ChatConnection conn) {
        conn.user.logout();
        return UserController.getGenericSucceedResponse("");
    }

    public static HashMap<String, String> sendMessage(MainController.ChatConnection conn, HashMap<String, String> requestData) {
        String content = requestData.getOrDefault("content", "");
        int receiverID = Integer.parseInt(requestData.get("receiverID"));
        User receiver = User.getUserByID(receiverID);
        long sendTimeStamp = Long.parseLong(requestData.get("sendTime"));

        if (receiver == null) {
            return UserController.getGenericFailResponse("Receiver does not exist.");
        }

        if (!conn.user.isFriendOf(receiver)) {
            return UserController.getGenericFailResponse(receiver.getUsername() + " is not in your friend.");
        }

        Message sentMessage = conn.user.sendMessage(content, receiverID, sendTimeStamp);
        String cacheKey = Global.getInstance().getProperty("newMessageCachePrefix") + receiverID;
        Cache.getInstance().push(cacheKey, String.valueOf(sentMessage.getID()));
        return UserController.getGenericSucceedResponse("");
    }

    public static HashMap<String, String> addFriend(MainController.ChatConnection conn, HashMap<String, String> requestData) {
        String friendUsername = requestData.get("friendUsername");
        User friend = User.getUserByUsername(friendUsername);

        if(friend == null) {
            return UserController.getGenericFailResponse("User " + friendUsername + " does not exist.");
        } else if (conn.user.getID() == friend.getID()) {
            return UserController.getGenericFailResponse("You can not add yourself.");
        } else if (conn.user.isFriendOf(friend)) {
            return UserController.getGenericFailResponse("You were friends.");
        } else if (conn.user.hasSentFriendRequest(friend)) {
            return UserController.getGenericFailResponse("You have already sent the request, please wait for confirmation!");
        } else {
            conn.user.addFriend(friend);
            String cacheName = Global.getInstance().getProperty("newAddFriendRequestPrefix") + friend.getID();
            Cache.getInstance().push(cacheName, String.valueOf(conn.user.getID()));
            return UserController.getGenericSucceedResponse("Request sent, please wait for confirmation.");
        }
    }

    public static HashMap<String, String> getFriendList(MainController.ChatConnection conn) {
        ArrayList<User> friendList = conn.user.getFriendsList(0);
        HashMap<String, String> responseData = new HashMap<>();
        responseData.put("friendList", Json.getInstance().toJson(friendList));
        return UserController.getGenericSucceedResponse(Json.getInstance().toJson(responseData));
    }

    public static HashMap<String, String> getLatestData(MainController.ChatConnection conn) {
        String messageKey = Global.getInstance().getProperty("newMessageCachePrefix") + conn.user.getID();
        ArrayList<Message> newMessages = new ArrayList<>();
        List<String> messageIDs = Cache.getInstance().popAll(messageKey);
        for(String messageID: messageIDs) {
            Message message = Message.getMessageByID(Integer.parseInt(messageID));
            newMessages.add(message);
        }

        String addFriendRequestKey = Global.getInstance().getProperty("newAddFriendRequestPrefix") + conn.user.getID();
        ArrayList<User> addFriendRequesters = new ArrayList<>();
        List<String> addFriendRequesterIDs = Cache.getInstance().popAll(addFriendRequestKey);
        for(String addFriendRequesterID : addFriendRequesterIDs) {
            User addFriendRequester = User.getUserByID(Integer.parseInt(addFriendRequesterID));
            addFriendRequesters.add(addFriendRequester);
        }

        String acceptedUserKey = Global.getInstance().getProperty("newAcceptFriendPrefix") + conn.user.getID();
        ArrayList<User> acceptedUsers = new ArrayList<>();
        List<String> acceptedUserIDs = Cache.getInstance().popAll(acceptedUserKey);
        for(String acceptedUserID : acceptedUserIDs) {
            User acceptedUser = User.getUserByID(Integer.parseInt(acceptedUserID));
            acceptedUsers.add(acceptedUser);
        }

        String rejectedUserKey = Global.getInstance().getProperty("newRejectFriendPrefix") + conn.user.getID();
        ArrayList<User> rejectedUsers = new ArrayList<>();
        List<String> rejectedUserIDs = Cache.getInstance().popAll(rejectedUserKey);
        for(String rejectedUserID : rejectedUserIDs) {
            User rejectedUser = User.getUserByID(Integer.parseInt(rejectedUserID));
            rejectedUsers.add(rejectedUser);
        }

        String deletedUserKey = Global.getInstance().getProperty("newDeleteFriendPrefix") + conn.user.getID();
        ArrayList<User> deletedUsers = new ArrayList<>();
        List<String> deletedUserIDs = Cache.getInstance().popAll(deletedUserKey);
        for(String deletedUserID : deletedUserIDs) {
            User deletedUser = User.getUserByID(Integer.parseInt(deletedUserID));
            deletedUsers.add(deletedUser);
        }

        HashMap<String, String> responseData = new HashMap<>();
        responseData.put("newMessages", Json.getInstance().toJson(newMessages));
        responseData.put("addFriendRequesters", Json.getInstance().toJson(addFriendRequesters));
        responseData.put("acceptedUsers", Json.getInstance().toJson(acceptedUsers));
        responseData.put("rejectedUsers", Json.getInstance().toJson(rejectedUsers));
        responseData.put("deletedUsers", Json.getInstance().toJson(deletedUsers));
        return UserController.getGenericSucceedResponse(Json.getInstance().toJson(responseData));
    }

    public static HashMap<String, String> acceptFriend(MainController.ChatConnection conn, HashMap<String, String> requestData) {
        int acceptedFriendID = Integer.parseInt(requestData.get("acceptedFriendID"));
        User acceptedUser = User.getUserByID(acceptedFriendID);
        conn.user.acceptFriend(acceptedUser);
        return UserController.getGenericSucceedResponse("");
    }

    public static HashMap<String, String> rejectFriend(MainController.ChatConnection conn, HashMap<String, String> requestData) {
        int acceptedFriendID = Integer.parseInt(requestData.get("rejectedFriendID"));
        User rejectedUser = User.getUserByID(acceptedFriendID);
        conn.user.rejectFriend(rejectedUser);
        return UserController.getGenericSucceedResponse("");
    }

    public static HashMap<String, String> deleteFriend(MainController.ChatConnection conn, HashMap<String, String> requestData) {
        int deletedFriendID = Integer.parseInt(requestData.get("deletedFriendID"));
        User deletedFriend = User.getUserByID(deletedFriendID);
        if(!conn.user.isFriendOf(deletedFriend)) {
            return UserController.getGenericFailResponse("You are not friends.");
        }
        conn.user.deleteFriend(deletedFriend);
        String cacheKey = Global.getInstance().getProperty("newDeleteFriendPrefix") + deletedFriendID;
        Cache.getInstance().push(cacheKey, String.valueOf(conn.user.getID()));
        return UserController.getGenericSucceedResponse("");
    }
}
