package com.hmall.item.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.PageQuery;
import com.hmall.common.utils.BeanUtils;
import com.hmall.item.domain.po.Item;
import com.hmall.item.service.IItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Api(tags = "商品管理相关接口")
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final IItemService itemService;

    @ApiOperation("分页查询商品")
    @GetMapping("/page")
    public PageDTO<ItemDTO> queryItemByPage(PageQuery query) {
        // 1.分页查询
        Page<Item> result = itemService.page(query.toMpPage("update_time", false));
        // 2.封装并返回
        return PageDTO.of(result, ItemDTO.class);
    }

    @ApiOperation("根据id批量查询商品")
    @GetMapping
    public List<ItemDTO> queryItemByIds(@RequestParam("ids") List<Long> ids){
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return itemService.queryItemByIds(ids);
    }

    @ApiOperation("根据id查询商品")
    @GetMapping("{id}")
    public ItemDTO queryItemById(@PathVariable("id") Long id) {
        return BeanUtils.copyBean(itemService.getById(id), ItemDTO.class);
    }

    @ApiOperation("新增商品")
    @PostMapping
    public void saveItem(@RequestBody ItemDTO itemDTO) {

        Item item = BeanUtils.copyBean(itemDTO, Item.class);
        // 新增
        itemService.save(item);

        //异步调用mq更新elasticsearch
        List<Item> es_items = new ArrayList<Item>();
        es_items.add(item);
        itemService.updateElasticsearch(es_items);

    }

    @ApiOperation("更新商品状态")
    @PutMapping("/status/{id}/{status}")
    public void updateItemStatus(@PathVariable("id") Long id, @PathVariable("status") Integer status){
        Item item = new Item();
        item.setId(id);
        int pre_Status = item.getStatus();
        item.setStatus(status);
        itemService.updateById(item);

        //异步调用mq更新elasticsearch
        if(item.getStatus() == 1){//商品上架
            List<Item> es_items = new ArrayList<Item>();
            es_items.add(item);
            itemService.updateElasticsearch(es_items);
        }

        if(pre_Status == 1 && item.getStatus() == 0){//商品从上架变成下降
            List<Long> es_ids = new ArrayList<Long>();
            es_ids.add(id);
            itemService.deleteElasticsearch(es_ids);
        }


    }

    @ApiOperation("更新商品")
    @PutMapping
    public void updateItem(@RequestBody ItemDTO itemDTO) {
        // 不允许修改商品状态，所以强制设置为null，更新时，就会忽略该字段
        itemDTO.setStatus(null);
        Item item = BeanUtils.copyBean(itemDTO, Item.class);

        // 更新
        itemService.updateById(item);

        //异步调用mq更新elasticsearch
        if(item.getStatus() == 1){
            List<Item> es_items = new ArrayList<Item>();
            es_items.add(item);
            itemService.updateElasticsearch(es_items);
        }


    }

    @ApiOperation("根据id删除商品")
    @DeleteMapping("{id}")
    public void deleteItemById(@PathVariable("id") Long id) {
        itemService.removeById(id);

        //异步调用mq更新elasticsearch
        List<Long> es_ids = new ArrayList<Long>();
        es_ids.add(id);
        itemService.deleteElasticsearch(es_ids);

    }

    @ApiOperation("批量扣减库存")
    @PutMapping("/stock/deduct")
    public void deductStock(@RequestBody List<OrderDetailDTO> items){
        itemService.deductStock(items);
    }

    @ApiOperation("增加指定商品的库存")
    @PutMapping("/stock/add")
    public void addStock(@RequestBody List<OrderDetailDTO> items){
        itemService.addStock(items);

    }

}
