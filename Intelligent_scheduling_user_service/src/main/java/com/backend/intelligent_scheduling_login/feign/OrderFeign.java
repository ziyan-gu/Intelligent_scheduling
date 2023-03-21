package com.backend.intelligent_scheduling_login.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 远程调用排班算法
 */
@FeignClient(value = "orderService")
public interface OrderFeign {

    @GetMapping("/getScheduling/{id}")
    Object getScheduling(@PathVariable String id);

}
