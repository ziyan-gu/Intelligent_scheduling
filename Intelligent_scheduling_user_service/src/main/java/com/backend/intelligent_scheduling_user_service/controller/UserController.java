package com.backend.intelligent_scheduling_user_service.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.nacos.common.utils.StringUtils;
import com.backend.intelligent_scheduling_user_service.common.BaseResponse;
import com.backend.intelligent_scheduling_user_service.common.ErrorCode;
import com.backend.intelligent_scheduling_user_service.common.ResultUtils;
import com.backend.intelligent_scheduling_user_service.exception.BusinessException;
import com.backend.intelligent_scheduling_user_service.feign.EmployeeFeign;
import com.backend.intelligent_scheduling_user_service.feign.OrderFeign;
import com.backend.intelligent_scheduling_user_service.model.FixedRules;
import com.backend.intelligent_scheduling_user_service.model.PassengerFlow;
import com.backend.intelligent_scheduling_user_service.model.SchedulingRules;
import com.backend.intelligent_scheduling_user_service.model.User;
import com.backend.intelligent_scheduling_user_service.model.request.*;
import com.backend.intelligent_scheduling_user_service.model.response.*;
import com.backend.intelligent_scheduling_user_service.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController implements Serializable {

    @Autowired
    public UserService userService;

    @Autowired
    public EmployeeFeign employeeFeign;
    @Autowired
    public OrderFeign orderFeign;

    @Autowired
    public PassengerFlowService passengerFlowService;
    @Resource
    public FixedRulesService fixedRulesService;
    @Autowired
    public SchedulingService schedulingService;

    @Autowired
    public AttendanceCountService attendanceCountService;

    @Autowired
    public SchedulingRulesService schedulingRulesService;


    @ApiOperation("用户注册")
    @PostMapping("/register")
    public BaseResponse<String> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
//            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        String account = userRegisterRequest.getAccount();
        String password = userRegisterRequest.getPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String name = userRegisterRequest.getName();
        if (StringUtils.isAnyBlank(account, password, checkPassword, name)) {
            return null;
        }
        String result = userService.userRegister(account, password, checkPassword,name);

        return ResultUtils.success(result);
    }

    @ApiOperation("用户员工一起的登录(Web端使用)")
    @PostMapping("/logins")
    public BaseResponse<Object> userLogins(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"请求数据为空");
        }
        String account = userLoginRequest.getAccount();
        String password = userLoginRequest.getPassword();

        if (StringUtils.isAnyBlank(account,password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"输入存在空格");
        }

        Identify identify = new Identify();

        if (userService.isUser(account, password)) {
            // User身份验证通过，返回成功响应
            User user = userService.userLogin(account, password,request);
            if(user.getType().equals("admin")){
                identify.setToken("admin-token");
                String jsonIdentify = JSON.toJSONString(identify, SerializerFeature.WriteSlashAsSpecial);
                return ResultUtils.success(JSONObject.parse(jsonIdentify));
            }
            if(user.getType().equals("store")){
                identify.setToken("editor-token");
                String jsonIdentify = JSON.toJSONString(identify, SerializerFeature.WriteSlashAsSpecial);
                return ResultUtils.success(JSONObject.parse(jsonIdentify));
            }
        } else if (userService.isEmployee(account, password)) {
            // Employee身份验证通过，返回成功响应
            identify.setToken("visitor-token");
            String jsonIdentify = JSON.toJSONString(identify, SerializerFeature.WriteSlashAsSpecial);
            return ResultUtils.success(JSONObject.parse(jsonIdentify));
        } else {
            // 身份验证失败，返回错误响应
            throw new BusinessException(ErrorCode.NULL_ERROR,"用户名不存在或密码错误");
        }
        return ResultUtils.success("ok");
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"请求数据为空");
        }
        String account = userLoginRequest.getAccount();
        String password = userLoginRequest.getPassword();

        if (StringUtils.isAnyBlank(account,password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"输入存在空格");
        }
        User user = userService.userLogin(account, password, request);
        if(user == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"未查询到数据");
        }
        return ResultUtils.success(user);

    }
    @ApiOperation("用户改密")
    @PostMapping("/changePassword")
    public BaseResponse<String> changePasswordByAccount(@RequestBody ChangePasswordRequest changePasswordRequest){
        if (changePasswordRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"请求数据为空");
        }
        String account = changePasswordRequest.getAccount();
        String password = changePasswordRequest.getPassword();
        String newPassword = changePasswordRequest.getNewPassword();

        if(newPassword.length() < 6)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"修改密码过短");
        }
        if (StringUtils.isAnyBlank(account,password,newPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"输入存在空格");
        }
        Integer result = userService.changePassword(account, password, newPassword);
        if (result == 0){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"修改失败");
        }
        return ResultUtils.success("ok");
    }

    @ApiOperation("管理员增添门店")
    @PostMapping("/addStore")
    @CacheEvict(value = "storeList", allEntries = true)
    public BaseResponse<String> addStoreByUser(@RequestBody UserAddStoreRequest userAddStoreRequest) {
        if (userAddStoreRequest == null) {
//            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        String account = userAddStoreRequest.getAccount();
        String name = userAddStoreRequest.getName();
        Float size = userAddStoreRequest.getSize();
        String address = userAddStoreRequest.getAddress();
        String company = userAddStoreRequest.getCompany();
        if (StringUtils.isAnyBlank(account, name)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        String result = userService.addStore(account, name, company, address, size);

        return ResultUtils.success(result);
    }

    @ApiOperation("根据token获取用户信息(Web端登录使用)")
    @GetMapping("info/{token}")
    public BaseResponse<LoginInfo> loginGetInfo(@PathVariable String token) {
        if (token == null || StringUtils.isAnyBlank(token)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }

        LoginInfo loginInfo = new LoginInfo();
        token = token.substring(0, token.indexOf("-")).replace("-", "");
        String[] token_out = new String[1];
        token_out[0] = token;
        loginInfo.setRoles(token_out);
        loginInfo.setAvatar("https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
        loginInfo.setName("");

        return ResultUtils.success(loginInfo);
    }

//    @ApiOperation("获取排班（根据id和data）")
//    @GetMapping("/getScheduling/{id}/and/{date}")
//    public BaseResponse<Object> getSchedulingByDay(@PathVariable("id") String id,
//                                                   @PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
//        if (id == null || date == null || StringUtils.isAnyBlank(id)) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
//        }
//
//        Object object = scheduling.getScheduleByIdAndDate(id, date);
//
//        return ResultUtils.success(object);
//    }

    @ApiOperation("获取排班（根据id和data）")
    @GetMapping("/getScheduling/{id}/and/{date}")
    public BaseResponse<Object> getSchedulingByDay(@PathVariable("id") String id,
                                                   @PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") String date) throws ParseException {
        if (id == null || date == null || StringUtils.isAnyBlank(id)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date newDate = dateFormat.parse(date);
        java.sql.Date sqlDate = new java.sql.Date(newDate.getTime());

        Object object = schedulingService.getScheduleByIdAndDate(id, sqlDate);
        return ResultUtils.success(JSONObject.parse(object.toString()));
    }


    @ApiOperation("获取排班（根据id，返回date和data）")
    @GetMapping("/getSchedulingById/{id}")
//    @Cacheable(value = "scheduling", key = "#id")
    public BaseResponse<List<GetSchedulingByIdResponse>> getSchedulingByID(@PathVariable("id") String id) throws ParseException {
        if (id == null|| StringUtils.isAnyBlank(id)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }

        List<GetSchedulingByIdResponse> getSchedulingByIdResponse = schedulingService.getScheduleById(id);
        if (getSchedulingByIdResponse == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"获取内容为空");
        }
        return ResultUtils.success(getSchedulingByIdResponse);
    }

    @ApiOperation("根据店铺id,一键生成排班（如果出现连接超时：备选http://ip:8432/getScheduling/1_1）")
    @GetMapping("/generateScheduling/{id}")
    public BaseResponse<String> getSchedulingById(@PathVariable("id") String id){
        if (id == null || StringUtils.isAnyBlank(id)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        Object scheduling = orderFeign.getScheduling(id);
        if (scheduling == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"貌似没有排班成功");
        }
//        return ResultUtils.success(JSONObject.parse(scheduling.toString()));
        return ResultUtils.success("OK");
    }

    @ApiOperation("管理员获取固定排班规则(根据管理员ID)")
    @GetMapping("/getFixedScheduling/{admin}")
    public BaseResponse<List<Object>> getFixedScheduling(@PathVariable("admin") String admin){
        if (admin == null || StringUtils.isAnyBlank(admin)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        List<FixedRules> fixedRules = fixedRulesService.getFixedRules(admin);
        fixedRules.forEach(fixedRule -> fixedRule.setRuleValue(JSONObject.parse((String) fixedRule.getRuleValue())));
        List<Object> fixedScheduling = new ArrayList<>();
        fixedRules.forEach(fixedRule -> fixedScheduling.add(fixedRule.getRuleValue()));

        return ResultUtils.success(fixedScheduling);
    }

    @ApiOperation("管理员修改固定排班规则")
    @PostMapping("/modifyFixedScheduling")
    public BaseResponse<String> modifyFixedScheduling(@RequestBody List<ModifyFixRulesRequest> modifyFixRulesRequests) throws JsonProcessingException {
        if (modifyFixRulesRequests == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        boolean result = fixedRulesService.ModifyFixRules(modifyFixRulesRequests);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"修改失败");
        }
        return ResultUtils.success("ok");
    }

    @ApiOperation("获取排班规则(根据商店ID)")
    @GetMapping("/getSchedulingRules/{id}")
    public BaseResponse<List<Object>> getSchedulingRules(@PathVariable("id") String id){
        if (id == null || StringUtils.isAnyBlank(id)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        List<SchedulingRules> schedulingRules = schedulingRulesService.getSchedulingRules(id);
        schedulingRules.forEach(schedulingRule -> schedulingRule.setRuleValue(JSONObject.parse((String) schedulingRule.getRuleValue())));
        List<Object> SchedulingRulesResponse = new ArrayList<>();
        schedulingRules.forEach(schedulingRule -> SchedulingRulesResponse.add(schedulingRule.getRuleValue()));

        return ResultUtils.success(SchedulingRulesResponse);
    }

    @ApiOperation("修改排班规则")
    @PostMapping("/modifySchedulingRules")
    public BaseResponse<String> modifySchedulingRules(@RequestBody List<ModifySchedulingRulesRequest> modifySchedulingRulesRequests) throws JsonProcessingException {
        if (modifySchedulingRulesRequests == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        boolean result = schedulingRulesService.ModifySchedulingRules(modifySchedulingRulesRequests);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"修改失败");
        }
        return ResultUtils.success("ok");
    }

    @ApiOperation("修改排班（根据ID和日期和排班）")
    @PutMapping("/changeScheduling/{id}/and/{date}")
    public BaseResponse<String> changeScheduling(@PathVariable(value = "id") String id,
                                                 @PathVariable(value = "date") @DateTimeFormat(pattern =
            "yyyy-MM-dd")String date, @RequestBody GetSchedulingData getSchedulingData) throws ParseException, JsonProcessingException {
        if (id == null || date == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date newDate = dateFormat.parse(date);
        java.sql.Date sqlDate = new java.sql.Date(newDate.getTime());


        String result = schedulingService.changeScheduleByIdAndDate(id, sqlDate, getSchedulingData.getData());
        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"修改失败");
        }
        return ResultUtils.success(result);
    }

    @ApiOperation("获取客流量(根据店铺id和日期)")
    @GetMapping("/getPassengerFlow/{id}/and/{date}")
    @Cacheable(value = "PassengerFlow", key = "#id + '_' + #date")
    public BaseResponse<Object> getPassengerFlow(@PathVariable("id") String id,
                                                       @PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") String date) throws ParseException {
        if (id == null || StringUtils.isAnyBlank(id) || date == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date newDate = simpleDateFormat.parse(date);
        java.sql.Date sqlDate = new java.sql.Date(newDate.getTime());

        Object passengerFlow = passengerFlowService.getPassengerFlow(id, sqlDate);

        return ResultUtils.success(passengerFlow);
    }

    @ApiOperation("添加客流量(根据id，日期，客流量)")
    @PostMapping("/setPassengerFlow")
//    @CacheEvict(value = "PassengerFlow", allEntries = true)
    public BaseResponse<String> setPassengerFlow(@RequestBody PassengerFlow passengerFlow) throws JsonProcessingException {
        if (passengerFlow == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        String id = passengerFlow.getId();
        Date date = passengerFlow.getDate();
        Object data = passengerFlow.getData();

        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        boolean result = passengerFlowService.setPassengerFlow(id, sqlDate, data);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"添加失败");
        }
        return ResultUtils.success("ok");
    }



    @ApiOperation("根据店铺id获取员工的出勤（返回员工id，姓名，出勤次数）")
    @GetMapping("/getAttendByStoreId/{storeId}")
    public BaseResponse<List<GetAttendResponse>> getAttendByStoreId(@PathVariable String storeId){
        if (storeId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if (StringUtils.isAnyBlank(storeId)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数存在空格或格式不正确");
        }
        List<GetAttendResponse> attendanceList = attendanceCountService.getAttendancesByStore(storeId);
        if (attendanceList == null)
        {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"获取的出勤表为空");
        }
        return ResultUtils.success(attendanceList);
    }

    @ApiOperation("获取排班处理数据版本（根据id，返回date和data）")
    @GetMapping("/getProcessedSchedulingById/{id}")
//    @Cacheable(value = "processedScheduling", key = "#id")
    public BaseResponse<List<GetAllProcessedLayoutResponse>> getProcessedSchedulingByID(@PathVariable("id") String id) throws ParseException, IOException {
        if (id == null|| StringUtils.isAnyBlank(id)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }

        List<GetAllProcessedLayoutResponse> schedulingList = schedulingService.getProcessedScheduleById(id);

        if (schedulingList == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"获取内容为空");
        }
        return ResultUtils.success(schedulingList);
    }
    @ApiOperation("获取各店铺当天的客流量和（传参: id,date")
    @GetMapping("/getAllPassengerFlowSum/{id}/and/{date}")
    public BaseResponse<List<GetPassengerFlowSum>> getAllPassengerFlowSum(@PathVariable("id") String id,
                                                                                    @PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") String date) throws ParseException, IOException {
        if (id == null|| StringUtils.isAnyBlank(id) || date == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date newDate = simpleDateFormat.parse(date);
        java.sql.Date sqlDate = new java.sql.Date(newDate.getTime());

        List<GetPassengerFlowSum> passengerFlowSum = passengerFlowService.getPassengerFlowSum(id, sqlDate);
        if (passengerFlowSum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"获取内容为空");
        }
        return ResultUtils.success(passengerFlowSum);
    }
}
