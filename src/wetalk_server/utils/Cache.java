package wetalk_server.utils;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.List;


/*
Works like a queue (FIFO)
 */
public class Cache{
    private final static Cache instance = new Cache();
    private final JedisPool pool;

    private Cache() {
        String host = Global.getInstance().getProperty("redisAddress");
        int port = Integer.parseInt(Global.getInstance().getProperty("redisPort"));
        this.pool = new JedisPool(host, port);
    }

    public void close() {
        this.pool.close();
    }

    public void push(String key, String value) {
        Jedis conn = this.pool.getResource();
        conn.rpush(key, value);
        conn.close();
    }

    public String pop(String key) {
        Jedis conn = this.pool.getResource();
        String data = conn.lpop(key);
        conn.close();
        return data;
    }

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

    public void sAdd(String key, String value) {
        Jedis conn = pool.getResource();
        conn.sadd(key, value);
        conn.close();
    }

    public void sRem(String key, String value) {
        Jedis conn = pool.getResource();
        conn.srem(key, value);
        conn.close();
    }

    public boolean sIsMember(String key, String value) {
        Jedis conn = pool.getResource();
        boolean isMember = conn.sismember(key, value);
        conn.close();
        return isMember;
    }

    public boolean contains(String key, String value) {
        Jedis conn = this.pool.getResource();
        List<String> result = conn.lrange(key, 0, -1);
        conn.close();
        return result.contains(value);
    }

    public static Cache getInstance() { return Cache.instance; }
}

