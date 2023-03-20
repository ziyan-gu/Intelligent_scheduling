package com.backend.intelligent_scheduling_login.mapper;

import com.backend.intelligent_scheduling_login.model.Scheduling;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 86136
* @description 针对表【scheduling】的数据库操作Mapper
* @createDate 2023-03-20 23:27:05
* @Entity com.backend.intelligent_scheduling_login.model.Scheduling
*/


@Mapper
public interface SchedulingMapper extends BaseMapper<Scheduling> {
}




