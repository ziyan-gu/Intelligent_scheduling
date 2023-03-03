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
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// 排班算法具体实现
@Service
public class scheduling_algorithm_impl implements scheduling_algorithm {

    //固定规则
    private List<Fixed_Rules> fixed_rules = new ArrayList<>();
    //员工信息
    private List<Employee> employees = new ArrayList<>();
    //自定义规则值
    private List<Scheduling_Rules> scheduling_rules = new ArrayList<>();
    //解析自定义规则值
    private JSONObject open_rule = new JSONObject();
    private JSONObject close_rule = new JSONObject();
    private JSONObject flow_rule = new JSONObject();
    private JSONObject on_duty_rule = new JSONObject();
    private JSONObject cashier_rule = new JSONObject();
    //解析固定规则值
    private JSONObject business_hours_rule = new JSONObject();
    private JSONObject working_hours_rule = new JSONObject();
    private JSONObject rest_time_rule = new JSONObject();

    private final Passenger_FlowDao passenger_flowDao;
    private final Scheduling_RulesDao scheduling_rulesDao;
    private final EmployeeDao employeeDao;
    private final Fixed_RulesDao fixed_rulesDao;
    private final StoreDao storeDao;
    private final SchedulingDao schedulingDao;
    @Autowired
    public scheduling_algorithm_impl(Passenger_FlowDao passenger_flowDao,
                                     Scheduling_RulesDao scheduling_rulesDao,
                                     EmployeeDao employeeDao,
                                     Fixed_RulesDao fixed_rulesDao,
                                     StoreDao storeDao,
                                     SchedulingDao schedulingDao) {
        this.passenger_flowDao = passenger_flowDao;
        this.scheduling_rulesDao = scheduling_rulesDao;
        this.employeeDao = employeeDao;
        this.fixed_rulesDao = fixed_rulesDao;
        this.storeDao = storeDao;
        this.schedulingDao = schedulingDao;
    }

    private List<Fixed_Rules> getFixed_rule(String str_admin) {
        QueryWrapper<Fixed_Rules> wrapper_fixedRule = new QueryWrapper<>();
        wrapper_fixedRule.eq("admin",str_admin);
        return fixed_rulesDao.selectList(wrapper_fixedRule);
    }

    private Store getStore(String id) {
        QueryWrapper<Store> wrapper_store = new QueryWrapper<>();
        wrapper_store.eq("id",id);
        return storeDao.selectOne(wrapper_store);
    }

    private List<Passenger_Flow> getPassenger_Flow(String id) {
        QueryWrapper<Passenger_Flow> wrapper_flow = new QueryWrapper<>();
        wrapper_flow.eq("id",id);
        return passenger_flowDao.selectList(wrapper_flow);
    }

    private List<Scheduling_Rules> getScheduling_Rules(String id) {
        QueryWrapper<Scheduling_Rules> wrapper_rule = new QueryWrapper<>();
        wrapper_rule.eq("store",id);
        return scheduling_rulesDao.selectList(wrapper_rule);
    }

    private List<Employee> getEmployee(String id) {
        QueryWrapper<Employee> wrapper_employee = new QueryWrapper<>();
        wrapper_employee.eq("store",id);
        wrapper_employee.eq("isDelete",0);
        return employeeDao.selectList(wrapper_employee);
    }

    private List<Scheduling> getScheduling(String id) {
        QueryWrapper<Scheduling> wrapper = new QueryWrapper<>();
        wrapper.eq("id",id);
        return schedulingDao.selectList(wrapper);

    }
    // 生成班次
    @Override
    public Object generation_shift(String id) {
        LocalTime time_1 = LocalTime.now();
        //获取门店信息
        Store store = getStore(id);
        String admin = store.getCompany();
        //获取固定规则
        fixed_rules = getFixed_rule(admin);
        //获取客流量
        List<Passenger_Flow> passenger_flows = getPassenger_Flow(id);
        //获取自定义规则
        scheduling_rules = getScheduling_Rules(id);
        //获取员工信息
        employees = getEmployee(id);

        for (int i = 0; i < 5; i++) {
            switch (scheduling_rules.get(i).getRuleType()) {
                case "open" -> open_rule = JSON.parseObject(scheduling_rules.get(i).getRuleValue());
                case "close" -> close_rule = JSON.parseObject(scheduling_rules.get(i).getRuleValue());
                case "flow" -> flow_rule = JSON.parseObject(scheduling_rules.get(i).getRuleValue());
                case "on_duty" -> on_duty_rule = JSON.parseObject(scheduling_rules.get(i).getRuleValue());
                case "cashier" -> cashier_rule = JSON.parseObject(scheduling_rules.get(i).getRuleValue());
            }
        }

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
            List<Integer> flow = new ArrayList<>();
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
            for (int i = 0; i < (up_down.get(1) - up_down.get(0)) * 2;) {
                float temp_1 = data.getFloat(String.valueOf(i + 1));
                float flow_pre = Float.parseFloat(flow_rule.get("pre").toString());
                int temp_1_int = (int) (temp_1 / flow_pre + (temp_1 % flow_pre != 0 ? 1 : 0));
                float temp_2 = data.getFloat(String.valueOf(i + 2));
                int temp_2_int = (int) (temp_2 / flow_pre + (temp_2 % flow_pre != 0 ? 1 : 0));
                int temp = (temp_1_int + temp_2_int) / 2 + ((temp_1_int + temp_2_int) % 2 != 0 ? 1 : 0);
                flow.add(temp);
                i += 2;
            }
            //逐小时排班
            JSONArray short_long = (JSONArray) working_hours_rule.get("c");
            int short_time = (int) short_long.get(0);
            int long_time = (int) short_long.get(1);
            List<Integer> scheduling = new ArrayList<>();
            List<Integer> scheduling_time = new ArrayList<>();
            List<Integer> current = new ArrayList<>();
            List<Integer> onDuty = new ArrayList<>();
            for (int i = 0; i < all_time; i++) {
                if (i < open_time && i == 0) {
                    for (int j = 0; j < open_num; j++) {
                        scheduling.add(1);
                        scheduling_time.add(up_down.get(0) - open_time + i);
                        scheduling_time.add(up_down.get(0) - open_time + i + 1);
                        current.add(j);
                    }
                }
                else if (i < open_time) {
                    for (int j = 0; j < current.size(); j++) {
                        extended_schedule(scheduling, scheduling_time, current, j);
                    }
                }
                else if (i >= all_time - close_time) {
                    List<Integer> short_num = new ArrayList<>();
                    int temp = 0;
                    for (int j = 0 ;j < current.size(); j++) {
                        if (scheduling.get(current.get(j)) < short_time) {
                            extended_schedule(scheduling, scheduling_time, current, j);
                            short_num.add(current.get(j));
                            temp++;
                        }
                    }
                    if (temp < close_num) {
                        int pag = close_num - temp;
                        if (current.size() - temp >= pag) {
                            increase_inTurn(scheduling, scheduling_time, current, short_num, pag);
                        }
                        else {
                            residual_addition(up_down, scheduling, scheduling_time, current, i, short_num, pag);
                        }
                    }
                }
                else {
                    List<Integer> delete_index = new ArrayList<>();
                    for (Integer integer : current) {
                        if (scheduling.get(integer) == long_time) {
                            delete_index.add(integer);
                        }
                    }
                    for (int j = delete_index.size() - 1; j > -1; j--) {
                        current.remove(delete_index.get(j));
                    }
                    List<Integer> short_num = new ArrayList<>();
                    for (int j = 0 ;j < current.size(); j++) {
                        if (scheduling.get(current.get(j)) < short_time) {
                            extended_schedule(scheduling, scheduling_time, current, j);
                            short_num.add(current.get(j));
                        }
                    }
                    if (short_num.size() < flow.get(i - open_time)) {
                        int pag = flow.get(i - open_time) - short_num.size();
                        if (current.size() - short_num.size() >= pag) {
                            increase_inTurn(scheduling, scheduling_time, current, short_num, pag);
                        }
                        else {
                            residual_addition(up_down, scheduling, scheduling_time, current, i, short_num, pag);
                        }

                    }
                    if (short_num.size() == 0 && flow.get(i - open_time) == 0) {
                        int temp;
                        if (current.size() >= 1) {
                            increase_inTurn(scheduling, scheduling_time, current, short_num, 1);
                            temp = current.get(0);
                        }
                        else {
                            residual_addition(up_down, scheduling, scheduling_time, current, i, short_num, 1);
                            temp = scheduling.size();
                        }
                        onDuty.add(temp);
                    }
                }
            }
            JSONObject up_data = new JSONObject();
            int total = scheduling.size();
            up_data.put("total", total);
            for (int i = 1; i <= total; i++) {
                JSONArray data_people = new JSONArray();
                int temp_1 = scheduling_time.get((i - 1) * 2);
                int temp_2 = scheduling_time.get((i - 1) * 2 + 1);
                data_people.add(temp_1);
                data_people.add(temp_2);
                data_people.add("0");
                if (onDuty.contains(i)) {
                    data_people.add(1);
                }
                else {
                    data_people.add(0);
                }
                up_data.put(String.valueOf(i),data_people);
            }
            Scheduling scheduling_up = new Scheduling();
            scheduling_up.setId(id);
            scheduling_up.setDate(String.valueOf(date));
            scheduling_up.setData(String.valueOf(up_data));
            QueryWrapper<Scheduling> wrapper_scheduling = new QueryWrapper<>();
            wrapper_scheduling.eq("id",id);
            wrapper_scheduling.eq("date",date);
            if (schedulingDao.exists(wrapper_scheduling)) {
                schedulingDao.update(scheduling_up,wrapper_scheduling);
            }
            else {
                schedulingDao.insert(scheduling_up);
            }
            System.out.println(generation_scheduling(scheduling_up, up_down));
        }
        LocalTime time_2 = LocalTime.now();
        Duration duration = Duration.between(time_2,time_1);
        System.out.println(duration);
        List<Scheduling> scheduling;
        scheduling = getScheduling(id);
        return scheduling;
    }

    //当前剩余增加，新增
    private void residual_addition(List<Integer> up_down, List<Integer> scheduling, List<Integer> scheduling_time, List<Integer> current, int i, List<Integer> short_num, int pag) {
        int pag_pag = pag - current.size() + short_num.size();
        for (int j = 0; j < current.size() - short_num.size(); j++) {
            if (!short_num.contains(current.get(j))) {
                extended_schedule(scheduling, scheduling_time, current, j);
            }
        }
        for (int j = 0; j < pag_pag; j++) {
            scheduling.add(1);
            scheduling_time.add(up_down.get(0) + i - 1);
            scheduling_time.add(up_down.get(0) + i);
            current.add(scheduling.size() - 1);
        }
    }

    //依次新增
    private void increase_inTurn(List<Integer> scheduling, List<Integer> scheduling_time, List<Integer> current, List<Integer> short_num, int pag) {
        int k = 0;
        for (int j = 0; j < pag; j++) {
            if (!short_num.contains(current.get(k))) {
                extended_schedule(scheduling, scheduling_time, current, k);
                k++;
            }
            else {
                k++;
                j--;
            }
        }
        int p = current.size() - 1;
        for (int j = 0; j < current.size() - pag - short_num.size(); j++) {
            if (!short_num.contains(current.get(p))) {
                current.remove(p);
                p--;
            }
            else {
                p--;
                j--;
            }
        }
    }

    //当前新增
    private void extended_schedule(List<Integer> scheduling, List<Integer> scheduling_time, List<Integer> current, int j) {
        int temp_time = scheduling_time.get(current.get(j) * 2 + 1);
        int temp_current = scheduling.get(current.get(j));
        scheduling_time.set(current.get(j) * 2 + 1,temp_time + 1);
        scheduling.set(current.get(j), temp_current + 1);
    }

    // 生成排班表
    @Override
    public Object generation_scheduling(Scheduling scheduling, List<Integer> up_down) {
        //解析员工
        List<Integer> position = new ArrayList<>();
        List<JSONObject> preference = new ArrayList<>();
        for (Employee employee : employees) {
            position.add(employee.getPosition());
            preference.add(JSON.parseObject(employee.getPreferenceValue()));
        }
        String data_str = scheduling.getData();
        JSONObject data = JSON.parseObject(data_str);
        int total = (int) data.get("total");
        JSONArray limit_position = new JSONArray();
        List<Object> employee_sort = new ArrayList<>();
        for (int i = 1; i <= total; i++) {
            new JSONArray();
            JSONArray current = (JSONArray) data.get(String.valueOf(i));
            int time_1 = (int) current.get(0);
            int time_2 = (int) current.get(1);
            List<Employee> current_employee = new ArrayList<>();
            if (up_down.get(0) == time_1) {
                limit_position = (JSONArray) open_rule.get("type");

                for (int j = 0; j < employees.size(); j++) {
                    if (limit_position.contains(position.get(j))) {
                        current_employee.add(employees.get(j));
                    }
                }
                employee_sort.add(current_employee);
            }
            else if (up_down.get(1) == time_2) {
                limit_position = (JSONArray) close_rule.get("type");

                for (int j = 0; j < employees.size(); j++) {
                    if (limit_position.contains(position.get(j))) {
                        current_employee.add(employees.get(j));
                    }
                }
                employee_sort.add(current_employee);
            }
        }
        return data;
    }
}
