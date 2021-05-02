package wetalk_server.model;

import com.google.gson.annotations.Expose;
import wetalk_server.utils.Cache;
import wetalk_server.utils.Global;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

//done
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

	public int getID() {
		return this.id;
	}

	public String getUsername() {
		return this.username;
	}

	public void setPassword(String newPassword) {
		DBUtil db = new DBUtil();
		db.update("UPDATE USER SET PASSWORD = '" + newPassword + "' WHERE USERNAME = '" + this.username + "';");
		db.close();
		this.password = newPassword;
	}

	public void addFriend(User friend) {
		Friend.addFriend(this.id, friend.getID());
	}

	public void deleteFriend(User friend) {
		Friend.deleteFriend(this.id, friend.getID());
	}

	public ArrayList<User> getFriendsList(int pageNum) {
		// TODO: pagination
		return Friend.getFriends(this.id, Integer.parseInt(Global.getInstance().getProperty("numPerPage")), pageNum);
	}

	// return User object id if login, otherwise return 0
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

	// return true if success, otherwise false.
	public static void register(String username, String password) {
		DBUtil db = new DBUtil();
		db.insert("INSERT INTO USER (USERNAME, PASSWORD) VALUES ('" + username + "', '" + password + "')");
		db.close();
	}

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

	public boolean isFriendOf(User anotherUser) {
		return this.getFriendsList(0).contains(anotherUser);
	}

	public boolean hasSentFriendRequest(User anotherUser) {
		String key = Global.getInstance().getProperty("newAddFriendRequestPrefix") + anotherUser.getID();
		return Cache.getInstance().contains(key, String.valueOf(this.id));
	}

	public Message sendMessage(String content, int receiverID, Long sendTimeStamp) {
		Message message = new Message(this.id, receiverID, content, sendTimeStamp);
		message.send();
		return message;
	}

	public void acceptFriend(User acceptedUser) {
		Friend.acceptFriend(this.id, acceptedUser.getID());
	}

	public void rejectFriend(User rejectedUser) {
		Friend.rejectFriend(this.id, rejectedUser.getID());
	}

	public String toString() {
		return "<User: " + this.id + ">";
	}

	// compare only by it's id
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

    public void logout() {
		String cacheKey = Global.getInstance().getProperty("loginUsersPrefix");
		Cache.getInstance().sRem(cacheKey, String.valueOf(this.id));
    }
}
