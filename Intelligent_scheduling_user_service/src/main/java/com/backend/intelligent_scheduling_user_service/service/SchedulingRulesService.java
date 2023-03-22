package com.backend.intelligent_scheduling_user_service.service;

import com.backend.intelligent_scheduling_user_service.model.SchedulingRules;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
* @author 86136
* @description 针对表【scheduling_rules】的数据库操作Service
* @createDate 2023-03-22 19:54:25
*/
public interface SchedulingRulesService extends IService<SchedulingRules> {

    void setDefaultValueScheduling(String store) throws JsonProcessingException;
}
