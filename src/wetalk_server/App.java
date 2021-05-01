package wetalk_server;

import wetalk_server.controller.MainController;

public class App {
    public static void main(String[] args) {
        MainController controller = new MainController();
        controller.mainLoop();
    }
}
