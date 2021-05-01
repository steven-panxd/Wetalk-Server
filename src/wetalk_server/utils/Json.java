package wetalk_server.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;


/**
 * Wrapper class for Gson, singleton design pattern
 */
public class Json {
    private static final Json instance = new Json();
    private final Gson gson;

    public Json() {
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        this.gson = builder.create();
    }

    public <T> T fromJson(String json, Type typeOf) {
        return this.gson.fromJson(json, typeOf);
    }

    public String toJson(Object src) {
        return this.gson.toJson(src);
    }

    public static Json getInstance() {
        return Json.instance;
    }
}
