package wetalk_server.model;

import com.google.gson.annotations.Expose;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Message model
 * Describe a message sent from a user to another user
 */
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

    private Message(int id, int senderID, int receiverID, String content, Long sendTimeStamp) {
        this.id = id;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.content = content;
        this.sendTimeStamp = sendTimeStamp;
    }

    /**
     * Constructor of Message model
     * @param senderID sender's user id
     * @param receiverID receiver's user id
     * @param content content of the message
     * @param sendTimeStamp timestamp of send time
     */
    public Message(int senderID, int receiverID, String content, Long sendTimeStamp) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.content = content;
        this.sendTimeStamp = sendTimeStamp;
    }

    /**
     * Send the message
     */
    public void send() {
        DBUtil db = new DBUtil();
        this.id = db.insert("INSERT INTO MESSAGE (SENDER_ID,RECEIVER_ID,CONTENT,SEND_TIME_STAMP)" +
                " VALUES ('" + this.senderID + "','" + this.receiverID + "','" + this.content + "'," + this.sendTimeStamp + ");");
        db.close();
    }

    /**
     * Get and return an instance of a Message model by message's id
     * @param id Message's id
     * @return An instance of a Message model
     */
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

    /**
     * Getter of sender's user id
     * @return Sender's user id
     */
    public int getSenderID() {
        return senderID;
    }

    /**
     * Getter of receiver's user id
     * @return Receiver's user id
     */
    public int getReceiverID() {
        return receiverID;
    }

    /**
     * Getter of send timestamp
     * @return Send timestamp
     */
    public Long getSendTimeStamp() {
        return sendTimeStamp;
    }

    /**
     * Getter of the content
     * @return String content
     */
    public String getContent() {
        return content;
    }

    /**
     * Getter of id
     * @return current message's id
     */
    public int getID() {
        return id;
    }
}
