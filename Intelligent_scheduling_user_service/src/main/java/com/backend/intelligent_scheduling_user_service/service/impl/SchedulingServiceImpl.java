package com.backend.intelligent_scheduling_user_service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.backend.intelligent_scheduling_user_service.model.Scheduling;
import com.backend.intelligent_scheduling_user_service.service.SchedulingService;
import com.backend.intelligent_scheduling_user_service.mapper.SchedulingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;

/**
* @author 86136
* @description 针对表【scheduling】的数据库操作Service实现
* @createDate 2023-03-20 23:27:05
*/
@Service
public class SchedulingServiceImpl extends ServiceImpl<SchedulingMapper, Scheduling>
    implements SchedulingService{

    @Autowired
    private SchedulingMapper schedulingMapper;
    @Override
    public Object getScheduleByIdAndDate(String id, Date date) {
        QueryWrapper<Scheduling> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("date", date);
        Scheduling scheduling = schedulingMapper.selectOne(wrapper);
        return scheduling.getData();
    }
}




