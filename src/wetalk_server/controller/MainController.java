package wetalk_server.controller;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import wetalk_server.utils.Cache;
import wetalk_server.utils.Global;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MainController {
    public void mainLoop() {
        // Init program exit listener
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Cache.getInstance().close();
            }
        });

        Server server = new Server();
        server.start();

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

        // waiting for connection
        server.addListener(new Listener(){
            private InetSocketAddress remoteAddressTcp;

            @Override
            public void connected(Connection connection) {
                this.remoteAddressTcp = connection.getRemoteAddressTCP();
                System.out.println(remoteAddressTcp.getAddress() + ":" + remoteAddressTcp.getPort() + " connected.");
            }

            public void received (Connection connection, Object request) {
                if(request instanceof FrameworkMessage.KeepAlive) {
                    // do nothing for keep alive request
                } else {
                    // spawn a new thread for each new connection from the client
                    new Thread(new MyClient(connection, (String) request)).start();
                }
            }

            public void disconnected(Connection connection) {
                System.out.println(remoteAddressTcp.getAddress() + ":" + remoteAddressTcp.getPort() + " disconnected.");
                connection.close();
            }
        });
    }
}
