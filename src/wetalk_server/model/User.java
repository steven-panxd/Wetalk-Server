package wetalk_server.model;

import com.google.gson.annotations.Expose;
import wetalk_server.utils.Cache;
import wetalk_server.utils.Global;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * User model
 * Describe a user
 */
public class User implements Comparable<User> {
	@Expose
	private final int id;
	@Expose
	private final String username;
	private String password;

	// constructor for own class use
	private User(int id, String username, String password) {
		this.id = id;
		this.username = username;
		this.password = password;
	}

	/**
	 * Getter of user's id
	 * @return user's id
	 */
	public int getID() {
		return this.id;
	}

	/**
	 * Getter of user's username
	 * @return user's username
	 */
	public String getUsername() {
		return this.username;
	}

	/**
	 * Modify current user's password
	 * @param newPassword New password
	 */
	public void setPassword(String newPassword) {
		DBUtil db = new DBUtil();
		db.update("UPDATE USER SET PASSWORD = '" + newPassword + "' WHERE USERNAME = '" + this.username + "';");
		db.close();
		this.password = newPassword;
	}

	/**
	 * Add a friend
	 * @param friend User model of another user
	 */
	public void addFriend(User friend) {
		Friend.addFriend(this.id, friend.getID());
	}

	/**
	 * Delete a friend
	 * @param friend User model of another user
	 */
	public void deleteFriend(User friend) {
		Friend.deleteFriend(this.id, friend.getID());
	}

	/**
	 * Get latest friend list
	 * @param pageNum Page number
	 */
	public ArrayList<User> getFriendsList(int pageNum) {
		// TODO: pagination
		return Friend.getFriends(this.id, Integer.parseInt(Global.getInstance().getProperty("numPerPage")), pageNum);
	}

	/**
	 * Login and store current user to logined user set in Cache
	 * @param username String username
	 * @param password String password
	 * @return Instance of the user
	 */
	public static User login(String username, String password) {
		DBUtil db = new DBUtil();

		ResultSet rs = db.select("SELECT rowid, * FROM USER WHERE USERNAME = '" + username + "' AND PASSWORD = '" + password + "'");

		User user = null;

		try {
			if (rs.next()) {
				user = new User(rs.getInt("rowid"), rs.getString("username"), rs.getString("password"));
			}
		} catch (SQLException e) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}

		try{
			rs.close();
			db.close();
		}catch (SQLException e) {
			e.printStackTrace();
		}

		return user;
	}

	/**
	 * Register
	 * @param username String username
	 * @param password String password
	 */
	public static void register(String username, String password) {
		DBUtil db = new DBUtil();
		db.insert("INSERT INTO USER (USERNAME, PASSWORD) VALUES ('" + username + "', '" + password + "')");
		db.close();
	}

	/**
	 * Get a user by a user's id
	 * @param id User's id
	 * @return Instance of User model
	 */
	public static User getUserByID(int id) {
		DBUtil db = new DBUtil();
		ResultSet rs = db.select("SELECT rowid, * FROM USER WHERE ROWID = " + id);

		User user = null;
		try {
			if (rs != null && rs.next()) {
				user = new User(rs.getInt("rowid"), rs.getString("username"), rs.getString("password"));
			}
		} catch (SQLException e) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );

		} finally {
			try {
				if(rs != null) {
					rs.close();
				}
				db.close();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}

		return user;
	}

	/**
	 * Get user by the username
	 * @param username String username
	 * @return Instance of User model
	 */
	public static User getUserByUsername(String username) {
		DBUtil db = new DBUtil();
		ResultSet rs = db.select("SELECT rowid, * FROM USER WHERE USERNAME = '" + username + "';");

		User user = null;
		try {
			if (rs != null && rs.next()) {
				user = new User(rs.getInt("rowid"), rs.getString("username"), rs.getString("password"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			rs.close();
			db.close();
		}catch (Exception e) {
			e.printStackTrace();
		}

		return user;
	}

	/**
	 * Check if current user is friend of another user
	 * @param anotherUser User model of another user
	 * @return Boolean flag of the result
	 */
	public boolean isFriendOf(User anotherUser) {
		return this.getFriendsList(0).contains(anotherUser);
	}

	/**
	 * Check if current user has sent a add friend request to another user
	 * @param anotherUser User model of another user
	 * @return Boolean flag of the result
	 */
	public boolean hasSentFriendRequest(User anotherUser) {
		String key = Global.getInstance().getProperty("newAddFriendRequestPrefix") + anotherUser.getID();
		return Cache.getInstance().contains(key, String.valueOf(this.id));
	}

	/**
	 * Send message to another user
	 * @param content String content of the message
	 * @param receiverID Receiver's user id
	 * @param sendTimeStamp	Send timestamp
	 * @return The Message model of the message just sent
	 */
	public Message sendMessage(String content, int receiverID, Long sendTimeStamp) {
		Message message = new Message(this.id, receiverID, content, sendTimeStamp);
		message.send();
		return message;
	}

	/**
	 * Accept a add friend request
	 * @param acceptedUser User model of accepted user
	 */
	public void acceptFriend(User acceptedUser) {
		Friend.acceptFriend(this.id, acceptedUser.getID());
	}

	/**
	 * Reject a add friend request
	 * @param rejectedUser User model of rejected user
	 */
	public void rejectFriend(User rejectedUser) {
		Friend.rejectFriend(this.id, rejectedUser.getID());
	}

	public String toString() {
		return "<User: " + this.id + ">";
	}

	/**
	 * If two user's ids are same, they are the same user
	 * @param o Another instance of User model class
	 * @return Boolean flag of the result
	 */
	@Override
	public int compareTo(User o) {
		return this.id - o.getID();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		User user = (User) o;
		return id == user.id && username.equals(user.username) && password.equals(user.password);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, username, password);
	}

	/**
	 * Logout and remove current user from the logined user set
	 */
    public void logout() {
		String cacheKey = Global.getInstance().getProperty("loginUsersPrefix");
		Cache.getInstance().sRem(cacheKey, String.valueOf(this.id));
    }
}
