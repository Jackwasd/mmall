package com.mmall.task;

import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务的类，定时关单
 */
@Component
@Slf4j
public class CloseOrderTask {
    @Autowired
    private IOrderService iOrderService;

    /**
     * 关闭任务方法的第一个版本, 没有分布式锁
     */
    @Scheduled(cron="0 */1 * * * ?")       //注解代表的意思是每分钟执行一次(每个一分钟的整数倍)
    public void closeOrderTaskV1(){
        log.info("关闭订单定时任务启动");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        //会关闭以当前时间为准，两个小时之前未关闭的订单
        iOrderService.closeOrder(hour);
        log.info("关闭订单定时任务结束");
    }
}
