package com.backend.intelligent_scheduling_employee_service.service.impl;

import com.backend.intelligent_scheduling_employee_service.common.ErrorCode;
import com.backend.intelligent_scheduling_employee_service.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.backend.intelligent_scheduling_employee_service.model.AttendanceCount;
import com.backend.intelligent_scheduling_employee_service.service.AttendanceCountService;
import com.backend.intelligent_scheduling_employee_service.mapper.AttendanceCountMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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

}




