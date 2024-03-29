package com.backend.intelligent_scheduling_user_service.service;

import com.backend.intelligent_scheduling_user_service.model.FixedRules;
import com.backend.intelligent_scheduling_user_service.model.request.ModifyFixRulesRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

/**
 * @author 86136
 * @description 针对表【fixed_rules】的数据库操作Service
 * @createDate 2023-03-22 19:54:22
 */
public interface FixedRulesService extends IService<FixedRules> {


    void setDefaultValueFixed(String admin) throws JsonProcessingException;

    List<FixedRules> getFixedRules(String admin);

    boolean ModifyFixRules(List<ModifyFixRulesRequest> modifyFixRulesRequests) throws JsonProcessingException;
}
