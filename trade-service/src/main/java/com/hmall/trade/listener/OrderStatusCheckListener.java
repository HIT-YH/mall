package com.hmall.trade.listener;

import com.hmall.api.client.PayClient;
import com.hmall.common.constants.MqConstants;
import com.hmall.common.domain.MultiDelayMessage;
import com.hmall.common.mq.DelayMessageProcessor;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusCheckListener {

    private final IOrderService orderService;
    private final RabbitTemplate rabbitTemplate;
    private final PayClient payClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConstants.DELAY_ORDER_QUEUE, durable = "true"),
            exchange = @Exchange(value = MqConstants.DELAY_EXCHANGE, delayed = "true" , type = ExchangeTypes.TOPIC),
            key = MqConstants.DELAY_ORDER_ROUTING_KEY
    ))
    public void listenOrderDelayMessage(MultiDelayMessage<Long> msg){
        //1.查询订单状态
        Order order = orderService.getById(msg.getData());

        //2.判断是否已经支付
        if(order == null || order.getStatus() == 2){
            return;
        }

        //3.主动去支付服务查询真正的支付状态
        Boolean isPay = payClient.isPayByPayOrder(order.getId());

        if(isPay){
            //3.1已经支付，标记订单状态
            orderService.markOrderPaySuccess(order.getId());
            return;
        }


        //4.判断是否存在延迟时间
        if(msg.hasNextDelay()){
            //4.1存在,则继续发送延迟消息
            Long nextDelay = msg.removeNextDelay();
            rabbitTemplate.convertAndSend(MqConstants.DELAY_EXCHANGE,MqConstants.DELAY_ORDER_ROUTING_KEY,msg,
                    new DelayMessageProcessor(nextDelay.intValue()));
            log.info("订单未支付且未超时");
            return;
        }


        //5.不存在，则取消订单并恢复库存
        orderService.cancelOrder(order.getId());




    }

}
