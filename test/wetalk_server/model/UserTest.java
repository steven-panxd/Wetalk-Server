package wetalk_server.model;

import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserTest{
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static User user;

    @AfterAll
    public static void teardown() {
        DBUtil db = new DBUtil();
        db.delete("DELETE FROM USER WHERE USERNAME = " + UserTest.USERNAME + " AND PASSWORD = " + UserTest.PASSWORD);
    }

    @Test
    @Order(1)
    public void testRegister() {
        User.register(UserTest.USERNAME, UserTest.PASSWORD);
        User user = User.getUserByUsername(UserTest.USERNAME);
        // if signed up successfully, then we should be able to query a user, otherwise we will get a null value
        Assertions.assertNotNull(user);
        UserTest.user = user;
    }

    @Test
    @Order(2)
    public void testLogin() {
        User user = User.login(UserTest.USERNAME, UserTest.PASSWORD);
        Assertions.assertNotNull(user);
        Assertions.assertEquals(UserTest.user, user);
    }

    @Test
    @Order(3)
    public void testGetUserByID() {
        User user = User.getUserByID(UserTest.user.getID());
        Assertions.assertEquals(UserTest.user, user);
    }

    @Test
    @Order(3)
    public void testGetUserByUsername() {
        User user = User.getUserByUsername(UserTest.user.getUsername());
        Assertions.assertEquals(UserTest.user, user);

    }
}
