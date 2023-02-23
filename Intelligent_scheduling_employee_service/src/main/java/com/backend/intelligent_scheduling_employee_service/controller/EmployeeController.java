package com.backend.intelligent_scheduling_employee_service.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.backend.intelligent_scheduling_employee_service.model.Employee;
import com.backend.intelligent_scheduling_employee_service.model.request.EmployeeAddRequest;
import com.backend.intelligent_scheduling_employee_service.service.EmployeeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @DeleteMapping("/{id}")
    public String deleteEmployee(@PathVariable String id) {
        if(id == null){
            return null;
        }
        if (StringUtils.isAnyBlank(id.toString())){
            return null;
        }
        employeeService.remove(new QueryWrapper<Employee>().eq("id", id));
        return "ok";
    }

    @PostMapping
    public String addEmployee(@RequestBody EmployeeAddRequest employeeAddRequest){
        if(employeeAddRequest == null){
            return null;
        }
        String id = employeeAddRequest.getId();
        String name = employeeAddRequest.getName();
        String email = employeeAddRequest.getEmail();
        Integer position = employeeAddRequest.getPosition();
        String store = employeeAddRequest.getStore();

        if(StringUtils.isAnyBlank(id,name,email,position.toString(),store)){
            return null;
        }

        String result = employeeService.addEmployee(id, name, email, position, store);

        return "ok";


    }


}
