package com.backend.intelligent_scheduling_user_service.mapper;

import com.backend.intelligent_scheduling_user_service.model.PassengerFlow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 86136
* @description 针对表【passenger_flow】的数据库操作Mapper
* @createDate 2023-03-25 21:45:01
* @Entity com.backend.intelligent_scheduling_user_service.model.PassengerFlow
*/
@Mapper
public interface PassengerFlowMapper extends BaseMapper<PassengerFlow> {

}




