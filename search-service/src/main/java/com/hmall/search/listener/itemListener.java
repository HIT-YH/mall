package com.hmall.search.listener;


import com.hmall.common.constants.MqConstants;
import com.hmall.common.utils.BeanUtils;
import com.hmall.search.domain.po.Item;
import com.hmall.search.service.ISearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class itemListener {

    private final ISearchService iSearchService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name= MqConstants.SEARCH_UPDATE_QUEUE,durable = "true"),
            exchange = @Exchange(name = MqConstants.SEARCH_EXCHANGE,type = ExchangeTypes.TOPIC),
            key = MqConstants.SEARCH_UPDATE_ROUTING_KEY
    ))
    public void listenItemUpdate(List<com.hmall.common.domain.po.Item> commonItems) {
        List<Item> items = BeanUtils.copyList(commonItems,Item.class);
        iSearchService.updateItems(items);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name= MqConstants.SEARCH_DELETE_QUEUE,durable = "true"),
            exchange = @Exchange(name = MqConstants.SEARCH_EXCHANGE,type = ExchangeTypes.TOPIC),
            key = MqConstants.SEARCH_DELETE_ROUTING_KEY
    ))
    public void listenItemDelete(List<Long> ids) {
        iSearchService.deleteItems(ids);
    }

}
