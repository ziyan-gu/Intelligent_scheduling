package com.backend.intelligent_scheduling_employee_service.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.backend.intelligent_scheduling_employee_service.model.Employee;
import com.backend.intelligent_scheduling_employee_service.model.request.EmployeeAddRequest;
import com.backend.intelligent_scheduling_employee_service.service.EmployeeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        if (StringUtils.isAnyBlank(id)){
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
    public List<Employee> getAllEmployees(){
        List<Employee> employees = employeeService.list();
        employees.forEach(employee -> employee.setPassword(null));
        return employees;
    }

    @GetMapping("/{id}")
    public Employee getEmployee(@PathVariable String id){
        if(id == null || StringUtils.isAnyBlank(id)){
            return null;
        }
        Employee employee = employeeService.getOne(new QueryWrapper<Employee>().eq("id", id));
        employee.setPassword(null);
        return employee;
    }

    @PutMapping("/prefer/{id}")
    public String modifyEmployeePreference(@PathVariable String id,@RequestBody Employee employee) throws JsonProcessingException {
        if(id == null || StringUtils.isAnyBlank(id)){
            return null;
        }

        Employee oldEmployee = employeeService.getOne(new QueryWrapper<Employee>().eq("id", id));

        //hashmap -> json
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(employee.getPreferenceValue());
        oldEmployee.setPreferenceValue(json);

        UpdateWrapper<Employee> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id);
        boolean result = employeeService.update(oldEmployee,updateWrapper);

        return "ok";
    }
}
