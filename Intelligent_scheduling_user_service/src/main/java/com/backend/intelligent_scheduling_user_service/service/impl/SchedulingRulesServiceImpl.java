package com.backend.intelligent_scheduling_user_service.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.backend.intelligent_scheduling_user_service.common.ErrorCode;
import com.backend.intelligent_scheduling_user_service.exception.BusinessException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.backend.intelligent_scheduling_user_service.model.SchedulingRules;
import com.backend.intelligent_scheduling_user_service.service.SchedulingRulesService;
import com.backend.intelligent_scheduling_user_service.mapper.SchedulingRulesMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author 86136
* @description 针对表【scheduling_rules】的数据库操作Service实现
* @createDate 2023-03-22 19:54:25
*/
@Service
public class SchedulingRulesServiceImpl extends ServiceImpl<SchedulingRulesMapper, SchedulingRules>
    implements SchedulingRulesService{

    @Resource
    private SchedulingRulesMapper schedulingRulesMapper;
    @Override
    public void setDefaultValueScheduling(String store) throws JsonProcessingException {
        SchedulingRules open = new SchedulingRules("open");
        SchedulingRules close = new SchedulingRules("close");
        SchedulingRules flow = new SchedulingRules("flow");
        SchedulingRules on_duty = new SchedulingRules("on_duty");
        SchedulingRules cashier = new SchedulingRules("cashier");

        ObjectMapper objectMapper = new ObjectMapper();

//        open.setRuleType("open");
        open.setStore(store);
        String vOpen = "{\"equ\": 100, \"pre\": 1, \"type\": [1, 2, 3, 5]}";
        vOpen = objectMapper.writeValueAsString(vOpen);
        open.setRuleValue(JSONObject.parse(vOpen));

//        close.setRuleType("close");
        close.setStore(store);
        String vClose = "{\"end\": 2, \"equ\": [80, 1], \"type\": [1, 2, 6]}";
        vClose = objectMapper.writeValueAsString(vClose);
        close.setRuleValue(JSONObject.parse(vClose));

//        flow.setRuleType("flow");
        flow.setStore(store);
        String vFlow = "{\"pre\": 3.8, \"type\": [0]}";
        vFlow = objectMapper.writeValueAsString(vFlow);
        flow.setRuleValue(JSONObject.parse(vFlow));

//        on_duty.setRuleType("on_duty");
        on_duty.setStore(store);
        String vOnDuty = "{\"num\": 1, \"type\": [4]}";
        vOnDuty = objectMapper.writeValueAsString(vOnDuty);
        on_duty.setRuleValue(JSONObject.parse(vOnDuty));

//        cashier.setRuleType("cashier");
        cashier.setStore(store);
        String vCashier = "{\"num\": 1, \"type\": [4]}";
        vCashier = objectMapper.writeValueAsString(vCashier);
        cashier.setRuleValue(JSONObject.parse(vCashier));

        boolean save = this.save(open);
        boolean save1 = this.save(close);
        boolean save2 = this.save(flow);
        boolean save3 = this.save(on_duty);
        boolean save4 = this.save(cashier);
        if(!save || !save1 || !save2 || !save3 || !save4) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"排班默认值设置失败");
        }


    }
}




