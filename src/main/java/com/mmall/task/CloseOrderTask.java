package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.common.RedissonManager;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 定时任务的类，定时关单,分布式锁是tomcat集群中所有的应用共享的，他们之间是竞争的关系，保证每次的定时任务只有一个服务器来执行
 * 为什么要进行分布式锁的添加，就是希望一个关单任务只由一台tomcat来执行
 */
@Component
@Slf4j
public class CloseOrderTask {
    @Autowired
    private IOrderService iOrderService;

    @Autowired
    private RedissonManager redissonManager;
    /**
     * 关闭任务方法的第一个版本, 没有分布式锁
     */
    //@Scheduled(cron="0 */1 * * * ?")       //注解代表的意思是每分钟执行一次(每个一分钟的整数倍)
    public void closeOrderTaskV1(){
        log.info("关闭订单定时任务启动");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        //会关闭以当前时间为准，两个小时之前未关闭的订单
        iOrderService.closeOrder(hour);
        log.info("关闭订单定时任务结束");
    }

    //@Scheduled(cron="0 */1 * * * ?")
    public void closeOrderTaskV2(){
        log.info("关闭订单定时任务启动");
        //声明分布式锁要锁多久,默认值是5秒
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "5000"));
        //里面的数值赋值成当前的毫秒数+设置的锁的时间
        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
        if(setnxResult != null && setnxResult.intValue() == 1){
            //如果返回值是1，代表设置成功，获取锁
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }else{
            log.info("没有获得分布式锁：{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }
        log.info("关闭订单定时任务结束");
    }

    //@Scheduled(cron="0 */1 * * * ?")
    public void closeOrderTaskV3(){
        log.info("关闭订单定时任务启动");
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "5000"));
        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
        if(setnxResult != null && setnxResult.intValue() == 1){
            //如果返回值是1，代表设置成功，获取锁
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }else{
            //没有获得分布式锁的情况，继续判断，判断时间戳，看是否可以重置并获取到锁
            //因为可能在运行到设置值的那一步的时候，tomcat正好中断了，那么就不会往下走，就会造成一个死锁的情况
            //为什么下面要各种不等于NUll，是因为在这个过程中其他的tomcat也在竞争锁，有可能先一步获取锁
            String lockValueStr = RedisShardedPoolUtil.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            //锁不为空，并且当前时间大于已经超过了锁的有效时间，那么锁
            if(lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)){
                //getset放方法在设置一个新值的时候返回一个旧值,也就是最新的时间，因为是Tomcat集群，所以要获取最新的旧值
                String getSetResult = RedisShardedPoolUtil.getSet(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
                if(getSetResult == null || (getSetResult != null && StringUtils.equals(lockValueStr, getSetResult))){
                    closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                } else {
                    log.info("没有获取到分布式锁:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }
            }else {
                log.info("没有获取到分布式锁:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            }
        }
        log.info("关闭订单定时任务结束");
    }

    @Scheduled(cron="0 */1 * * * ?")
    //使用Redisson来写分布式锁
    public void closeOrderTaskV4(){
        RLock lock = redissonManager.getRedisson().getLock(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        boolean getLock = false;
        try {
            //添加等待时间，锁的自动解锁时间，时间的单位
            if(lock.tryLock(2, 5, TimeUnit.SECONDS)){
                log.info("Redisson获取分布式锁:{},ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
                int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
                iOrderService.closeOrder(hour);
            }else{
                log.info("Redisson没有获取分布式锁:{},ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
            }
        } catch (InterruptedException e) {
            log.error("Redission分布式锁获取异常", e);
        } finally{
            if(!getLock){
                return;
            }
            lock.unlock();
            log.info("Redission分布式锁释放锁");
        }

    }


    private void closeOrder(String lockName){
        //有效期为5秒，防止死锁。因为setnx是说在有key的情况下无法存入，那么之前的锁如果不释放的话，就无法存入，返回-1
        RedisShardedPoolUtil.expire(lockName, 5);
        log.info("获取{}，ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        //iOrderService.closeOrder(hour);
        RedisShardedPoolUtil.del (Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        log.info("释放{}，ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
        log.info("==============================================================================");
    }

}
