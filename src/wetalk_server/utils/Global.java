package wetalk_server.utils;

import wetalk_server.App;

import java.io.*;
import java.util.Properties;

/*
Singleton Pattern
Global Constance for the program
 */
public class Global extends Properties {
    private final static Global instance = new Global();

    private Global() {
        Properties props = new Properties();

        try {
            InputStream stream = App.class.getResourceAsStream("config.properties");
            props.load(stream);
        } catch (IOException exception) {
            System.out.println("Can not read the config.properties");
            System.exit(0);
        }

        for(String propertyName: props.stringPropertyNames()) {
            this.put(propertyName, props.getProperty(propertyName));
        }
    }

    public static Global getInstance() { return Global.instance; }
}
