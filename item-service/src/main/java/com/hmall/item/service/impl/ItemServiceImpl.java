package com.hmall.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.common.constants.MqConstants;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.BeanUtils;
import com.hmall.item.domain.po.Item;
import com.hmall.item.mapper.ItemMapper;
import com.hmall.item.service.IItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author 虎哥
 */
@Service
@RequiredArgsConstructor
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements IItemService {

    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public void deductStock(List<OrderDetailDTO> items) {
        String sqlStatement = "com.hmall.item.mapper.ItemMapper.reduceStock";
        boolean r = false;
        try {
            r = executeBatch(items, (sqlSession, entity) -> sqlSession.update(sqlStatement, entity));
        } catch (Exception e) {
            log.error("更新库存异常", e);
            throw new BizIllegalException("库存不足！");
        }
        if (!r) {
            throw new BizIllegalException("库存不足！");
        }

        //异步调用mq更新elasticsearch
        List<Item> es_items = new ArrayList<Item>();

        for (OrderDetailDTO itemDTO : items) {
            Item item = getById(itemDTO.getItemId());
            es_items.add(item);
        }

        updateElasticsearch(es_items);


    }


    @Override
    @Transactional
    public void addStock(List<OrderDetailDTO> items) {

        List<Item> es_items = new ArrayList<Item>();

        for (OrderDetailDTO itemDTO : items) {
            Long id = itemDTO.getItemId();
            Integer num = itemDTO.getNum();
            System.out.println("商品 "+id+" 恢复库存 "+num+" 个");
            Item item = getById(id);
            item.setStock(item.getStock() + num);

            lambdaUpdate()
                    .set(Item::getStock,item.getStock())
                    .eq(Item::getId,id).update();

            es_items.add(item);

        }

        log.debug("恢复库存成功");

        //异步调用mq更新elasticsearch
        updateElasticsearch(es_items);

    }


    @Override
    public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
        return BeanUtils.copyList(listByIds(ids), ItemDTO.class);
    }

    @Override
    public void updateElasticsearch(List<Item> items){

        List<com.hmall.common.domain.po.Item> commonItems = BeanUtils.copyList(items, com.hmall.common.domain.po.Item.class);

        try {
            rabbitTemplate.convertAndSend(MqConstants.SEARCH_EXCHANGE,MqConstants.SEARCH_UPDATE_ROUTING_KEY,commonItems);
        } catch (AmqpException e) {
            log.error("数据更新同步到Elasticsearch失败",e);
        }
    }

    @Override
    public void deleteElasticsearch(List<Long> ids) {
        try {
            rabbitTemplate.convertAndSend(MqConstants.SEARCH_EXCHANGE,MqConstants.SEARCH_DELETE_ROUTING_KEY,ids);
        } catch (AmqpException e) {
            log.error("数据删除同步到Elasticsearch失败",e);
        }
    }
}
