package wetalk_server.utils;

import wetalk_server.model.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

import java.util.Date;

/**
 * JWT generator class
 * The token store the information of a user
 */
public class Token {
    private static final Token instance = new Token();
    private Algorithm algorithm;

    /**
     * Constructor of Token class
     */
    private Token() {
        this.algorithm = Algorithm.HMAC256(Global.getInstance().getProperty("secretKey"));
    }

    /**
     * Generate a token from a user's id
     * @param userID String user id
     * @return Generated token
     */
    public String getUserToken(String userID) {
        String token = null;
        try{
            Date expireDate = new Date(System.currentTimeMillis() + (Integer.parseInt(Global.getInstance().getProperty("tokenExpireTimeInMinute"))) * 60 * 1000);
            token = JWT.create().withIssuer(userID).withExpiresAt(expireDate).sign(this.algorithm);
        } catch (JWTCreationException e) {
            e.printStackTrace();
        }
        return token;
    }

    /**
     * Verify token and return corresponding User models
     * @param token String token
     * @return A User model
     */
    public User verifyUserToken(String token) {
        int userID = Integer.parseInt(JWT.decode(token).getIssuer());
        return User.getUserByID(userID);
    }

    /**
     * returns the only one instance of Json
     * @return an instance of Json
     */
    public static Token getInstance() {
        return Token.instance;
    }
}
