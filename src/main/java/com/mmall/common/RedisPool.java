package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 创建一个redis连接池
 */
public class RedisPool {
    private static JedisPool pool; //jedis连接池
    //这里使用getProperties的重载方法，加上一个defalutvlue防止配置文件被修改，出现空指针
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total", "20"));//最大连接数
    private static Integer maxIdle =  Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle", "10"));//jedis连接池中最大Idle(空闲状态)的jedis实例的个数
    private static Integer minIdle =  Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle", "2"));//jedis连接池中最小Idle(空闲状态)的jedis实例的个数
    private static Boolean testOnBorrow =  Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow", "true"));//在borrow(借)一个jedis实例的时候，是否要进行验证操作，如果赋值true,则得到的jedis实例肯定可用
    private static Boolean testOnReturn =  Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return", "true"));//在return(还)一个jedis实例的时候，是否要进行验证操作，如果赋值true,则放回的jedispoold的实例肯定可用

    private static String redisIp = PropertiesUtil.getProperty("redis.ip");
    private static Integer redisPort = Integer.parseInt(PropertiesUtil.getProperty("redis.port"));

    //初始化连接池
    private static void initPool(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        config.setBlockWhenExhausted(true);     //连接耗尽的时候，是否阻塞，false会抛出异常，true阻塞直到超时，默认为true

        pool = new JedisPool(config, redisIp, redisPort, 1000*2); //超时时间设置为2秒(2000毫米)
    }

    //加载文件的时候直接初始化连接池
    static {
        initPool();
    }

    //从连接池中获取jedis的实例
    public static Jedis getJedis(){
        return pool.getResource();
    }

    //把jedis放回连接池
    public static void returnResource(Jedis jedis){
            pool.returnResource(jedis);
    }

    public static void returnBrokenResource(Jedis jedis){
        pool.returnBrokenResource(jedis);
    }

}
