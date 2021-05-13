package wetalk_server.utils;

import wetalk_server.App;

import java.io.*;
import java.util.Properties;

/**
 * Global Constance for the program
 * Singleton design pattern
 */
public class Global extends Properties {
    private final static Global instance = new Global();

    /**
     * Constructor of Global class
     */
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

    /**
     * Return an instance of Cache class
     * @return An instance of Cache class
     */
    public static Global getInstance() { return Global.instance; }
}
