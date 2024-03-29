package com.backend.intelligent_scheduling_user_service.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.backend.intelligent_scheduling_user_service.common.ErrorCode;
import com.backend.intelligent_scheduling_user_service.exception.BusinessException;
import com.backend.intelligent_scheduling_user_service.model.request.ModifyFixRulesRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.backend.intelligent_scheduling_user_service.model.FixedRules;
import com.backend.intelligent_scheduling_user_service.service.FixedRulesService;
import com.backend.intelligent_scheduling_user_service.mapper.FixedRulesMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.List;

/**
* @author 86136
* @description 针对表【fixed_rules】的数据库操作Service实现
* @createDate 2023-03-22 19:54:22
*/
@Service
public class FixedRulesServiceImpl extends ServiceImpl<FixedRulesMapper, FixedRules>
    implements FixedRulesService{

    @Resource
    private FixedRulesMapper fixedRulesMapper;
    @Override
    public void setDefaultValueFixed(String admin) throws JsonProcessingException {
        FixedRules business_hours = new FixedRules();
        FixedRules working_hours = new FixedRules();
        FixedRules rest_time = new FixedRules();

        business_hours.setRuleType("business_hours");
        business_hours.setAdmin(admin);
        String business = "{\"dayoff\": [10, 22], \"workingday\": [9, 21]}";
        ObjectMapper objectMapper = new ObjectMapper();
        business = objectMapper.writeValueAsString(business);
        business_hours.setRuleValue(JSONObject.parse(business));

        working_hours.setRuleType("working_hours");
        working_hours.setAdmin(admin);
        String working = "{\"a\": 40, \"b\": 8, \"c\": [2, 4], \"d\": 4}";
        working = objectMapper.writeValueAsString(working);
        working_hours.setRuleValue(JSONObject.parse(working));

        rest_time.setRuleType("rest_time");
        rest_time.setAdmin(admin);
        String rest = "{\"rest\": 0.5, \"lunch\": [11, 14, 0.5], \"dinner\": [17, 20, 0.5]}";
        rest = objectMapper.writeValueAsString(rest);
        rest_time.setRuleValue(JSONObject.parse(rest));

        boolean save = this.save(business_hours);
        boolean save1 = this.save(working_hours);
        boolean save2 = this.save(rest_time);
        if(!save || !save1 || !save2) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"排班默认值设置失败");
        }
//        int insert1 = fixedRulesMapper.insert(business_hours);
//        int insert2 = fixedRulesMapper.insert(working_hours);
//        int insert3 = fixedRulesMapper.insert(rest_time);
    }

    @Override
    public List<FixedRules> getFixedRules(String admin){
        QueryWrapper<FixedRules> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("admin", admin);
        List<FixedRules> fixedRulesList = fixedRulesMapper.selectList(objectQueryWrapper);
        if(fixedRulesList == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"未查询到数据");
        }
        return fixedRulesList;
    }

    @Override
    public boolean ModifyFixRules(List<ModifyFixRulesRequest> ModifyFixRulesRequest) throws JsonProcessingException {
        int count = 0;
        for (ModifyFixRulesRequest rule : ModifyFixRulesRequest) {
            UpdateWrapper<FixedRules> wrapper = new UpdateWrapper<>();
            wrapper.eq("rule_type", rule.getRuleType())
                    .eq("admin", rule.getAdmin());

            ObjectMapper objectMapper = new ObjectMapper();
            String value = objectMapper.writeValueAsString((rule.getRuleValue()));
            FixedRules updatedRule = new FixedRules();
            updatedRule.setRuleValue(value);

            int rows = fixedRulesMapper.update(updatedRule, wrapper);
            count = count + rows;
        }

//        for (ModifyFixRulesRequest modifyFixRulesRequest : ModifyFixRulesRequest) {
//            String ruleType = modifyFixRulesRequest.getRuleType();
//            String admin = modifyFixRulesRequest.getAdmin();
//            Object ruleValue = modifyFixRulesRequest.getRuleValue();
//            String jsonStr = JSON.toJSONString(ruleValue);
//            byte[] jsonBytes = jsonStr.getBytes();
//            fixedRulesMapper.update(null,
//                    Wrappers.<FixedRules>lambdaUpdate()
//                            .set(FixedRules::getRuleValue, jsonBytes)
//                            .eq(FixedRules::getRuleType, ruleType)
//                            .eq(FixedRules::getAdmin, admin));
//        }
        if(count == 0){
            return false;
        }
        return true;

    }
}




