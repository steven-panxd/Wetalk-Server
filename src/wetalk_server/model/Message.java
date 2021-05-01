package wetalk_server.model;

import com.google.gson.annotations.Expose;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Message {
    @Expose
    private int id;
    @Expose
    private int senderID;
    @Expose
    private int receiverID;
    @Expose
    private String content;
    @Expose
    private Long sendTimeStamp;

    // constructor for own class use
    private Message(int id, int senderID, int receiverID, String content, Long sendTimeStamp) {
        this.id = id;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.content = content;
        this.sendTimeStamp = sendTimeStamp;
    }

    // constructor for other class use
    public Message(int senderID, int receiverID, String content, Long sendTimeStamp) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.content = content;
        this.sendTimeStamp = sendTimeStamp;
    }

    public void send() {
        DBUtil db = new DBUtil();
        this.id = db.insert("INSERT INTO MESSAGE (SENDER_ID,RECEIVER_ID,CONTENT,SEND_TIME_STAMP)" +
                " VALUES ('" + this.senderID + "','" + this.receiverID + "','" + this.content + "'," + this.sendTimeStamp + ");");
        db.close();
    }

    public static Message getMessageByID(int id) {
        DBUtil db = new DBUtil();
        ResultSet rs = db.select("SELECT ROWID,* FROM MESSAGE WHERE ROWID = " + id);

        Message message = null;
        try {
            if (rs != null && rs.next()) {
                message = new Message(rs.getInt("rowid"), rs.getInt("sender_id"), rs.getInt("receiver_id"), rs.getString("content"), rs.getLong("send_time_stamp"));
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

        return message;
    }

    public int getSenderID() {
        return senderID;
    }

    public int getReceiverID() {
        return receiverID;
    }

    public Long getSendTimeStamp() {
        return sendTimeStamp;
    }

    public String getContent() {
        return content;
    }

    public int getID() {
        return id;
    }
}
