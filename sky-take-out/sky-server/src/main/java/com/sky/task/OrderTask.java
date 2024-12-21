package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类
 */

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 定时处理超时订单
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeOutOrder() {
        log.info("定时处理超时订单: {}", LocalDateTime.now());

        // 获得当前时间减去 15 分钟后的时间
        LocalDateTime time = LocalDateTime.now().minusMinutes(15);
        // 查询出下单时间早于 time 的订单，说明此订单已超时
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                // 将超时订单状态设置为已取消
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("超时未支付，订单自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }

    }

    /**
     * 定时处理还在派送中的订单
     * 每天凌晨 1 点执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder() {
        log.info("定时处理派送中订单: {}", LocalDateTime.now());

        // 获得当前时间减去 60 分钟后的时间（每天凌晨 1 点执行）
        LocalDateTime time = LocalDateTime.now().minusMinutes(60);
        // 查询出下单时间早于 time 的订单，说明此订单已满足完成条件
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now());
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                // 将订单状态设置为已完成
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
