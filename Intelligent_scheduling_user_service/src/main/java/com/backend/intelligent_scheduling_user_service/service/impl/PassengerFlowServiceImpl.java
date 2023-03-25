package com.backend.intelligent_scheduling_user_service.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.backend.intelligent_scheduling_user_service.common.ErrorCode;
import com.backend.intelligent_scheduling_user_service.exception.BusinessException;
import com.backend.intelligent_scheduling_user_service.service.PassengerFlowService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.backend.intelligent_scheduling_user_service.model.PassengerFlow;
import com.backend.intelligent_scheduling_user_service.mapper.PassengerFlowMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Date;

/**
* @author 86136
* @description 针对表【passenger_flow】的数据库操作Service实现
* @createDate 2023-03-25 21:45:01
*/
@Service
public class PassengerFlowServiceImpl extends ServiceImpl<PassengerFlowMapper, PassengerFlow>
    implements PassengerFlowService {

    @Resource
    private PassengerFlowMapper passengerFlowMapper;

    @Override
    public Object getPassengerFlow(String id, Date date) {
        QueryWrapper<PassengerFlow> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("date", date);
        PassengerFlow passengerFlow = passengerFlowMapper.selectOne(wrapper);
        if(passengerFlow == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"未查询到当日客流量");
        }
        Object data = passengerFlow.getData();

        return JSONObject.parse(String.valueOf(data));
    }

    @Override
    public boolean setPassengerFlow(String id, Date date, Object data) throws JsonProcessingException {
        //是否存在
        QueryWrapper<PassengerFlow> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("date", date);
        Long count = passengerFlowMapper.selectCount(wrapper);
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该日客流量已经存在");
        }

        //设定
        PassengerFlow passengerFlow = new PassengerFlow();
        passengerFlow.setId(id);
        passengerFlow.setDate(date);

        ObjectMapper objectMapper = new ObjectMapper();
        String value = objectMapper.writeValueAsString(data);
        passengerFlow.setData(value);


        boolean result = this.save(passengerFlow);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"保存时失败");
        }
        return true;
    }
}




