package com.backend.intelligent_scheduling_login.feign;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * 远程调用排班算法
 */
@FeignClient(value = "orderService")
public interface OrderFeign {

}
