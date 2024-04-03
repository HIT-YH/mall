package com.hmall.api.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("pay-service")
public interface PayClient {
    @GetMapping("/pay-orders/getStatus")
    Boolean isPayByPayOrder(@RequestParam("Biz_id") Long Biz_id);


}
