package com.backend.intelligent_scheduling_user_service.mapper;

import com.backend.intelligent_scheduling_user_service.model.AttendanceCount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 86136
* @description 针对表【attendance_count】的数据库操作Mapper
* @createDate 2023-03-26 15:22:12
* @Entity com.backend.intelligent_scheduling_user_service.model.AttendanceCount
*/
@Mapper
public interface AttendanceCountMapper extends BaseMapper<AttendanceCount> {

}




