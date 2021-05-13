package wetalk_server.model;

import wetalk_server.utils.Cache;
import wetalk_server.utils.Global;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * Friend model
 * Describe the relationship of two users
 * If two users are friends, there will be two rows of data in the database which one of the
 */
public class Friend {
    /**
     * Get all friends from a user's id
     * @param userID User's id
     * @param limit Limitation of number returns
     * @param pageNum The page number of the limitation
     * @return Return a list of User model
     */
    public static ArrayList<User> getFriends(int userID, int limit, int pageNum) {
        DBUtil db = new DBUtil();
        ResultSet rs1 = db.select("SELECT * FROM FRIEND WHERE USER_ID = " + userID +";");
        DBUtil db2 = new DBUtil();
        ResultSet rs2 = db2.select("SELECT * FROM FRIEND WHERE FRIEND_ID = " + userID + ";");
        HashSet<Integer> hs1 = new HashSet<>();
        HashSet<Integer> hs2 = new HashSet<>();
        try {
            while (rs1 != null && rs1.next()) {
                hs1.add(rs1.getInt("FRIEND_ID"));
            }
            while (rs2 != null && rs2.next()) {
                hs2.add(rs2.getInt("USER_ID"));
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }

        ArrayList<User> friendList = new ArrayList<>();
        // if no friend, return empty
        if(hs1.isEmpty() || hs2.isEmpty()) {
            return friendList;
        }

        // conver ids to Users
        hs1.retainAll(hs2);
        for(int user_id : hs1) {
            User user = User.getUserByID(user_id);
            friendList.add(user);
        }
        Collections.sort(friendList);

        try {
            rs1.close();
            rs2.close();
            db.close();
            db2.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return friendList;
    }

    /**
     * Create a new relationship between two users (add friend)
     * @param userID User's id
     * @param friendID Friend's user id
     */
    public static void addFriend(int userID, int friendID) {
        DBUtil db = new DBUtil();
        db.insert("INSERT INTO FRIEND (USER_ID,FRIEND_ID)" +
                " VALUES ('" + userID + "','" + friendID + "');");
        db.close();
    }

    /**
     * Delete a relationship between two users (delete friend)
     * @param userID User's id
     * @param friendID Friend's user id
     */
    public static void deleteFriend(int userID, int friendID) {
        DBUtil db = new DBUtil();
        db.delete("DELETE FROM FRIEND WHERE USER_ID = " + userID + " AND FRIEND_ID = " + friendID);
        db.delete("DELETE FROM FRIEND WHERE FRIEND_ID = " + userID + " AND USER_ID = " + friendID);
        db.close();
    }

    /**
     * Create a new relationship between two users (accepted a friend request)
     * @param userID User's id
     * @param friendID Friend's user id
     */
    public static void acceptFriend(int userID, int friendID) {
        Friend.addFriend(userID, friendID);
        String acceptFriendCacheKey = Global.getInstance().getProperty("newAcceptFriendPrefix") + friendID;
        Cache.getInstance().push(acceptFriendCacheKey, String.valueOf(userID));
    }

    /**
     * Delete a relationship between two users (rejected a friend request)
     * @param userID User's id
     * @param friendID Friend's user id
     */
    public static void rejectFriend(int userID, int friendID) {
        Friend.deleteFriend(friendID, userID);
        String rejectFriendCacheKey = Global.getInstance().getProperty("newRejectFriendPrefix") + friendID;
        Cache.getInstance().push(rejectFriendCacheKey, String.valueOf(userID));
    }
}
