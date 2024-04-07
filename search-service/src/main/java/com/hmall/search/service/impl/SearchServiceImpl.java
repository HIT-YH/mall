package com.hmall.search.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.dto.ItemESDTO;
import com.hmall.common.utils.BeanUtils;
import com.hmall.search.domain.dto.ItemDTO;
import com.hmall.search.domain.po.Item;
import com.hmall.search.domain.query.ItemPageQuery;
import com.hmall.search.mapper.ItemMapper;
import com.hmall.search.service.ISearchService;
import com.hmall.search.utils.ESResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
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

//        log.info("ES服务器信息: {}",httpHost);


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
    public PageDTO<ItemDTO> searchES(ItemPageQuery query) {
//        // 分页查询
//        Page<Item> result = iSearchService.lambdaQuery()
//                .like(StrUtil.isNotBlank(query.getKey()), Item::getName, query.getKey())
//                .eq(StrUtil.isNotBlank(query.getBrand()), Item::getBrand, query.getBrand())
//                .eq(StrUtil.isNotBlank(query.getCategory()), Item::getCategory, query.getCategory())
//                .eq(Item::getStatus, 1)
//                .between(query.getMaxPrice() != null, Item::getPrice, query.getMinPrice(), query.getMaxPrice())
//                .page(query.toMpPage("update_time", false));
//        // 封装并返回
//        return PageDTO.of(result, com.hmall.api.dto.ItemDTO.class);

        String httpHost = "http://"+ host + ":" + port;

        //初始化RestHighLevelClient
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create(httpHost)
        ));

        // 1.创建Request
        SearchRequest request = new SearchRequest("items");

        //2.bool复合查询
        BoolQueryBuilder bool = QueryBuilders.boolQuery();

        //2.1关键词过滤
        if(StrUtil.isNotBlank(query.getKey())){
            bool.must(QueryBuilders.matchQuery("name", query.getKey()));
        }

        //2.2品牌过滤
        if(StrUtil.isNotBlank(query.getBrand())){
            bool.filter(QueryBuilders.termQuery("brand",query.getBrand()));
        }

        //2.3分类过滤
        if(StrUtil.isNotBlank(query.getCategory())){
            bool.filter(QueryBuilders.termQuery("category",query.getCategory()));
        }

        //2.4价格区间过滤
        if(query.getMaxPrice() != null){
            bool.filter(QueryBuilders.rangeQuery("price").lte(query.getMaxPrice()).gte(query.getMinPrice()));
        }

        request.source().query(bool);

        //3.设置分页参数
        request.source().from((query.getPageNo() - 1) * query.getPageSize()).size(query.getPageSize());

        Page<ItemDTO> page = new Page<>(query.getPageNo(), query.getPageSize());

        //4.发送请求
        try {
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);

            PageDTO<ItemDTO> result = ESResponseHandler.handlePageSearchResponse(response,page);

            client.close();
            return result;
        } catch (IOException e) {
            log.error("删除es数据失败: ", e);
            //如果出现异常,就查询结果为空
            return PageDTO.of(null);
        }


    }


}
