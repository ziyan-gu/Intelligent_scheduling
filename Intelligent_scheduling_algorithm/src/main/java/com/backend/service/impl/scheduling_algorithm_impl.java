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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        generation_scheduling(schedulings, up_downs);
//        System.out.println(generation_scheduling(schedulings, up_downs));
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
    private void generation_scheduling(List<Scheduling> schedulings, List<List<Integer>> up_downs) {
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
            //本周第一天为周几
            Date first_date = Date.valueOf(schedulings.get(index_scheduling).getDate());
            String first_day = sdf.format(first_date);
            int first_day_int = week_list.indexOf(first_day) + 1;
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
                        rules_employeeSort(position, employee_sort, current_employee, open_rule, employee_schedulings, week_int, i);
                    }
                    else if (up_down.get(1) + (int)close_rule.get("end") == time_2) {
                        rules_employeeSort(position, employee_sort, current_employee, close_rule, employee_schedulings, week_int, i);
                    }
                    else if (is_onDuty == 1) {
                        rules_employeeSort(position, employee_sort, current_employee, on_duty_rule, employee_schedulings, week_int, i);
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
                employee_sorts.add(employee_sort);
            }
//            System.out.println(employee_sorts);
//            System.out.println(employee_schedulings);
            //员工工作日时长
            List<Integer> day_time = new ArrayList<>();
            //员工周时长
            List<Integer> week_time = new ArrayList<>();
            for (int i = 0; i < employee_schedulings.size(); i++) {
                day_time.add(0);
                week_time.add(0);
            }
            //将所有员工排班信息转换为Map
            Map<String, Employee_Scheduling> employee_schedulingMap = employee_schedulings.stream().collect(Collectors.toMap(Employee_Scheduling::getId, Function.identity()));
            //对员工进行优先级筛选及规则匹配
            for (int h = 0; h < employee_schedulings.size(); h++) {
                Employee_Scheduling employee_scheduling = employee_schedulings.get(h);
//                List<List<Employee_Sort>> employee_sort_day = employee_sorts.get(h);
                for (int i = 0; i < 7; i++) {
                    day_time.set(h, 0);
                    if (i == 0 && employee_scheduling.getMonday() != null) {
                        scheduling_done(employee_schedulings,employee_scheduling,h,day_time,week_time,employee_sorts,i,first_day_int,employee_schedulingMap,index_scheduling,schedulings,1);
                    }
                    else if (i == 1 && employee_scheduling.getTuesday() != null) {
                        scheduling_done(employee_schedulings,employee_scheduling,h,day_time,week_time,employee_sorts,i,first_day_int,employee_schedulingMap,index_scheduling,schedulings,2);
                    }
                    else if (i == 2 && employee_scheduling.getWednesday() != null) {
                        scheduling_done(employee_schedulings,employee_scheduling,h,day_time,week_time,employee_sorts,i,first_day_int,employee_schedulingMap,index_scheduling,schedulings,3);
                    }
                    else if (i == 3 && employee_scheduling.getThursday() != null) {
                        scheduling_done(employee_schedulings,employee_scheduling,h,day_time,week_time,employee_sorts,i,first_day_int,employee_schedulingMap,index_scheduling,schedulings,4);
                    }
                    else if (i == 4 && employee_scheduling.getFriday() != null) {
                        scheduling_done(employee_schedulings,employee_scheduling,h,day_time,week_time,employee_sorts,i,first_day_int,employee_schedulingMap,index_scheduling,schedulings,5);
                    }
                    else if (i == 5 && employee_scheduling.getSaturday() != null) {
                        scheduling_done(employee_schedulings,employee_scheduling,h,day_time,week_time,employee_sorts,i,first_day_int,employee_schedulingMap,index_scheduling,schedulings,6);
                    }
                    else if (i == 6 && employee_scheduling.getSunday() != null) {
                        scheduling_done(employee_schedulings,employee_scheduling,h,day_time,week_time,employee_sorts,i,first_day_int,employee_schedulingMap,index_scheduling,schedulings,7);
                    }
                }
            }
            for (int i = 0; i < num_day.get(k); i++) {
                new Scheduling();
                Scheduling scheduling_up;
                //排班列表序号
                scheduling_up = schedulings.get(index_scheduling + i);
                QueryWrapper<Scheduling> wrapper_scheduling = new QueryWrapper<>();
                wrapper_scheduling.eq("id",scheduling_up.getId());
                wrapper_scheduling.eq("date",scheduling_up.getDate());
                if (schedulingDao.exists(wrapper_scheduling)) {
                    schedulingDao.update(scheduling_up,wrapper_scheduling);
                }
                else {
                    schedulingDao.insert(scheduling_up);
                }
            }
//            System.out.println(employee_sorts);
        }
    }

    //将员工排班入表
    private void scheduling_done(List<Employee_Scheduling> employee_schedulings,
                                 Employee_Scheduling employee_scheduling,
                                 int h,
                                 List<Integer> day_time,
                                 List<Integer> week_time,
                                 List<List<List<Employee_Sort>>> employee_sorts,
                                 int i,
                                 int first_day_int,
                                 Map<String, Employee_Scheduling> employee_schedulingMap,
                                 int index_scheduling,
                                 List<Scheduling> schedulings,
                                 int key) {
        List<JSONArray> employee_nowData = new ArrayList<>();
        //获取当前员工当天班次信息
        switch (key) {
            case 1 -> employee_nowData = employee_scheduling.getMonday();
            case 2 -> employee_nowData = employee_scheduling.getTuesday();
            case 3 -> employee_nowData = employee_scheduling.getWednesday();
            case 4 -> employee_nowData = employee_scheduling.getThursday();
            case 5 -> employee_nowData = employee_scheduling.getFriday();
            case 6 -> employee_nowData = employee_scheduling.getSaturday();
            case 7 -> employee_nowData = employee_scheduling.getSunday();
        }
        //班次初始索引
        int temp_index;
        //按优先级排序
        sort_scheduling(employee_nowData);
        for (int t = 0; t < employee_nowData.size(); t++) {
            //超过每天/每周时长
            if (day_time.get(h) > (int) working_hours_rule.get("b")) {
                switch (key) {
                    case 1 -> employee_schedulings.get(h).setMonday(null);
                    case 2 -> employee_schedulings.get(h).setTuesday(null);
                    case 3 -> employee_schedulings.get(h).setWednesday(null);
                    case 4 -> employee_schedulings.get(h).setThursday(null);
                    case 5 -> employee_schedulings.get(h).setFriday(null);
                    case 6 -> employee_schedulings.get(h).setSaturday(null);
                    case 7 -> employee_schedulings.get(h).setSunday(null);
                }
                List<List<Integer>> op = new ArrayList<>();
                for (int l = 0; l < employee_sorts.get(i + 1 - first_day_int).size(); l++) {
                    for (int m = employee_sorts.get(i + 1 - first_day_int).get(l).size() - 1; m > 0; m--) {
                        if (employee_sorts.get(i + 1 - first_day_int).get(l).get(m).getEmployeeId().equals(employee_scheduling.getId())) {
                            List<Integer> op_t = new ArrayList<>();
                            op_t.add(l);
                            op_t.add(m);
                            op.add(op_t);
                        }
                    }
                }
                for (List<Integer> integers : op) {
                    employee_sorts.get(i + 1 - first_day_int).get(integers.get(0)).remove((int) integers.get(1));
                }
                break;
            }
            else if (week_time.get(h) > (int) working_hours_rule.get("a")) {
                employee_schedulings.get(h).setMonday(null);
                employee_schedulings.get(h).setTuesday(null);
                employee_schedulings.get(h).setWednesday(null);
                employee_schedulings.get(h).setSaturday(null);
                employee_schedulings.get(h).setFriday(null);
                employee_schedulings.get(h).setThursday(null);
                employee_schedulings.get(h).setSunday(null);
                List<List<Integer>> op = new ArrayList<>();
                for (int j = 0; j < employee_sorts.size(); j++) {
                    for (int k = 0; k < employee_sorts.get(j).size(); k++) {
                        for (int n = employee_sorts.get(j).get(k).size() - 1; n > 0; n--) {
                            if (employee_sorts.get(j).get(k).get(n).getEmployeeId().equals(employee_scheduling.getId())) {
                                List<Integer> op_t = new ArrayList<>();
                                op_t.add(j);
                                op_t.add(k);
                                op_t.add(n);
                                op.add(op_t);
                            }
                        }
                    }
                }
                for (List<Integer> integers : op) {
                    employee_sorts.get(integers.get(0)).get(integers.get(1)).remove((int) integers.get(2));
                }
                break;
            }

            new JSONArray();
            JSONArray current_scheduling;
            //员工当前班次
            current_scheduling = employee_nowData.get(t);
            //当前班次所有员工employee_nowData
            List<Employee_Sort> employee_sort_temp = employee_sorts.get(i + 1 - first_day_int).get((Integer) current_scheduling.get(0) - 1);
            //取当前班次第一个员工信息
            Employee_Sort employee_sort = employee_sort_temp.get(0);
            int index_employee = 0;
            String employee_id = employee_sort.getEmployeeId();
            //根据id取出员工排班信息
            Employee_Scheduling sort_employee_scheduling = employee_schedulingMap.get(employee_id);
            //取得员工序列索引
            for (int j = 0; j < employee_schedulings.size(); j++) {
                if (employee_schedulings.get(j).getId().equals(employee_id)) {
                    index_employee = j;
                    break;
                }
            }
            List<JSONArray> sort_employee = new ArrayList<>();
            switch (key) {
                case 1 -> sort_employee = sort_employee_scheduling.getMonday();
                case 2 -> sort_employee = sort_employee_scheduling.getTuesday();
                case 3 -> sort_employee = sort_employee_scheduling.getWednesday();
                case 4 -> sort_employee = sort_employee_scheduling.getThursday();
                case 5 -> sort_employee = sort_employee_scheduling.getFriday();
                case 6 -> sort_employee = sort_employee_scheduling.getSaturday();
                case 7 -> sort_employee = sort_employee_scheduling.getSunday();
            }
            //班次序号
            int scheduling_index = 0;
            //排序
            sort_scheduling(sort_employee);
            int num = 0;
            for (int l = 0; l < sort_employee.size(); l++) {
                if (sort_employee.get(l).get(0) == current_scheduling.get(0)) {
                    num = l;
                    scheduling_index = (int) sort_employee.get(l).get(0);
                    break;
                }
            }
            int select = employee_sort.getPriority() + num;
            for (int l = 1; l < employee_sort_temp.size(); l++) {
                Employee_Sort temp = employee_sort_temp.get(l);
                String temp_employee_id = temp.getEmployeeId();
                //根据id取出员工排班信息
                Employee_Scheduling temp_sort_employee_scheduling = employee_schedulingMap.get(temp_employee_id);
//                System.out.println(temp_sort_employee_scheduling);
                List<JSONArray> temp_sort_employee = new ArrayList<>();
                switch (key) {
                    case 1 -> temp_sort_employee = temp_sort_employee_scheduling.getMonday();
                    case 2 -> temp_sort_employee = temp_sort_employee_scheduling.getTuesday();
                    case 3 -> temp_sort_employee = temp_sort_employee_scheduling.getWednesday();
                    case 4 -> temp_sort_employee = temp_sort_employee_scheduling.getThursday();
                    case 5 -> temp_sort_employee = temp_sort_employee_scheduling.getFriday();
                    case 6 -> temp_sort_employee = temp_sort_employee_scheduling.getSaturday();
                    case 7 -> temp_sort_employee = temp_sort_employee_scheduling.getSunday();
                }
                //排序
                sort_scheduling(temp_sort_employee);
                //取得员工序列索引
                int temp_scheduling_index = 0;
                for (int j = 0; j < employee_schedulings.size(); j++) {
                    if (employee_schedulings.get(j).getId().equals(temp_employee_id)) {
                        temp_scheduling_index = j;
                        break;
                    }
                }
                int temp_num = 0;
                for (int p = 0; p < temp_sort_employee.size(); p++) {
                    if (temp_sort_employee.get(p).get(0) == current_scheduling.get(0)) {
                        temp_num = p;
                        break;
                    }
                }
                int temp_select = temp.getPriority() + temp_num;
                //删除优先级低的数据
                String remove_id = temp_employee_id;
                int remove_index = temp_scheduling_index;
                if (select > temp_select) {
                    remove_id = employee_id;
                    remove_index = index_employee;
                    index_employee = temp_scheduling_index;
                    employee_id = temp_employee_id;
                }
                List<Integer> op = new ArrayList<>();
                for (int m = employee_sorts.get(i + 1 - first_day_int).get((Integer) current_scheduling.get(0) - 1).size() - 1; m >= 0 ; m--) {
                    if (employee_sorts.get(i + 1 - first_day_int).get((Integer) current_scheduling.get(0) - 1).get(m).getEmployeeId().equals(remove_id)) {
                        op.add(m);
                        break;
                    }
                }
                for (Integer value : op) {
                    employee_sorts.get(i + 1 - first_day_int).get((Integer) current_scheduling.get(0) - 1).remove((int) value);
                    l--;
                }
                List<JSONArray> jsonArray = new ArrayList<>();
                switch (key) {
                    case 1 -> jsonArray = employee_schedulings.get(remove_index).getMonday();
                    case 2 -> jsonArray = employee_schedulings.get(remove_index).getTuesday();
                    case 3 -> jsonArray = employee_schedulings.get(remove_index).getWednesday();
                    case 4 -> jsonArray = employee_schedulings.get(remove_index).getThursday();
                    case 5 -> jsonArray = employee_schedulings.get(remove_index).getFriday();
                    case 6 -> jsonArray = employee_schedulings.get(remove_index).getSaturday();
                    case 7 -> jsonArray = employee_schedulings.get(remove_index).getSunday();
                }
                List<Integer> op_2 = new ArrayList<>();
                for (int m = jsonArray.size() - 1; m >= 0; m--) {
                    if ((int) jsonArray.get(m).get(0) == (int) current_scheduling.get(0)) {
                        op_2.add(m);
                        break;
                    }
                }
                for (Integer integer : op_2) {
                    switch (key) {
                        case 1 -> employee_schedulings.get(remove_index).getMonday().remove((int) integer);
                        case 2 -> employee_schedulings.get(remove_index).getTuesday().remove((int) integer);
                        case 3 -> employee_schedulings.get(remove_index).getWednesday().remove((int) integer);
                        case 4 -> employee_schedulings.get(remove_index).getThursday().remove((int) integer);
                        case 5 -> employee_schedulings.get(remove_index).getFriday().remove((int) integer);
                        case 6 -> employee_schedulings.get(remove_index).getSaturday().remove((int) integer);
                        case 7 -> employee_schedulings.get(remove_index).getSunday().remove((int) integer);
                    }
                }
            }
            //排班列表序号
            temp_index = index_scheduling + i + 1 - first_day_int;
            //将排班后的数据放入排班表里
            JSONObject data_obj = JSON.parseObject(schedulings.get(temp_index).getData());
            JSONArray jsonArray = data_obj.getJSONArray(String.valueOf(scheduling_index));
            //班次持续时长
            int long_time = (int) jsonArray.get(1) - (int) jsonArray.get(0);
            jsonArray.set(2, employee_id);
            data_obj.put(String.valueOf(scheduling_index), jsonArray);
            schedulings.get(temp_index).setData(String.valueOf(data_obj));
            //删除该员工同时间其他班次
            int time_1 = (int) jsonArray.get(0);    //开始时间
            int time_2 = (int) jsonArray.get(1);    //结束时间
            List<JSONArray> time_temp = new ArrayList<>();
            switch (key) {
                case 1 -> time_temp = employee_schedulings.get(index_employee).getMonday();
                case 2 -> time_temp = employee_schedulings.get(index_employee).getTuesday();
                case 3 -> time_temp = employee_schedulings.get(index_employee).getWednesday();
                case 4 -> time_temp = employee_schedulings.get(index_employee).getThursday();
                case 5 -> time_temp = employee_schedulings.get(index_employee).getFriday();
                case 6 -> time_temp = employee_schedulings.get(index_employee).getSaturday();
                case 7 -> time_temp = employee_schedulings.get(index_employee).getSunday();
            }
            for (int j = 0; j < time_temp.size(); j++) {
                int time_temp_1 = (int) data_obj.getJSONArray(String.valueOf(time_temp.get(j).get(0))).get(0);
                int time_temp_2 = (int) data_obj.getJSONArray(String.valueOf(time_temp.get(j).get(0))).get(1);
                if (time_1 < time_temp_2 && time_2 > time_temp_1 && !employee_schedulings.get(j).getId().equals(employee_id)) {

                    List<Integer> op = new ArrayList<>();
                    for (int m = employee_sorts.get(i + 1 - first_day_int).get( (int) time_temp.get(j).get(0) - 1).size() - 1; m >= 0 ; m--) {
                        if (employee_sorts.get(i + 1 - first_day_int).get( (int) time_temp.get(j).get(0) - 1).get(m).getEmployeeId().equals(employee_id)) {
                            op.add(m);
                        }
                    }
                    for (Integer value : op) {
                        employee_sorts.get(i + 1 - first_day_int).get( (int) time_temp.get(j).get(0) - 1).remove((int) value);
                    }
                    switch (key) {
                        case 1 -> employee_schedulings.get(index_employee).getMonday().remove(j);
                        case 2 -> employee_schedulings.get(index_employee).getTuesday().remove(j);
                        case 3 -> employee_schedulings.get(index_employee).getWednesday().remove(j);
                        case 4 -> employee_schedulings.get(index_employee).getThursday().remove(j);
                        case 5 -> employee_schedulings.get(index_employee).getFriday().remove(j);
                        case 6 -> employee_schedulings.get(index_employee).getSaturday().remove(j);
                        case 7 -> employee_schedulings.get(index_employee).getSunday().remove(j);
                    }
                    j--;
                }
            }
            //设置该员工天/周工作总时长
            int temp_day_time = day_time.get(index_employee);
            temp_day_time = temp_day_time + long_time;
            day_time.set(index_employee, temp_day_time);
            int temp_week_time = week_time.get(index_employee);
            temp_week_time = temp_week_time + long_time;
            week_time.set(index_employee, temp_week_time);
        }
    }

    //员工班次排序
    private void sort_scheduling(List<JSONArray> employee_nowData) {
        new JSONArray();
        JSONArray temp;
        for (int i = employee_nowData.size() - 1; i > 0; i--) { // 每次需要排序的长度
            for (int j = 0; j < i; j++) { // 从第一个元素到第i个元素
                if ((int)employee_nowData.get(j).get(1) > (int)employee_nowData.get(j + 1).get(1)) {
                    temp = employee_nowData.get(j);
                    employee_nowData.set(j, employee_nowData.get(j + 1));
                    employee_nowData.set(j + 1, temp);
                }
            }
        }
    }

    //开店关店规则-选取员工
    private void rules_employeeSort(List<Integer> position,
                                    List<List<Employee_Sort>> employee_sort,
                                    List<Employee_Sort> current_employee,
                                    JSONObject rule,
                                    List<Employee_Scheduling> employee_schedulings,
                                    int week_int,
                                    int i) {
        JSONArray limit_position;
        limit_position = (JSONArray) rule.get("type");

        for (int j = 0; j < employees.size(); j++) {
            if (limit_position.contains(position.get(j))) {
                Employee_Sort employee_sort_current = new Employee_Sort();
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
