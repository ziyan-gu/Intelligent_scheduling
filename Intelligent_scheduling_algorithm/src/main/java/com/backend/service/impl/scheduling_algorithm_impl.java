package com.backend.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.backend.dao.*;
import com.backend.model.*;
import com.backend.service.scheduling_algorithm;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.text.SimpleDateFormat;
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
        QueryWrapper<Store> wrapper_store = new QueryWrapper<>();
        wrapper_store.eq("id",id);
        Store store = storeDao.selectOne(wrapper_store);
//        System.out.println(area);
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
        wrapper_employee.eq("isDelete",0);
        List<Employee> employees = employeeDao.selectList(wrapper_employee);
//        System.out.println(employees);
        //解析自定义规则值
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
        //解析固定规则值
        JSONObject business_hours_rule = new JSONObject();
        JSONObject working_hours_rule = new JSONObject();
        JSONObject rest_time_rule = new JSONObject();
        for (int i = 0; i < 3; i++) {
            switch (fixed_rules.get(i).getRuleType()) {
                case "business_hours" -> business_hours_rule = JSON.parseObject(fixed_rules.get(i).getRuleValue());
                case "working_hours" -> working_hours_rule = JSON.parseObject(fixed_rules.get(i).getRuleValue());
                case "rest_time" -> rest_time_rule = JSON.parseObject(fixed_rules.get(i).getRuleValue());
            }
        }
        //开店前进行多久准备工作
        int open_time = (int) open_rule.get("pre");
        //人数
        int area = (int) store.getSize();
        int open_rule_equ = (int) open_rule.get("equ");
        int open_num = area / open_rule_equ + (area % open_rule_equ != 0 ? 1 : 0);
        //关店后进行多久收尾工作
        int close_time = (int) close_rule.get("end");
///        System.out.println(close_rule.get("equ"));
///        System.out.println(close_rule.get("equ").getClass());
        JSONArray temp_json = (JSONArray) close_rule.get("equ");
        List<Integer> close_num_equ = JSONArray.parseArray(temp_json.toString(),Integer.class);
        int close_num = (area / close_num_equ.get(0) + (area % close_num_equ.get(0) != 0 ? 1 : 0)) + close_num_equ.get(1);
        //客流量解析
        for (Passenger_Flow passenger_flow : passenger_flows) {
            String data_str = passenger_flow.getData();
            JSONObject data = JSON.parseObject(data_str);
            Date date = passenger_flow.getDate();
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
            String week = sdf.format(date);
//            System.out.println(week);
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
            //获取上下班时间
            List<Integer> up_down;
            JSONArray up_down_json;
            if (week.equals("星期六") || week.equals("星期日")) {
                up_down_json = (JSONArray) business_hours_rule.get("dayoff");
            }
            else {
                up_down_json = (JSONArray) business_hours_rule.get("workingday");
            }
            up_down = JSONArray.parseArray(up_down_json.toString(),Integer.class);
            //持续时长
            int all_time = up_down.get(1) - up_down.get(0) + open_time + close_time;
            System.out.println(up_down);
            //逐小时排班
            for (int i = 0; i < all_time; i++) {
                
                System.out.println("");
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
