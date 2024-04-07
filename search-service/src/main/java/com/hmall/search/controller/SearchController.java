package com.hmall.search.controller;


import com.hmall.common.domain.PageDTO;
import com.hmall.search.domain.dto.ItemDTO;
import com.hmall.search.domain.query.ItemPageQuery;
import com.hmall.search.service.ISearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags = "搜索相关接口")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final ISearchService iSearchService;

    @ApiOperation("搜索商品")
    @GetMapping("/list")
    public PageDTO<ItemDTO> search(ItemPageQuery query) {
        // 分页查询
//        Page<Item> result = iSearchService.lambdaQuery()
//                .like(StrUtil.isNotBlank(query.getKey()), Item::getName, query.getKey())
//                .eq(StrUtil.isNotBlank(query.getBrand()), Item::getBrand, query.getBrand())
//                .eq(StrUtil.isNotBlank(query.getCategory()), Item::getCategory, query.getCategory())
//                .eq(Item::getStatus, 1)
//                .between(query.getMaxPrice() != null, Item::getPrice, query.getMinPrice(), query.getMaxPrice())
//                .page(query.toMpPage("update_time", false));
//        // 封装并返回
//        PageDTO<ItemDTO> pageDTO = PageDTO.of(result, ItemDTO.class);
//        log.info("共搜索到"+pageDTO.getTotal()+"条数据。共"+pageDTO.getPages()+"页数据");
//        log.info("当前页的数据量为: "+pageDTO.getList().size());
//        return pageDTO;

        PageDTO<ItemDTO> pageDTO = iSearchService.searchES(query);
        log.info("共搜索到"+pageDTO.getTotal()+"条数据。共"+pageDTO.getPages()+"页数据");
        log.info("当前页的数据量为: "+pageDTO.getList().size());
        return pageDTO;
    }
}
