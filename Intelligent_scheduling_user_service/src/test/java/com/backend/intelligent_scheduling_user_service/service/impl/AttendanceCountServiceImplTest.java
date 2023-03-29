package com.backend.intelligent_scheduling_user_service.service.impl;

import com.backend.intelligent_scheduling_user_service.mapper.SchedulingMapper;
import com.backend.intelligent_scheduling_user_service.mapper.StoreMapper;
import com.backend.intelligent_scheduling_user_service.model.Scheduling;
import com.backend.intelligent_scheduling_user_service.model.Store;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


@SpringBootTest
class AttendanceCountServiceImplTest {

    @Autowired
    private StoreMapper storeMapper;
    @Resource
    private SchedulingMapper schedulingMapper;
    @Test
    void updateAttendanceScheduling() {
        java.sql.Date sqlDate = java.sql.Date.valueOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        System.out.println(sqlDate);
        //获取店铺
        QueryWrapper<Store> storeQueryWrapper = new QueryWrapper<>();
        storeQueryWrapper.select("id");
        List<Store> stores = storeMapper.selectList(storeQueryWrapper);
        System.out.println(stores);

        java.sql.Date sql = java.sql.Date.valueOf("2023-05-10");

        QueryWrapper<Scheduling> schedulingQuery = new QueryWrapper<>();
        schedulingQuery.eq("id", "1_1").eq("date", sql);
        Scheduling scheduling = schedulingMapper.selectOne(schedulingQuery);
        System.out.println(scheduling);
    }
}