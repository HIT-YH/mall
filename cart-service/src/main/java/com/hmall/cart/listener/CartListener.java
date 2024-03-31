package com.hmall.cart.listener;

import com.hmall.cart.service.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class CartListener {

    private final ICartService iCartService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name="cart.clear.queue",durable = "true"),
            exchange = @Exchange(name = "trade.topic",type = ExchangeTypes.TOPIC),
            key = "order.create"
    ))
    public void listenOrderCreate(Collection<Long> itemIds) {
        iCartService.removeByItemIds(itemIds);
    }

}
