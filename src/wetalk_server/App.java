package wetalk_server;

import wetalk_server.controller.MainController;

/**
 * Entrance of the project
 */
public class App {
    public static void main(String[] args) {
        MainController controller = new MainController();
        controller.mainLoop();
    }
}
