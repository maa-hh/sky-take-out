package com.sky.Task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
@Component
public class OrderTask {
    @Autowired
    OrderMapper orderMapper;
    @Scheduled(cron = "0 * * * * ?")
    public void checkOrder(){
        List<Orders> list=orderMapper.getStatusOrdertime(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(15));
        if(list!=null && !list.isEmpty()){
            for(Orders order:list){
                order.setStatus(Orders.CANCELLED);
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason("超时");
                orderMapper.update(order);
            }
        }
    }
    @Scheduled(cron = "0 0 1 * * ?")
    public void checkDelivery(){
        List<Orders> list=orderMapper.getStatusOrdertime(Orders.DELIVERY_IN_PROGRESS,LocalDateTime.now().minusHours(1));
        if(list!=null && !list.isEmpty()){
            for(Orders order:list){
                order.setStatus(Orders.COMPLETED);
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }
    }
}
