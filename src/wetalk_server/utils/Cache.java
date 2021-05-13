package wetalk_server.utils;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.List;


/**
 * Cache class
 * push/pop work like a queue (FIFO)
 * Depend on Redis
 * Singleton design pattern
 */
public class Cache{
    private final static Cache instance = new Cache();
    private final JedisPool pool;

    /**
     * Constructor of Cache class
     * Will create a redis connection pool
     */
    private Cache() {
        String host = Global.getInstance().getProperty("redisAddress");
        int port = Integer.parseInt(Global.getInstance().getProperty("redisPort"));
        this.pool = new JedisPool(host, port);
    }

    /**
     * Close redis connection pool
     */
    public void close() {
        this.pool.close();
    }

    /**
     * Push the value to a queue named by key
     * @param key String key
     * @param value String value
     */
    public void push(String key, String value) {
        Jedis conn = this.pool.getResource();
        conn.rpush(key, value);
        conn.close();
    }

    /**
     * Pop a data from the queue of the key
     * @param key String key
     * @return Return string value
     */
    public String pop(String key) {
        Jedis conn = this.pool.getResource();
        String data = conn.lpop(key);
        conn.close();
        return data;
    }

    /**
     * Pop all data from the queue of the key
     * @param key
     * @return
     */
    public List<String> popAll(String key) {
        Jedis conn = this.pool.getResource();
        Pipeline pipeline = conn.pipelined();
        Response<List<String>> response = pipeline.lrange(key, 0, -1);
        pipeline.del(key);
        pipeline.sync();
        pipeline.close();
        conn.close();
        return response.get();
    }

    /**
     * Check if the queue named key has the value
     * @param key String key
     * @param value String value
     * @return Return a boolean flag
     */
    public boolean contains(String key, String value) {
        Jedis conn = this.pool.getResource();
        List<String> result = conn.lrange(key, 0, -1);
        conn.close();
        return result.contains(value);
    }

    /**
     * Add the value to a set named key
     * @param key String key
     * @param value String value
     */
    public void sAdd(String key, String value) {
        Jedis conn = pool.getResource();
        conn.sadd(key, value);
        conn.close();
    }

    /**
     * Remove the value from a set named key
     * @param key String key
     * @param value String value
     */
    public void sRem(String key, String value) {
        Jedis conn = pool.getResource();
        conn.srem(key, value);
        conn.close();
    }

    /**
     * Check if the set named key has the value
     * @param key String key
     * @param value String value
     * @return Return a boolean flag
     */
    public boolean sIsMember(String key, String value) {
        Jedis conn = pool.getResource();
        boolean isMember = conn.sismember(key, value);
        conn.close();
        return isMember;
    }

    /**
     * Return an instance of Cache class
     * @return An instance of Cache class
     */
    public static Cache getInstance() { return Cache.instance; }
}

