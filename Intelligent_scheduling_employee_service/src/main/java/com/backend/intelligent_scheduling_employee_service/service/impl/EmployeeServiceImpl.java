package com.backend.intelligent_scheduling_employee_service.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.StringUtils;
import com.backend.intelligent_scheduling_employee_service.common.ErrorCode;
import com.backend.intelligent_scheduling_employee_service.common.UserInfoCheckUtil;
import com.backend.intelligent_scheduling_employee_service.exception.BusinessException;
import com.backend.intelligent_scheduling_employee_service.mapper.StoreMapper;
import com.backend.intelligent_scheduling_employee_service.model.Store;
import com.backend.intelligent_scheduling_employee_service.model.preference.Preference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.backend.intelligent_scheduling_employee_service.model.Employee;
import com.backend.intelligent_scheduling_employee_service.service.EmployeeService;
import com.backend.intelligent_scheduling_employee_service.mapper.EmployeeMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
* @author 86136
* @description 针对表【employee】的数据库操作Service实现
* @createDate 2023-02-23 23:05:29
*/
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee>
    implements EmployeeService{

    private final static String SALT = "wx";

    public String USER_LOGIN_STATE = "userLoginState";
    @Resource
    public EmployeeMapper employeeMapper;
    @Resource
    public StoreMapper storeMapper;

//    @Override
//    public String addNewEmployee(String id, String name, String email, Integer position, String store) {
//
//        //邮箱匹配
//        if(!UserInfoCheckUtil.isValidEmail(email)){
//            return null;
//        }
//
//        //账户不能重复
//        QueryWrapper<Employee> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("id", id);
//        long count = employeeMapper.selectCount(queryWrapper);
//        if(count>0){
//            return null;
//        }
//
//        //员工起始密码均为123456 进行加密
//        Employee employee = new Employee();
//        String password = "123456";
//        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
//
//        employee.setId(id);
//        employee.setName(name);
//        employee.setEmail(email);
//        employee.setPassword(encryptPassword);
//        employee.setPosition(position);
//        employee.setStore(store);
//
//        boolean saveResult = this.save(employee);
//        if(!saveResult){
//            return null;
//        }
//        return employee.getId();
//    }

    @Override
    public String addNewEmployee(String name, String email, Integer position, String store) throws JsonProcessingException {

        //邮箱匹配
        if(!UserInfoCheckUtil.isValidEmail(email)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"无效的邮箱");
        }

        //账户不能重复
        QueryWrapper<Employee> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("employee.email", email);
        long count = employeeMapper.selectCount(queryWrapper);
        if(count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱已存在");
        }

        //判断店铺是否存在
        QueryWrapper<Store> storeQuery = new QueryWrapper<>();
        storeQuery.eq("store.id", store);
        Store one = storeMapper.selectOne(storeQuery);
        if(one==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"未找到相关店铺");
        }
        //id设定
        String id = null;
        Integer maxSuffix = employeeMapper.findMaxIdByPrefix(store);
        if (maxSuffix == null) {
            // 如果不存在，则返回 prefix_1
            id =  store + "_1";
        } else {
            // 否则在当前编号基础上加 1
            int newSuffix = maxSuffix + 1;
            id =  store + "_" + newSuffix;
        }

        //员工起始密码均为123456 进行加密
        Employee employee = new Employee();
        String password = "123456";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());

        //是否重名
        QueryWrapper<Employee> nameWrapper = new QueryWrapper<>();
        nameWrapper.eq("store", store).eq("name", name);
        Long nameCount = employeeMapper.selectCount(nameWrapper);
        if(nameCount>0){
            name = name + "(" + nameCount + ")";
        }
        //偏好初始值设定
        String preference = "{\"workday\": {\"day\": [-1]}, \"working_hours\": {\"time\": [-1]}, \"shift_duration\": " +
                "{\"day\": -1, \"week\": -1}}";

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(preference);

        employee.setPreferenceValue(JSONObject.parse(json));
        employee.setName(name);
        employee.setEmail(email);
        employee.setPassword(encryptPassword);
        employee.setPosition(position);
        employee.setId(id);
        employee.setStore(store);

        boolean saveResult = this.save(employee);
        if(!saveResult){
            return null;
        }
        return employee.getId();
    }

//    @Override
//    public Boolean modifyEmployeePreferenceService(String id, Employee employee) throws JsonProcessingException {
//
//        Employee oldEmployee = this.getOne(new QueryWrapper<Employee>().eq("id", id));
//
//        //hashmap -> json
//        ObjectMapper objectMapper = new ObjectMapper();
//        String json = objectMapper.writeValueAsString(employee.getPreferenceValue());
//        oldEmployee.setPreferenceValue(json);
//
//        UpdateWrapper<Employee> updateWrapper = new UpdateWrapper<>();
//        updateWrapper.eq("id",id);
//        boolean result = this.update(oldEmployee,updateWrapper);
//
//        return result;
//    }
    public Boolean modifyEmployeePreferenceService(String id,  Object preference) throws JsonProcessingException {

        Employee oldEmployee = this.getOne(new QueryWrapper<Employee>().eq("id", id));

        //hashmap -> json
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(preference);
        oldEmployee.setPreferenceValue(json);

        UpdateWrapper<Employee> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id);
        boolean result = this.update(oldEmployee,updateWrapper);

        return result;
    }

    @Override
    public Employee employeeLogin(String email, String password, HttpServletRequest request) {
        if(StringUtils.isAnyBlank(email,password)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"存在空格");
        }
        if (password.length() < 6){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        }

        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        //查询用户是否存在
        QueryWrapper<Employee> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        queryWrapper.eq("password", encryptPassword);
        Employee employee = employeeMapper.selectOne(queryWrapper);
        //用户不存在
        if (employee == null) {
            //log.info("user login failed, account cannot match password");
            throw new BusinessException(ErrorCode.NULL_ERROR,"该用户不存在或密码错误");
        }

        Employee safetyEmployee = getSafeEmployee(employee);

        //4.记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE,employee);

        return safetyEmployee;
    }

    @Override
    public Employee getSafeEmployee(Employee originEmployee) {
        if (originEmployee == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"该用户不存在");
        }
        Employee safetyEmployee = new Employee();
        safetyEmployee.setId(originEmployee.getId());
        safetyEmployee.setName(originEmployee.getName());
        safetyEmployee.setEmail(originEmployee.getEmail());
        safetyEmployee.setPosition(originEmployee.getPosition());
        safetyEmployee.setStore(originEmployee.getStore());
        safetyEmployee.setPreferenceValue(originEmployee.getPreferenceValue());


        return safetyEmployee;
    }

    @Override
    public String getStoreByEmployeeId(String id) {
        QueryWrapper<Employee> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("employee.id", id);
        Employee employee = employeeMapper.selectOne(queryWrapper);
        String storeName = employee.getStore();

        QueryWrapper<Store> queryWrapperOfStore = new QueryWrapper<>();
        queryWrapperOfStore.eq("store.id", storeName);
        Store store = storeMapper.selectOne(queryWrapperOfStore);
        if(store == null){
            return null;
        }
        return store.getName();
    }

    @Override
    public List<Employee> getEmployeesByStore(String storeId) {
        QueryWrapper<Employee> queryWrapperEmployees = new QueryWrapper<>();
        queryWrapperEmployees.eq("employee.store", storeId);
        List<Employee> employeeList = employeeMapper.selectList(queryWrapperEmployees);
//        System.out.println("从数据库获取数据");
        if (employeeList  == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"为查询到数据");
        }
        return employeeList;
    }
}




