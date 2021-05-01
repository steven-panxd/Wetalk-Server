package wetalk_server.utils;

import wetalk_server.model.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

import java.util.Date;

public class Token {
    private static final Token instance = new Token();
    private Algorithm algorithm;

    private Token() {
        this.algorithm = Algorithm.HMAC256(Global.getInstance().getProperty("secretKey"));
    }


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

    public User verifyUserToken(String token) {
        int userID = Integer.parseInt(JWT.decode(token).getIssuer());
        return User.getUserByID(userID);
    }

    public static Token getInstance() {
        return Token.instance;
    }
}
