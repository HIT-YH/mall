package com.hmall.trade.listener;

import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PayStatusListener {

    private final IOrderService orderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "mark.order.pay.queue",durable = "true"),
            exchange = @Exchange(name = "pay.topic", type = ExchangeTypes.TOPIC),
            key = "pay.success"
    ))
    public void listenOrderPay(Long orderId){
//        //1.查询订单状态
//        Order order = orderService.getById(orderId);
//
//        //2.判断订单状态是否为未支付
//        if(order == null || order.getStatus() != 1){
//            //异常情况
//            return;
//        }
//
//        //3.标记订单状态为已支付
//        orderService.markOrderPaySuccess(orderId);

        //使用乐观锁机制，即update order set status = 2 where id = ? and status = 1
        orderService.lambdaUpdate()
                .set(Order::getStatus,2)
                .set(Order::getPayTime, LocalDateTime.now())
                .eq(orderService::getById,orderId)
                .eq(Order::getStatus,1);

    }

}
