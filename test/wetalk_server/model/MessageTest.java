package wetalk_server.model;

import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MessageTest {
    private static final String USERNAME1 = "username1";
    private static final String PASSWORD1 = "password1";
    private static final String USERNAME2 = "username2";
    private static final String PASSWORD2 = "password2";
    private static final String CONTENT = "content";
    private static final Long SENDTIMESTAMP = System.currentTimeMillis();
    private static User user1;
    private static User user2;

    @BeforeAll
    public static void init() {
        User.register(MessageTest.USERNAME1, MessageTest.PASSWORD1);
        User.register(MessageTest.USERNAME2, MessageTest.PASSWORD2);
        MessageTest.user1 = User.login(MessageTest.USERNAME1, MessageTest.PASSWORD1);
        MessageTest.user2 = User.login(MessageTest.USERNAME2, MessageTest.PASSWORD2);
    }

    @AfterAll
    public static void teardown() {
        DBUtil db = new DBUtil();
        db.delete("DELETE FROM USER WHERE USERNAME = \"" + MessageTest.USERNAME1 + "\" AND PASSWORD = \"" + MessageTest.PASSWORD1 + "\"");
        db.delete("DELETE FROM USER WHERE USERNAME = \"" + MessageTest.USERNAME2 + "\" AND PASSWORD = \"" + MessageTest.PASSWORD2 + "\"");
        db.delete("DELETE FROM MESSAGE WHERE SENDER_ID = " + MessageTest.user1.getID() + " AND RECEIVER_ID = " + MessageTest.user2.getID() + " AND SEND_TIME_STAMP = " + MessageTest.SENDTIMESTAMP);
    }

    @Test
    @Order(1)
    public void testSendMessage() {
        Message message = new Message(user1.getID(), user2.getID(), MessageTest.CONTENT, MessageTest.SENDTIMESTAMP);
        Assertions.assertDoesNotThrow(message::send);
    }
}
