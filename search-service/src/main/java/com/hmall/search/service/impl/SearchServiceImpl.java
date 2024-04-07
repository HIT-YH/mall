package com.hmall.search.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.dto.ItemESDTO;
import com.hmall.common.utils.BeanUtils;
import com.hmall.search.domain.dto.ItemDTO;
import com.hmall.search.domain.po.Item;
import com.hmall.search.domain.query.ItemPageQuery;
import com.hmall.search.mapper.ItemMapper;
import com.hmall.search.service.ISearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author 虎哥
 */
@Slf4j
@Service
public class SearchServiceImpl extends ServiceImpl<ItemMapper, Item> implements ISearchService {

    @Value("${hm.es.host}")
    private String host;

    @Value("${hm.es.port}")
    private String port;


    @Override
    public void updateItems(List<Item> items) {

        String httpHost = "http://"+ host + ":" + port;

        List<ItemESDTO> itemDTOS = BeanUtils.copyList(items, ItemESDTO.class);

        log.info("ES服务器信息: {}",httpHost);


        //初始化RestHighLevelClient
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create(httpHost)
        ));
        // 1.创建Request
        BulkRequest request = new BulkRequest();
        // 2.准备请求参数
        for (ItemESDTO itemDTO : itemDTOS) {//批处理
            String doc = JSONUtil.toJsonStr(itemDTO);
            log.info(doc);
            request.add(new IndexRequest("items").id(itemDTO.getId()).source(doc, XContentType.JSON));
        }

        // 3.发送请求
        try {
            client.bulk(request, RequestOptions.DEFAULT);
            client.close();
        } catch (IOException e) {
            log.error("更新es数据失败: ", e);
        }

        log.info("更新es数据成功");

    }


    @Override
    public void deleteItems(List<Long> ids)  {

        String httpHost = "http://"+ host + ":" + port;

        //初始化RestHighLevelClient
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create(httpHost)
        ));
        // 1.创建Request
        BulkRequest request = new BulkRequest();//批处理
        // 2.准备请求参数
        for (Long id : ids) {
            request.add(new DeleteRequest("items").id(id.toString()));
        }

        // 3.发送请求
        try {
            client.bulk(request, RequestOptions.DEFAULT);
            client.close();
        } catch (IOException e) {
            log.error("删除es数据失败: ", e);
        }

        log.info("删除es数据成功");

    }


    @Override
    public PageDTO<ItemDTO> search(ItemPageQuery query) {
        return null;
    }


}
