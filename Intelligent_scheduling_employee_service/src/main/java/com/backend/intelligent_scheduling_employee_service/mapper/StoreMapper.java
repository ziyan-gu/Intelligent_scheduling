package com.backend.intelligent_scheduling_employee_service.mapper;

import com.backend.intelligent_scheduling_employee_service.model.Store;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 86136
* @description 针对表【store】的数据库操作Mapper
* @createDate 2023-03-01 23:34:59
* @Entity com.backend.intelligent_scheduling_employee_service.model.Store
*/

@Mapper
public interface StoreMapper extends BaseMapper<Store> {

}




