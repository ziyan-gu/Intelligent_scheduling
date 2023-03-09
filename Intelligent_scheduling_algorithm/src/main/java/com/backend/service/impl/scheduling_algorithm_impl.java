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
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

// 排班算法具体实现
@Service
public class scheduling_algorithm_impl implements scheduling_algorithm {

    //员工信息
    private List<Employee> employees = new ArrayList<>();
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
        Date current_date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        String week = sdf.format(current_date);
        List<String> week_list = new ArrayList<>(Arrays.asList("星期一","星期二","星期三","星期四","星期五","星期六","星期日"));
        int week_int = week_list.indexOf(week) + 1;
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(current_date);
        rightNow.add(Calendar.DAY_OF_YEAR,1 - week_int);
        SimpleDateFormat sdf_2 = new SimpleDateFormat("yyyy-MM-dd");
        Date date = Date.valueOf(sdf_2.format(rightNow.getTime()));
        wrapper_flow.ge("date",date);
        wrapper_flow.last("limit 28");
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
        //固定规则
        List<Fixed_Rules> fixed_rules = getFixed_rule(admin);
        //获取客流量
        List<Passenger_Flow> passenger_flows = getPassenger_Flow(id);
        //获取自定义规则
        //自定义规则值
        List<Scheduling_Rules> scheduling_rules = getScheduling_Rules(id);
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
        List<Scheduling> schedulings = new ArrayList<>();
        List<List<Integer>> up_downs = new ArrayList<>();
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
            schedulings.add(scheduling_up);
            up_downs.add(up_down);
        }
        System.out.println(generation_scheduling(schedulings, up_downs));
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
    public Object generation_scheduling(List<Scheduling> schedulings, List<List<Integer>> up_downs) {
        //星期表
        List<String> week_list = new ArrayList<>(Arrays.asList("星期一","星期二","星期三","星期四","星期五","星期六","星期日"));
        //需要排班天数
        int num_scheduling = schedulings.size();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        int total_week = 0;
        int first_week_int = 0;
        if (num_scheduling > 0) {
            Date first_date = Date.valueOf(schedulings.get(0).getDate());
            String first_week = sdf.format(first_date);
            first_week_int = week_list.indexOf(first_week);
            //共有总周数
            if (num_scheduling <= (7 - first_week_int)) {
                total_week = 1;
            }
            else {
                total_week = (num_scheduling - 7 + first_week_int) / 7 + ((num_scheduling - 7 + first_week_int) % 7 == 0 ? 1 : 2);
            }
        }
        //每周共有几天
        List<Integer> num_day = new ArrayList<>();
        for (int i = 0; i < total_week; i++) {
            if (total_week == 1){
                num_day.add(num_scheduling);
            }
            else if (i == 0) {
                num_day.add(7 - first_week_int);
            }
            else if (i == total_week - 1) {
                num_day.add(num_scheduling - 7 + first_week_int - (total_week - 2) * 7);
            }
            else {
                num_day.add(7);
            }
        }
        //解析员工
        //员工的职位
        List<Integer> position = new ArrayList<>();
        //员工的偏好
        List<JSONObject> preference = new ArrayList<>();
        //员工班次
        List<Employee_Scheduling> employee_schedulings = new ArrayList<>();
        for (Employee employee : employees) {
            position.add(employee.getPosition());
            preference.add(JSON.parseObject(employee.getPreferenceValue()));
            Employee_Scheduling employee_scheduling = new Employee_Scheduling();
            employee_scheduling.setId(employee.getId());
            employee_schedulings.add(employee_scheduling);
        }
        //按周排班
        for (int k = 0; k < total_week; k++) {
            List<List<List<Employee_Sort>>> employee_sorts = new ArrayList<>();
            //本周第一天的排班表序号
            int index_scheduling;
            if (k == 0) {
                index_scheduling = 0;
            }
            else if (k == total_week - 1) {
                index_scheduling = num_day.get(0) + 7 * (total_week - 2);
            }
            else {
                index_scheduling = num_day.get(0) + 7 * (k - 1);
            }
            for (int z = 0, index = index_scheduling; z < num_day.get(k); z++, index++) {
                //当天班次
                Scheduling scheduling = schedulings.get(index);
                List<Integer> up_down = up_downs.get(index);
                Date date = Date.valueOf(scheduling.getDate());
                String current_week = sdf.format(date);
                //排班表数据信息
                String data_str = scheduling.getData();
                JSONObject data = JSON.parseObject(data_str);
                int total = (int) data.get("total");
                //限定职位
                new JSONArray();
                JSONArray limit_position;
                //每个班次拥有的员工
                List<List<Employee_Sort>> employee_sort = new ArrayList<>();
                int week_int = week_list.indexOf(current_week) + 1;
                for (int i = 1; i <= total; i++) {
                    new JSONArray();
                    JSONArray current = (JSONArray) data.get(String.valueOf(i));
                    //当前班次时间
                    int time_1 = (int) current.get(0);
                    int time_2 = (int) current.get(1);
                    JSONArray lunch = rest_time_rule.getJSONArray("lunch");
                    JSONArray dinner = rest_time_rule.getJSONArray("dinner");
                    if (time_1 < (int)lunch.get(0) && time_2 > (int)lunch.get(1)) {
                        JSONArray temp = data.getJSONArray(String.valueOf(i));
                        int temp_int = (int) temp.get(1) + (int)lunch.get(2);
                        temp.set(1,temp_int);
                        data.put(String.valueOf(i),temp);
                    }
                    else if (time_1 < (int)dinner.get(0) && time_2 > (int)dinner.get(1)) {
                        JSONArray temp = data.getJSONArray(String.valueOf(i));
                        int temp_int = (int) temp.get(1) + (int)dinner.get(2);
                        temp.set(1,temp_int);
                        data.put(String.valueOf(i),temp);
                    }
                    //判断是否值班班次
                    int is_onDuty = (int) current.get(3);
                    //当前班次所拥有的员工
                    List<Employee_Sort> current_employee = new ArrayList<>();

                    if (up_down.get(0) - (int)open_rule.get("pre") == time_1) {
                        Employee_Sort employee_sort_current = new Employee_Sort();
                        rules_employeeSort(position, employee_sort, current_employee, employee_sort_current, open_rule, employee_schedulings, week_int, i);
                    }
                    else if (up_down.get(1) + (int)close_rule.get("end") == time_2) {
                        Employee_Sort employee_sort_current = new Employee_Sort();
                        rules_employeeSort(position, employee_sort, current_employee, employee_sort_current, close_rule, employee_schedulings, week_int, i);
                    }
                    else if (is_onDuty == 1) {
                        Employee_Sort employee_sort_current = new Employee_Sort();
                        rules_employeeSort(position, employee_sort, current_employee, employee_sort_current, on_duty_rule, employee_schedulings, week_int, i);
                    }
                    else {
                        limit_position = (JSONArray) cashier_rule.get("type");
                        for (int j = 0; j < employees.size(); j++) {
                            if (limit_position.contains(position.get(j))) {
                                Employee_Sort employee_sort_current = new Employee_Sort();
                                employee_sort_current.setEmployeeId(employees.get(j).getId());
                                employee_sort_current.setPriority(1);
                                current_employee.add(employee_sort_current);
                                setEmployees_Scheduling(employee_schedulings, week_int, i, j, 5);
                            }
                            JSONArray workday = preference.get(j).getJSONObject("workday").getJSONArray("day");
                            JSONArray working_hours_time = preference.get(j).getJSONObject("working_hours").getJSONArray("time");
                            //是否属于偏好时间段
                            int temp = Math.min((time_2 - (int) working_hours_time.get(0)), (time_1 - (int) working_hours_time.get(1)));
                            int temp_2 = Math.min((time_2 - (int) working_hours_time.get(2)), (time_1 - (int) working_hours_time.get(3)));
                            if (workday.contains(week_int) && (temp >= (time_2 - time_1) / 2 || temp_2 >= (time_2 - time_1) / 2)) {
                                Employee_Sort employee_sort_current = new Employee_Sort();
                                employee_sort_current.setEmployeeId(employees.get(j).getId());
                                employee_sort_current.setPriority(5);
                                current_employee.add(employee_sort_current);
                                setEmployees_Scheduling(employee_schedulings, week_int, i, j, 5);
                            }
                        }
                        employee_sort.add(current_employee);
                    }
                }
//                System.out.println(employee_schedulings);
                employee_sorts.add(employee_sort);
            }
//            System.out.println(employee_sorts);
        }
        return employee_schedulings;
    }

    //开店关店规则-选取员工
    private void rules_employeeSort(List<Integer> position,
                                    List<List<Employee_Sort>> employee_sort,
                                    List<Employee_Sort> current_employee,
                                    Employee_Sort employee_sort_current,
                                    JSONObject rule,
                                    List<Employee_Scheduling> employee_schedulings,
                                    int week_int,
                                    int i) {
        JSONArray limit_position;
        limit_position = (JSONArray) rule.get("type");

        for (int j = 0; j < employees.size(); j++) {
            if (limit_position.contains(position.get(j))) {
                employee_sort_current.setEmployeeId(employees.get(j).getId());
                employee_sort_current.setPriority(1);
                current_employee.add(employee_sort_current);
                setEmployees_Scheduling(employee_schedulings, week_int, i, j, 1);
            }
        }
        employee_sort.add(current_employee);
    }

    private void setEmployees_Scheduling(List<Employee_Scheduling> employee_schedulings,
                                         int week_int,
                                         int i,
                                         int j,
                                         int key) {
        JSONArray objects = new JSONArray();
        objects.add(i);
        objects.add(key);
        switch (week_int) {
            case 1 -> {
                List<JSONArray> list_1 = employee_schedulings.get(j).getMonday();
                if (list_1 == null) {
                    List<JSONArray> temp = new ArrayList<>();
                    temp.add(objects);
                    employee_schedulings.get(j).setMonday(temp);
                }
                else {
                    list_1.add(objects);
                    employee_schedulings.get(j).setMonday(list_1);
                }
            }
            case 2 -> {
                List<JSONArray> list_2 = employee_schedulings.get(j).getTuesday();
                if (list_2 == null) {
                    List<JSONArray> temp = new ArrayList<>();
                    temp.add(objects);
                    employee_schedulings.get(j).setTuesday(temp);
                }
                else {
                    list_2.add(objects);
                    employee_schedulings.get(j).setTuesday(list_2);
                }
            }
            case 3 -> {
                List<JSONArray> list_3 = employee_schedulings.get(j).getWednesday();
                if (list_3 == null) {
                    List<JSONArray> temp = new ArrayList<>();
                    temp.add(objects);
                    employee_schedulings.get(j).setWednesday(temp);
                }
                else {
                    list_3.add(objects);
                    employee_schedulings.get(j).setWednesday(list_3);
                }
            }
            case 4 -> {
                List<JSONArray> list_4 = employee_schedulings.get(j).getThursday();
                if (list_4 == null) {
                    List<JSONArray> temp = new ArrayList<>();
                    temp.add(objects);
                    employee_schedulings.get(j).setThursday(temp);
                }
                else {
                    list_4.add(objects);
                    employee_schedulings.get(j).setThursday(list_4);
                }
            }
            case 5 -> {
                List<JSONArray> list_5 = employee_schedulings.get(j).getFriday();
                if (list_5 == null) {
                    List<JSONArray> temp = new ArrayList<>();
                    temp.add(objects);
                    employee_schedulings.get(j).setFriday(temp);
                }
                else {
                    list_5.add(objects);
                    employee_schedulings.get(j).setFriday(list_5);
                }
            }
            case 6 -> {
                List<JSONArray> list_6 = employee_schedulings.get(j).getSaturday();
                if (list_6 == null) {
                    List<JSONArray> temp = new ArrayList<>();
                    temp.add(objects);
                    employee_schedulings.get(j).setSaturday(temp);
                }
                else {
                    list_6.add(objects);
                    employee_schedulings.get(j).setSaturday(list_6);
                }
            }
            case 7 -> {
                List<JSONArray> list_7 = employee_schedulings.get(j).getSunday();
                if (list_7 == null) {
                    List<JSONArray> temp = new ArrayList<>();
                    temp.add(objects);
                    employee_schedulings.get(j).setSunday(temp);
                }
                else {
                    list_7.add(objects);
                    employee_schedulings.get(j).setSunday(list_7);
                }
            }
        }
    }
}
