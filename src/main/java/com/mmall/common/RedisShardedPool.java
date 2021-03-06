package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

/**
 * shard分片的意思
 */
public class RedisShardedPool {
    private static ShardedJedisPool pool; //sharded jedis连接池
    //这里使用getProperties的重载方法，加上一个defalutvlue防止配置文件被修改，出现空指针
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total", "20"));//最大连接数
    private static Integer maxIdle =  Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle", "10"));//jedis连接池中最大Idle(空闲状态)的jedis实例的个数
    private static Integer minIdle =  Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle", "2"));//jedis连接池中最小Idle(空闲状态)的jedis实例的个数
    private static Boolean testOnBorrow =  Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow", "true"));//在borrow(借)一个jedis实例的时候，是否要进行验证操作，如果赋值true,则得到的jedis实例肯定可用
    private static Boolean testOnReturn =  Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return", "true"));//在return(还)一个jedis实例的时候，是否要进行验证操作，如果赋值true,则放回的jedispoold的实例肯定可用

    private static String redis1Ip = PropertiesUtil.getProperty("redis1.ip");
    private static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));
    private static String redis2Ip = PropertiesUtil.getProperty("redis2.ip");
    private static Integer redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis2.port"));

    //初始化连接池
    private static void initPool(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        config.setBlockWhenExhausted(true);     //连接耗尽的时候，是否阻塞，false会抛出异常，true阻塞直到超时，默认为true

        JedisShardInfo info1 = new JedisShardInfo(redis1Ip, redis1Port, 1000*2);
        JedisShardInfo info2 = new JedisShardInfo(redis2Ip, redis2Port, 1000*2);

        List<JedisShardInfo> jedisShardInfoList = new ArrayList<JedisShardInfo>(2);
        jedisShardInfoList.add(info1);
        jedisShardInfoList.add(info2);
        //Hashing.MURMUR_HASH代表的就是Hash一致性算法
        //Sharded是一个枚举
        pool = new ShardedJedisPool(config, jedisShardInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);
    }

    //加载文件的时候直接初始化连接池
    static {
        initPool();
    }

    //从连接池中获取jedis的实例
    public static ShardedJedis getJedis(){
        return pool.getResource();
    }

    //把jedis放回连接池
    public static void returnResource(ShardedJedis jedis){
        pool.returnResource(jedis);
    }

    public static void returnBrokenResource(ShardedJedis jedis){
        pool.returnBrokenResource(jedis);
    }

    public static void main(String[] args) {
        ShardedJedis jedis = pool.getResource();
        for(int i = 0; i < 10; i++){
            jedis.set("key" + i, "value" + i);
            jedis.get("key" + i);
        }
        returnResource(jedis);
        System.out.println("program is end");
    }


}
