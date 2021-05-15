package wetalk_server.model;

import org.junit.jupiter.api.*;

import java.util.ArrayList;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FriendTest {
    private static final String USERNAME1 = "username1";
    private static final String PASSWORD1 = "password1";
    private static final String USERNAME2 = "username2";
    private static final String PASSWORD2 = "password2";
    private static User user1;
    private static User user2;

    @BeforeAll
    public static void init() {
        User.register(FriendTest.USERNAME1, FriendTest.PASSWORD1);
        User.register(FriendTest.USERNAME2, FriendTest.PASSWORD2);
        FriendTest.user1 = User.login(FriendTest.USERNAME1, FriendTest.PASSWORD1);
        FriendTest.user2 = User.login(FriendTest.USERNAME2, FriendTest.PASSWORD2);
    }

    @AfterAll
    public static void teardown() {
        DBUtil db = new DBUtil();
        db.delete("DELETE FROM USER WHERE USERNAME = \"" + FriendTest.USERNAME1 + "\" AND PASSWORD = \"" + FriendTest.PASSWORD1 + "\"");
        db.delete("DELETE FROM USER WHERE USERNAME = \"" + FriendTest.USERNAME2 + "\" AND PASSWORD = \"" + FriendTest.PASSWORD2 + "\"");
    }


    @Test
    @Order(1)
    public void testAddFriend() {
        Assertions.assertFalse(user1.isFriendOf(user2));
        Assertions.assertFalse(user2.isFriendOf(user1));
        Friend.addFriend(user1.getID(), user2.getID());
        Friend.addFriend(user2.getID(), user1.getID());
        Assertions.assertTrue(user1.isFriendOf(user2));
        Assertions.assertTrue(user2.isFriendOf(user1));
    }

    @Test
    @Order(2)
    public void testGetFriendList() {
        ArrayList<User> friends =  Friend.getFriends(user1.getID(), 0, 0);
        Assertions.assertTrue(friends.contains(user2));
    }

    @Test
    @Order(3)
    public void testDeleteFriend() {
        Assertions.assertTrue(user1.isFriendOf(user2));
        Assertions.assertTrue(user2.isFriendOf(user1));
        Friend.deleteFriend(user1.getID(), user2.getID());
        Friend.deleteFriend(user2.getID(), user1.getID());
        Assertions.assertFalse(user1.isFriendOf(user2));
        Assertions.assertFalse(user2.isFriendOf(user1));
    }
}
