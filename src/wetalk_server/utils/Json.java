package wetalk_server.utils;

import com.google.gson.Gson;

import java.lang.reflect.Type;


/**
 * Wrapper class for Gson
 * An encoder and decoder of Json string data
 * singleton design pattern
 */
public class Json{
    private static final Json instance = new Json();
    private final Gson gson;

    /**
     * Constructor of Json class
     */
    public Json() {
        this.gson = new Gson();
    }

    /**
     * Convert Json string to Java object
     * @param json Json string
     * @param typeOf Java object type
     * @param <T> Return Java object type
     * @return Return correct type of java object
     */
    public <T> T fromJson(String json, Type typeOf) {
        return this.gson.fromJson(json, typeOf);
    }

    /**
     * Convert Java object to Json string
     * @param src Source Java object
     * @return Return correct Json string
     */
    public String toJson(Object src) {
        return this.gson.toJson(src);
    }

    /**
     * returns the only one instance of Json
     * @return an instance of Json
     */
    public static Json getInstance() {
        return Json.instance;
    }
}
