package com.hmall.search.utils;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.common.domain.PageDTO;
import com.hmall.search.domain.dto.ItemDTO;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.LinkedList;
import java.util.List;


public class ESResponseHandler {
    public static PageDTO<ItemDTO> handlePageSearchResponse(SearchResponse response, Page<ItemDTO> page) {//解析响应

        List<ItemDTO> list = new LinkedList<ItemDTO>();

        SearchHits searchHits = response.getHits();
        // 1.获取总条数
        long total = searchHits.getTotalHits().value;

        // 2.遍历结果数组
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            // 3.得到_source，也就是原始json文档
            String source = hit.getSourceAsString();
            // 4.反序列化
            ItemDTO item = JSONUtil.toBean(source, ItemDTO.class);

            list.add(item);

        }


        page.setRecords(list);
        page.setTotal(total);
        if(total%page.getSize() == 0){
            page.setPages(total/page.getSize());
        }else {
            page.setPages(total/page.getSize()+1);
        }

        // 封装并返回
        return PageDTO.of(page);
    }
}
