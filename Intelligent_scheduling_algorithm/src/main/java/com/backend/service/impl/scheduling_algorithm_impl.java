package com.backend.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.backend.dao.*;
import com.backend.model.*;
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

///    @Autowired
///    Passenger_FlowDao passenger_flowDao;
///    @Autowired
///    Scheduling_RulesDao scheduling_rulesDao;

    private final Passenger_FlowDao passenger_flowDao;
    private final Scheduling_RulesDao scheduling_rulesDao;
    private final EmployeeDao employeeDao;
    private final Fixed_RulesDao fixed_rulesDao;
    private final StoreDao storeDao;
    @Autowired
    public scheduling_algorithm_impl(Passenger_FlowDao passenger_flowDao,
                                     Scheduling_RulesDao scheduling_rulesDao,
                                     EmployeeDao employeeDao,
                                     Fixed_RulesDao fixed_rulesDao,
                                     StoreDao storeDao) {
        this.passenger_flowDao = passenger_flowDao;
        this.scheduling_rulesDao = scheduling_rulesDao;
        this.employeeDao = employeeDao;
        this.fixed_rulesDao = fixed_rulesDao;
        this.storeDao = storeDao;
    }


    // 生成班次
    @Override
    public Object generation_shift(String id) {
        String[] str_admin = id.split("_",2);
        //获取固定规则
        QueryWrapper<Fixed_Rules> wrapper_fixedRule = new QueryWrapper<>();
        wrapper_fixedRule.eq("admin",str_admin[0]);
        List<Fixed_Rules> fixed_rules = fixed_rulesDao.selectList(wrapper_fixedRule);
        System.out.println(fixed_rules);
        //获取门店面积
        QueryWrapper<Store> wrapper_area = new QueryWrapper<>();
        wrapper_area.eq("id",id);
        Store area = storeDao.selectOne(wrapper_area);
        System.out.println(area);
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
        System.out.println(scheduling_rules);
        //获取员工信息
        QueryWrapper<Employee> wrapper_employee = new QueryWrapper<>();
        wrapper_employee.eq("store",id);
        List<Employee> employees = employeeDao.selectList(wrapper_employee);
        System.out.println(employees);
        //解析规则值
        JSONObject open_rule = new JSONObject();
        JSONObject close_rule = new JSONObject();
        JSONObject flow_rule = new JSONObject();
        JSONObject on_duty_rule = new JSONObject();
        JSONObject cashier_rule = new JSONObject();
        for (int i = 0; i < 5; i++) {
            switch (scheduling_rules.get(i).getRuleType()) {
                case "open" -> open_rule = JSON.parseObject(scheduling_rules.get(i).getRuleValue());
                case "close" -> close_rule = JSON.parseObject(scheduling_rules.get(i).getRuleValue());
                case "flow" -> flow_rule = JSON.parseObject(scheduling_rules.get(i).getRuleValue());
                case "on_duty" -> on_duty_rule = JSON.parseObject(scheduling_rules.get(i).getRuleValue());
                case "cashier" -> cashier_rule = JSON.parseObject(scheduling_rules.get(i).getRuleValue());
            }
        }
        //客流量解析
        for (Passenger_Flow passenger_flow : passenger_flows) {
            String data_str = passenger_flow.getData();
            JSONObject data = JSON.parseObject(data_str);
            List<Integer> flow = new ArrayList<>();
            for (int i = 0; i < 26;) {
                float temp_1 = data.getFloat(String.valueOf(i + 1));
                float flow_pre = Float.parseFloat(flow_rule.get("pre").toString());
                int temp_1_int = (int) (temp_1 / flow_pre + (temp_1 % flow_pre != 0 ? 1 : 0));
                float temp_2 = data.getFloat(String.valueOf(i + 2));
                int temp_2_int = (int) (temp_2 / flow_pre + (temp_2 % flow_pre != 0 ? 1 : 0));
                int temp = (temp_1_int + temp_2_int) / 2 + ((temp_1_int + temp_2_int) % 2 != 0 ? 1 : 0);
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
