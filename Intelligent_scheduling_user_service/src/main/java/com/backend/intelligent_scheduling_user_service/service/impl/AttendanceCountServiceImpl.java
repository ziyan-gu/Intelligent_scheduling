package com.backend.intelligent_scheduling_user_service.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.backend.intelligent_scheduling_user_service.common.ErrorCode;
import com.backend.intelligent_scheduling_user_service.exception.BusinessException;
import com.backend.intelligent_scheduling_user_service.mapper.SchedulingMapper;
import com.backend.intelligent_scheduling_user_service.mapper.StoreMapper;
import com.backend.intelligent_scheduling_user_service.model.Employee;
import com.backend.intelligent_scheduling_user_service.model.Scheduling;
import com.backend.intelligent_scheduling_user_service.model.Store;
import com.backend.intelligent_scheduling_user_service.service.EmployeeService;
import com.backend.intelligent_scheduling_user_service.service.StoreService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.backend.intelligent_scheduling_user_service.model.AttendanceCount;
import com.backend.intelligent_scheduling_user_service.service.AttendanceCountService;
import com.backend.intelligent_scheduling_user_service.mapper.AttendanceCountMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author 86136
* @description 针对表【attendance_count】的数据库操作Service实现
* @createDate 2023-03-26 15:22:12
*/
@Service
public class AttendanceCountServiceImpl extends ServiceImpl<AttendanceCountMapper, AttendanceCount>
    implements AttendanceCountService{
    @Resource
    private AttendanceCountMapper attendanceCountMapper;

    @Resource
    private SchedulingMapper schedulingMapper;

    @Resource
    private EmployeeService employeeService;

    @Resource
    private StoreMapper storeMapper;

    @Override
    public void updateAttendanceCount(Scheduling scheduling){
        Map<String, Integer> attendanceMap = new HashMap<>();
        QueryWrapper<Employee> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("store",scheduling.getId());
        List<Employee> employees = employeeService.list(queryWrapper);
        for (Employee employee : employees) {
            attendanceMap.put(employee.getId(), 0);
        }

        Object jsonData = scheduling.getData();
        JSONObject data = JSON.parseObject((String) jsonData);


        if (data != null) {
            for (String key : data.keySet()) {
                if (!"total".equals(key)) {
                    JSONArray array = data.getJSONArray(key);
                    String employeeId = array.getString(2);
                    if (!"0".equals(employeeId)) {
                        Integer attendanceCount = attendanceMap.get(employeeId);
                        if (attendanceCount != null) {
                            attendanceMap.put(employeeId, attendanceCount + 1);
                        }
                    }
                }
            }
        }

        for (String employeeId : attendanceMap.keySet()) {
            Integer attendanceCount = attendanceMap.get(employeeId);
            if (attendanceCount == 0) {
                continue;
            }
            try {
                this.updateAttendanceCount(employeeId, attendanceCount);
            } catch (BusinessException e) {
                // 处理异常
                e.printStackTrace();
            }
        }
    }


    public void updateAttendanceCount(String id, int count) {
        QueryWrapper<AttendanceCount> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        AttendanceCount qAttendanceCount = attendanceCountMapper.selectOne(wrapper);
        if (qAttendanceCount == null) {
            AttendanceCount newAttendanceCount = new AttendanceCount();
            newAttendanceCount.setId(id);
            newAttendanceCount.setCount(count);
            boolean save = this.save(newAttendanceCount);
            if (save) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,id + "Not found"+",already added");
            }
            throw new BusinessException(ErrorCode.PARAMS_ERROR,id + "Not found"+",not added");
        }

        //更新数据
        UpdateWrapper<AttendanceCount> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        AttendanceCount attendanceCount = new AttendanceCount();
        attendanceCount.setCount(qAttendanceCount.getCount() + count);

        int update = attendanceCountMapper.update(attendanceCount, updateWrapper);
        if(update != 0) {
            System.out.println(id + " Updated Attendance");
        }
    }

    @Override
    @Scheduled(cron = "0 30 23 * * ?")
    public void updateAttendanceScheduling() {
//        System.out.println("我进来了");
        //获取当日时间
        java.sql.Date sqlDate = java.sql.Date.valueOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        //获取店铺
        QueryWrapper<Store> storeQueryWrapper = new QueryWrapper<>();
        storeQueryWrapper.select("id");
        List<Store> stores = storeMapper.selectList(storeQueryWrapper);

        List<String> storeId = new ArrayList<>();
        for (Store store : stores) {
            storeId.add(store.getId());
        }
        System.out.println(storeId);

        for(String id : storeId) {
//            System.out.println("进入每个店铺统计");
            //获取员工列表
            QueryWrapper<Employee> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("store", id);
            List<Employee> employees = employeeService.list(queryWrapper);
            if (employees.size() == 0) {
                continue;
            }

//            System.out.println("获取当日排班了");
            //获取当日排班
            QueryWrapper<Scheduling> schedulingQuery = new QueryWrapper<>();
            schedulingQuery.eq("id", id).eq("date", sqlDate);
            Scheduling scheduling = schedulingMapper.selectOne(schedulingQuery);
            if(scheduling == null) {
//                System.out.println("该日无排班");
                continue;
            }
            Object jsonData = scheduling.getData();
            JSONObject data = JSON.parseObject((String) jsonData);

            //创建匹配容器
            Map<String, Integer> attendanceMap = new HashMap<>();

            for (Employee employee : employees) {
                attendanceMap.put(employee.getId(), 0);
            }

            if (data != null) {
                for (String key : data.keySet()) {
                    if (!"total".equals(key)) {
                        JSONArray array = data.getJSONArray(key);
                        String employeeId = array.getString(2);
                        if (!"0".equals(employeeId)) {
                            Integer attendanceCount = attendanceMap.get(employeeId);
                            if (attendanceCount != null) {
                                attendanceMap.put(employeeId, attendanceCount + 1);
                            }
                        }
                    }
                }
            }
            for (String employeeId : attendanceMap.keySet()) {
                Integer attendanceCount = attendanceMap.get(employeeId);
                if (attendanceCount == 0) {
                    continue;
                }
                try {
                    this.updateAttendanceCount(employeeId, attendanceCount);
                } catch (BusinessException e) {
                    // 处理异常
                    e.printStackTrace();
                }
            }
        }
    }
}




