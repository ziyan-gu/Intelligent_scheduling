package com.backend.intelligent_scheduling_employee_service.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.backend.intelligent_scheduling_employee_service.common.BaseResponse;
import com.backend.intelligent_scheduling_employee_service.common.ErrorCode;
import com.backend.intelligent_scheduling_employee_service.common.ResultUtils;
import com.backend.intelligent_scheduling_employee_service.exception.BusinessException;
import com.backend.intelligent_scheduling_employee_service.model.Employee;
import com.backend.intelligent_scheduling_employee_service.model.request.EmployeeAddRequest;
import com.backend.intelligent_scheduling_employee_service.model.request.EmployeeLoginRequest;
import com.backend.intelligent_scheduling_employee_service.service.EmployeeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> deleteEmployee(@PathVariable String id) {
        if(id == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if (StringUtils.isAnyBlank(id)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"存在空格");
        }
        boolean result = employeeService.remove(new QueryWrapper<Employee>().eq("id", id));
        return ResultUtils.success(result);
    }

    @PostMapping
    public BaseResponse<String> addEmployee(@RequestBody EmployeeAddRequest employeeAddRequest){
        if(employeeAddRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        String id = employeeAddRequest.getId();
        String name = employeeAddRequest.getName();
        String email = employeeAddRequest.getEmail();
        Integer position = employeeAddRequest.getPosition();
        String store = employeeAddRequest.getStore();

        if(StringUtils.isAnyBlank(id,name,email,position.toString(),store)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"存在空格");
        }

        String result = employeeService.addEmployee(id, name, email, position, store);

        return ResultUtils.success(result);


    }

//    @PutMapping("/{id}")
//    public String modifyEmployee(@PathVariable String id, @RequestBody Employee employee){
//        if (id == null || employee == null || StringUtils.isAnyBlank(id)) {
//            return null;
//        }
//
//        boolean result = employeeService.update(employee, new UpdateWrapper<Employee>().eq("id", id));
//        return "ok";
//    }

    @GetMapping
    public BaseResponse<List<Employee>> getAllEmployees(){
        List<Employee> employees = employeeService.list();
        employees.forEach(employee -> employee.setPassword(null));
        return ResultUtils.success(employees);
    }

    @GetMapping("/{id}")
    public BaseResponse<Employee> getEmployee(@PathVariable String id){
        if(id == null || StringUtils.isAnyBlank(id)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空或存在非法字符");
        }
        Employee employee = employeeService.getOne(new QueryWrapper<Employee>().eq("id", id));
        employee.setPassword(null);
        return ResultUtils.success(employee);
    }

    @PutMapping("/prefer/{id}")
    public BaseResponse<Boolean> modifyEmployeePreference(@PathVariable String id, @RequestBody Employee employee) throws JsonProcessingException {
        if(id == null || StringUtils.isAnyBlank(id)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空或存在非法字符");
        }

        boolean result = employeeService.modifyEmployeePreferenceService(id,employee);

        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<Employee> userLogin(@RequestBody EmployeeLoginRequest employeeLoginRequest,
                                            HttpServletRequest request) {
        if (employeeLoginRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"请求数据为空");
        }
        String email = employeeLoginRequest.getEmail();
        String password = employeeLoginRequest.getPassword();

        if (StringUtils.isAnyBlank(email,password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"输入存在空格");
        }
        Employee employee = employeeService.employeeLogin(email, password, request);
        return ResultUtils.success(employee);
    }
}
