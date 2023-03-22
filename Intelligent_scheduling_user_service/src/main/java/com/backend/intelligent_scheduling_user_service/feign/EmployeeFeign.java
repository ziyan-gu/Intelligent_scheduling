package com.backend.intelligent_scheduling_user_service.feign;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * 员工模块调用
 */
@FeignClient(value = "employeeservice",url = "http://localhost")
public interface EmployeeFeign {
//    @PostMapping("/login")
//    @ApiOperation("员工登录")
//    BaseResponse<String> employeeLogin(@RequestBody EmployeeLoginRequest employeeLoginRequest,
//                                     @RequestParam HttpServletRequest request);
}
