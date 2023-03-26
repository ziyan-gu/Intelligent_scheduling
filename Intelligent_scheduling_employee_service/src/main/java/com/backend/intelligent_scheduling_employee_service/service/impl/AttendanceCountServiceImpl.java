package com.backend.intelligent_scheduling_employee_service.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.backend.intelligent_scheduling_employee_service.common.ErrorCode;
import com.backend.intelligent_scheduling_employee_service.exception.BusinessException;
import com.backend.intelligent_scheduling_employee_service.mapper.SchedulingMapper;
import com.backend.intelligent_scheduling_employee_service.model.Employee;
import com.backend.intelligent_scheduling_employee_service.model.Scheduling;
import com.backend.intelligent_scheduling_employee_service.service.EmployeeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.backend.intelligent_scheduling_employee_service.model.AttendanceCount;
import com.backend.intelligent_scheduling_employee_service.service.AttendanceCountService;
import com.backend.intelligent_scheduling_employee_service.mapper.AttendanceCountMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author 86136
* @description 针对表【attendance_count】的数据库操作Service实现
* @createDate 2023-03-26 00:17:26
*/
@Service
public class AttendanceCountServiceImpl extends ServiceImpl<AttendanceCountMapper, AttendanceCount>
    implements AttendanceCountService{

    @Resource
    private AttendanceCountMapper attendanceCountMapper;


    @Resource
    private EmployeeService employeeService;

    @Override
    public int getCount(String id) {

        QueryWrapper<AttendanceCount> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        AttendanceCount attendanceCount = attendanceCountMapper.selectOne(wrapper);
        if(attendanceCount == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"未查询到此员工出勤记录");
        }
        int count = attendanceCount.getCount();
        return count;
    }

    @Override
    public boolean addCount(String id) {
        final int addCount = 1;
        QueryWrapper<AttendanceCount> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        AttendanceCount attendanceCount = attendanceCountMapper.selectOne(wrapper);
        attendanceCount.setCount(attendanceCount.getCount() + addCount);

        int update = attendanceCountMapper.update(attendanceCount, wrapper);
        if(update == 0){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"增加失败");
        }
        return true;
    }

//    @Override
//    public void updateAttendanceCount(Scheduling scheduling) {
//        Map<String, Integer> attendanceMap = new HashMap<>();
//        List<Employee> employees = employeeService.list();
//        for (Employee employee : employees) {
//            attendanceMap.put(employee.getId(), 0);
//        }
//        JSONObject data = (JSONObject) scheduling.getData();
//        if (data != null) {
//            for (String key : data.keySet()) {
//                if (!"total".equals(key)) {
//                    JSONArray array = data.getJSONArray(key);
//                    String employeeId = array.getString(2);
//                    if (!"0".equals(employeeId)) {
//                        Integer attendanceCount = attendanceMap.get(employeeId);
//                        if (attendanceCount != null) {
//                            attendanceMap.put(employeeId, attendanceCount + 1);
//                        }
//                    }
//                }
//            }
//        }
//        for (String employeeId : attendanceMap.keySet()) {
//            Integer attendanceCount = attendanceMap.get(employeeId);
//            this.updateAttendanceCount(employeeId, attendanceCount);
//        }
//    }
//
//    private void updateAttendanceCount(String id, int count) {
//        QueryWrapper<AttendanceCount> wrapper = new QueryWrapper<>();
//        wrapper.eq("id", id);
//        AttendanceCount attendanceCount = attendanceCountMapper.selectOne(wrapper);
//        attendanceCount.setCount(attendanceCount.getCount() + count);
//
//        attendanceCountMapper.update(attendanceCount, wrapper);
//    }
}




