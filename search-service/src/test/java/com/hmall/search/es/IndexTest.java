package com.hmall.search.es;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

//索引库测试类
public class IndexTest {

    private RestHighLevelClient client;

    static final String MAPPING_TEMPLATE = "{\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"id\": {\n" +
            "        \"type\": \"Long\"\n" +
            "      },\n" +
            "      \"name\":{\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_max_word\"\n" +
            "      },\n" +
            "      \"price\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"stock\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"image\":{\n" +
            "        \"type\": \"keyword\",\n" +
            "        \"index\": false\n" +
            "      },\n" +
            "      \"category\":{\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"brand\":{\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"sold\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"commentCount\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"isAD\":{\n" +
            "        \"type\": \"boolean\"\n" +
            "      },\n" +
            "      \"updateTime\":{\n" +
            "        \"type\": \"date\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    //在每次测试前执行，初始化client的代码
    @BeforeEach
    void setUp() {
        this.client = new RestHighLevelClient(RestClient.builder(//初始化操作代理
                HttpHost.create("http://192.168.137.128:9200")
        ));
    }

    //在每次测试后执行，关闭client的代码，关闭资源
    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }

    @Test
    void testConnect() {
        System.out.println(client);
    }



    @Test
    void testCreateIndex() throws IOException {//创建索引库
        // 1.创建Request对象
        CreateIndexRequest request = new CreateIndexRequest("items");
        // 2.准备请求参数
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        // 3.发送请求
        //client.indices()表示获取索引库的操作对象
        client.indices().create(request, RequestOptions.DEFAULT);
    }



    @Test
    void testDeleteIndex() throws IOException {//删除索引库
        // 1.创建Request对象
        DeleteIndexRequest request = new DeleteIndexRequest("items");
        // 2.发送请求
        client.indices().delete(request, RequestOptions.DEFAULT);
    }


    @Test
    void testExistsIndex() throws IOException {//判断索引库是否存在
        // 1.创建Request对象
        GetIndexRequest request = new GetIndexRequest("items");
        // 2.发送请求
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        // 3.输出
        System.err.println(exists ? "索引库已经存在！" : "索引库不存在！");
    }

}
