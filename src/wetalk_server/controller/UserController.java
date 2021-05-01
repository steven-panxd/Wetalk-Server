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

    public static void register(MyClient client, String username, String password) {
        User.register(username, password);
        client.sendDataToClient("<Success>:");
    }

    public static void login(MyClient client, String username, String password) {
        User user = User.login(username, password);
        String cacheKey = Global.getInstance().getProperty("loginUsersPrefix");
//        HashMap<String, String> mapData = new HashMap<>();
//        mapData.put("callback", Global.getInstance().getProperty("loginFinishedPrefix"));

        if(user == null) {
            client.sendDataToClient(Global.getInstance().getProperty("failPrefix") + ":Username or password is wrong");
        } else if (Cache.getInstance().sIsMember(cacheKey, String.valueOf(user.getID()))) {
            client.sendDataToClient(Global.getInstance().getProperty("failPrefix") + ":You've already login in another client");
        } else {
            String accessToken = Token.getInstance().getUserToken(String.valueOf(user.getID()));
            Cache.getInstance().sAdd(cacheKey, String.valueOf(user.getID()));
            client.sendDataToClient(Global.getInstance().getProperty("succeedPrefix") + ":" + accessToken + ":" + user.getID() + ":" + user.getUsername());
        }
    }

    public static void logout(MyClient client, String accessToken) {
        User user = Token.getInstance().verifyUserToken(accessToken);
        String cacheKey = Global.getInstance().getProperty("loginUsersPrefix");
        Cache.getInstance().sRem(cacheKey, String.valueOf(user.getID()));
    }

    public static void sendMessage(MyClient client, String accessToken, String receiverID, String content, String sendTimeStamp) {
        User user = Token.getInstance().verifyUserToken(accessToken);
        Message sentMessage = user.sendMessage(content, Integer.parseInt(receiverID), Long.parseLong(sendTimeStamp));
        String cacheKey = Global.getInstance().getProperty("newMessageCachePrefix") + receiverID;
        Cache.getInstance().push(cacheKey, String.valueOf(sentMessage.getID()));
        client.sendDataToClient(Global.getInstance().getProperty("succeedPrefix"));
    }

    public static void addFriend(MyClient client, String accessToken, String friendUsername) {
        User user = Token.getInstance().verifyUserToken(accessToken);
        User friend = User.getUserByUsername(friendUsername);

        HashMap<String, String> mapData = new HashMap<>();

        if(friend == null) {
            mapData.put("status", Global.getInstance().getProperty("failPrefix"));
            mapData.put("data",  "Username does not exist");
        } else if (user.getID() == friend.getID()) {
            mapData.put("status", Global.getInstance().getProperty("failPrefix"));
            mapData.put("data", "You can not add yourself.");
        } else if (user.isFriendOf(friend)) {
            mapData.put("status", Global.getInstance().getProperty("failPrefix"));
            mapData.put("data", "You were friends.");
        } else if (user.hasSentFriendRequest(friend)) {
            mapData.put("status", Global.getInstance().getProperty("failPrefix"));
            mapData.put("data", "You have already sent the request, please wait for confirmation!");
        } else {
            mapData.put("status", Global.getInstance().getProperty("succeedPrefix"));
            user.addFriend(friend);
            String cacheName = Global.getInstance().getProperty("newAddFriendRequestPrefix") + friend.getID();
            Cache.getInstance().push(cacheName, String.valueOf(user.getID()));
        }

        String data = Json.getInstance().toJson(mapData);
        client.sendDataToClient(data);
    }

    public static void getFriendList(MyClient client, String accessToken) {
        User user = Token.getInstance().verifyUserToken(accessToken);
        ArrayList<User> friendList = user.getFriendsList(0);
        // sample response data = "<Success>:1,username1:2,username2"
        String data = Global.getInstance().getProperty("succeedPrefix");
        for (User friend : friendList) {
            data += ":";
            data += friend.getID();
            data += ",";
            data += friend.getUsername();
        }
        client.sendDataToClient(data);
    }

    public static void getLatestMessage(MyClient client, String accessToken) {
        User user = Token.getInstance().verifyUserToken(accessToken);

        String messageKey = Global.getInstance().getProperty("newMessageCachePrefix") + user.getID();
        ArrayList<Message> newMessages = new ArrayList<>();
        List<String> messageIDs = Cache.getInstance().popAll(messageKey);
        for(String messageID: messageIDs) {
            Message message = Message.getMessageByID(Integer.parseInt(messageID));
            newMessages.add(message);
        }

        String addFriendRequestKey = Global.getInstance().getProperty("newAddFriendRequestPrefix") + user.getID();
        ArrayList<User> addFriendRequesters = new ArrayList<>();
        List<String> addFriendRequesterIDs = Cache.getInstance().popAll(addFriendRequestKey);
        for(String addFriendRequesterID : addFriendRequesterIDs) {
            User addFriendRequester = User.getUserByID(Integer.parseInt(addFriendRequesterID));
            addFriendRequesters.add(addFriendRequester);
        }

        String acceptedUserKey = Global.getInstance().getProperty("newAcceptFriendPrefix") + user.getID();
        ArrayList<User> acceptedUsers = new ArrayList<>();
        List<String> acceptedUserIDs = Cache.getInstance().popAll(acceptedUserKey);
        for(String acceptedUserID : acceptedUserIDs) {
            User acceptedUser = User.getUserByID(Integer.parseInt(acceptedUserID));
            acceptedUsers.add(acceptedUser);
        }

        String rejectedUserKey = Global.getInstance().getProperty("newRejectFriendPrefix") + user.getID();
        ArrayList<User> rejectedUsers = new ArrayList<>();
        List<String> rejectedUserIDs = Cache.getInstance().popAll(rejectedUserKey);
        for(String requestUserID : rejectedUserIDs) {
            User rejectedUser = User.getUserByID(Integer.parseInt(requestUserID));
            rejectedUsers.add(rejectedUser);
        }

        HashMap<String, String> data = new HashMap<>();
        data.put("status", Global.getInstance().getProperty("succeedPrefix"));
        data.put("newMessages", Json.getInstance().toJson(newMessages));
        data.put("addFriendRequesters", Json.getInstance().toJson(addFriendRequesters));
        data.put("acceptedUsers", Json.getInstance().toJson(acceptedUsers));
        data.put("rejectedUsers", Json.getInstance().toJson(rejectedUsers));
        String responseData = Json.getInstance().toJson(data);
        System.out.println(responseData);
        client.sendDataToClient(responseData);
    }

    public static void acceptAddFriend(MyClient client, String accessToken, String friendID) {
        User user = Token.getInstance().verifyUserToken(accessToken);
        User acceptedUser = User.getUserByID(Integer.parseInt(friendID));
        user.acceptFriend(acceptedUser);
        String data = Global.getInstance().getProperty("succeedPrefix");
        client.sendDataToClient(data);
    }

    public static void rejectAddFriend(MyClient client, String accessToken, String friendID) {
        User user = Token.getInstance().verifyUserToken(accessToken);
        User rejectedUser = User.getUserByID(Integer.parseInt(friendID));
        user.rejectFriend(rejectedUser);
        String data = Global.getInstance().getProperty("succeedPrefix");
        client.sendDataToClient(data);
    }
}
