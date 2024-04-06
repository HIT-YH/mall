package com.hmall.search.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.BeanUtils;
import com.hmall.search.domain.po.Item;
import com.hmall.search.mapper.ItemMapper;
import com.hmall.search.service.ISearchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class SearchServiceImpl extends ServiceImpl<ItemMapper, Item> implements ISearchService {


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
    }


    @Override
    @Transactional
    public void addStock(List<OrderDetailDTO> items) {
        for (OrderDetailDTO itemDTO : items) {
            Long id = itemDTO.getItemId();
            Integer num = itemDTO.getNum();
            System.out.println("商品 "+id+" 恢复库存 "+num+" 个");
            Item item = getById(id);
            lambdaUpdate()
                    .set(Item::getStock,item.getStock() + num)
                    .eq(Item::getId,id).update();
        }

        log.debug("恢复库存成功");

    }


    @Override
    public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
        return BeanUtils.copyList(listByIds(ids), ItemDTO.class);
    }


}
