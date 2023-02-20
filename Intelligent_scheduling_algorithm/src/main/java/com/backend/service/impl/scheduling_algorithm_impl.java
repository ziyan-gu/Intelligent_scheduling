package com.backend.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.backend.dao.Passenger_FlowDao;
import com.backend.dao.Scheduling_RulesDao;
import com.backend.model.Passenger_Flow;
import com.backend.model.Scheduling_Rules;
import com.backend.service.scheduling_algorithm;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

// 排班算法具体实现
@Service
public class scheduling_algorithm_impl implements scheduling_algorithm {
/**    @Autowired
    Passenger_FlowDao passenger_flowDao;
    @Autowired
    Scheduling_RulesDao scheduling_rulesDao;
 **/
    private final Passenger_FlowDao passenger_flowDao;
    private final Scheduling_RulesDao scheduling_rulesDao;
    @Autowired
    public scheduling_algorithm_impl(Passenger_FlowDao passenger_flowDao,Scheduling_RulesDao scheduling_rulesDao) {
        this.passenger_flowDao = passenger_flowDao;
        this.scheduling_rulesDao = scheduling_rulesDao;
    }


    // 生成班次
    @Override
    public Object generation_shift(String id) {
        //获取客流量
//        Date date = Date.valueOf("2023-5-10");
        QueryWrapper<Passenger_Flow> wrapper_flow = new QueryWrapper<>();
        wrapper_flow.eq("id",id);
//        wrapper.eq("date",date);
        List<Passenger_Flow> passenger_flows = passenger_flowDao.selectList(wrapper_flow);
        //获取自定义规则
        QueryWrapper<Scheduling_Rules> wrapper_rule = new QueryWrapper<>();
        wrapper_rule.eq("store",id);
        List<Scheduling_Rules> scheduling_rules = scheduling_rulesDao.selectList(wrapper_rule);

        for (Passenger_Flow passenger_flow : passenger_flows) {
            String data_str = passenger_flow.getData();
            JSONObject data = JSON.parseObject(data_str);
            List<Integer> flow = new ArrayList<>();
            for (int i = 0; i < 26;) {
                int temp_1 = data.getInteger(String.valueOf(i + 1));
                int temp_2 = data.getInteger(String.valueOf(i + 2));
                int temp = temp_1 + temp_2;
                flow.add(temp);
                i += 2;
            }
            System.out.println(flow);
        }
        return passenger_flows.get(0);
    }

    // 生成排班表
    @Override
    public Object generation_scheduling() {
        List<Passenger_Flow> passenger_flows = passenger_flowDao.selectList(null);
        System.out.println(passenger_flows.get(0).getId());
        return passenger_flows.get(0).getId();
    }
}
