package wetalk_server.controller;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.google.gson.reflect.TypeToken;
import wetalk_server.model.User;
import wetalk_server.utils.Cache;
import wetalk_server.utils.Global;
import wetalk_server.utils.Json;
import wetalk_server.utils.Token;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * Main controller of the project
 */
public class MainController {
    /**
     * Main loop of the main controller
     * Start the server and waiting for connection/requests from clients
     * If received a request, send it to router
     */
    public void mainLoop() {
        Server server = new Server() {
            protected Connection newConnection () {
                // By providing our own connection implementation, we can store per
                // connection state without a connection ID to state look up.
                return new ChatConnection();
            }
        };


        // Init program exit listener
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Cache.getInstance().close();
                server.close();
            }
        });

        // waiting for connection
        server.addListener(new Listener(){

            @Override
            public void connected(Connection c) {
                ChatConnection connection = (ChatConnection) c;
                connection.address = connection.getRemoteAddressTCP().getHostString();
                connection.port = connection.getRemoteAddressTCP().getPort();
                System.out.println(connection.address + ":" + connection.port + " connected.");
            }

            public void received (Connection c, Object request) {
                ChatConnection connection = (ChatConnection) c;
                if(request instanceof String) {
                    HashMap<String, String> mapRequest = Json.getInstance().fromJson((String) request, new TypeToken<HashMap<String, String>>(){}.getType());
                    String accessToken = mapRequest.getOrDefault("accessToken", null);
                    User user = connection.user;
                    // if access token exists, verify access token
                    if(user == null && accessToken != null) {
                        user = Token.getInstance().verifyUserToken(accessToken);
                        // if invalid access token, terminate the request
                        if(user == null) {
                            HashMap<String, String> mapResponse = UserController.getGenericFailResponse("Invalid access token.");
                            String data = Json.getInstance().toJson(mapResponse);
                            connection.sendData(data);
                            return;
                        }
                        // else, set user to current login-ed user
                        connection.user = user;
                    }
                    connection.requestName = mapRequest.get("request");
                    Router.castRequest(connection, mapRequest);
                }
            }

            public void disconnected(Connection c) {
                ChatConnection connection = (ChatConnection) c;
                if(connection.user != null) {
                    connection.user.logout();
                }
                System.out.println(connection.address + ":" + connection.port + " disconnected.");
            }
        });

        // bind address and port
        String address = Global.getInstance().getProperty("bindAddress");
        int port = Integer.parseInt(Global.getInstance().getProperty("bindPort"));
        InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);
        try {
            server.bind(inetSocketAddress, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server started, listening on " + address + ":" + port);

        server.start();
    }

    /**
     * Customized connection that can store some important information
     */
    static class ChatConnection extends Connection {
        public String address;
        public int port;
        public String requestName;
        public User user;

        public void sendData(String data) {
            this.sendTCP(data);
        }
    }
}
