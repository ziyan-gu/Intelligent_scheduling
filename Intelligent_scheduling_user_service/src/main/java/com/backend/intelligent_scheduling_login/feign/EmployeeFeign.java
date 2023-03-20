package com.backend.intelligent_scheduling_login.feign;

import com.backend.intelligent_scheduling_login.common.BaseResponse;
import com.backend.intelligent_scheduling_login.model.request.EmployeeLoginRequest;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

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
