package com.backend.intelligent_scheduling_user_service.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.backend.intelligent_scheduling_user_service.common.ErrorCode;
import com.backend.intelligent_scheduling_user_service.exception.BusinessException;
import com.backend.intelligent_scheduling_user_service.mapper.StoreMapper;
import com.backend.intelligent_scheduling_user_service.model.Store;
import com.backend.intelligent_scheduling_user_service.model.response.GetPassengerFlowSum;
import com.backend.intelligent_scheduling_user_service.service.PassengerFlowService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.backend.intelligent_scheduling_user_service.model.PassengerFlow;
import com.backend.intelligent_scheduling_user_service.mapper.PassengerFlowMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

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

    @Resource
    private StoreMapper storeMapper;

    @Override
    public Object getPassengerFlow(String id, Date date) {
        QueryWrapper<PassengerFlow> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("date", date);
        PassengerFlow passengerFlow = passengerFlowMapper.selectOne(wrapper);
        if (passengerFlow == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未查询到当日客流量");
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
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该日客流量已经存在");
        }

        //设定
        PassengerFlow passengerFlow = new PassengerFlow();
        passengerFlow.setId(id);
        passengerFlow.setDate(date);

        ObjectMapper objectMapper = new ObjectMapper();
        String value = objectMapper.writeValueAsString(data);
        passengerFlow.setData(value);


        boolean result = this.save(passengerFlow);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存时失败");
        }
        return true;
    }

    @Override
    public List<GetPassengerFlowSum> getPassengerFlowSum(String id, Date date) {
        QueryWrapper<Store> storeQueryWrapper = new QueryWrapper<>();
        storeQueryWrapper.eq("company", id);
        List<Store> stores = storeMapper.selectList(storeQueryWrapper);
        if (stores.size() == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"为查询到改公司下的店铺");
        }

        List<GetPassengerFlowSum> passengerFlowSums = new ArrayList<>();
        for (Store store : stores) {
            QueryWrapper<PassengerFlow> passengerFlowWrapper = new QueryWrapper<>();
            passengerFlowWrapper.eq("id", store.getId());
            passengerFlowWrapper.eq("date", date);
            PassengerFlow passengerFlow = passengerFlowMapper.selectOne(passengerFlowWrapper);

            Double sum = 0.0;
            if (passengerFlow != null) {
                String dataString = (String) passengerFlow.getData();
                JSONObject data = JSON.parseObject(dataString);
                if (data != null) {
                    for (String key : data.keySet()) {
                        sum += data.getDouble(key);
                    }
                }
            }

            GetPassengerFlowSum passengerFlowSum = new GetPassengerFlowSum();
            passengerFlowSum.setId(store.getId());
            passengerFlowSum.setName(store.getName());
            passengerFlowSum.setPassengerFlowSum(sum);
            passengerFlowSums.add(passengerFlowSum);
        }

        return passengerFlowSums;
    }
}




