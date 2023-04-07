package com.backend.intelligent_scheduling_user_service.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.backend.intelligent_scheduling_user_service.common.ErrorCode;
import com.backend.intelligent_scheduling_user_service.exception.BusinessException;
import com.backend.intelligent_scheduling_user_service.mapper.PassengerFlowMapper;
import com.backend.intelligent_scheduling_user_service.mapper.StoreMapper;
import com.backend.intelligent_scheduling_user_service.model.PassengerFlow;
import com.backend.intelligent_scheduling_user_service.model.Store;
import com.backend.intelligent_scheduling_user_service.model.Week;
import com.backend.intelligent_scheduling_user_service.model.response.GetPassengerFlowSum;
import com.backend.intelligent_scheduling_user_service.model.response.GetWeekPassengerFlow;
import com.backend.intelligent_scheduling_user_service.service.PassengerFlowService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

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
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"为查询到该公司下的店铺");
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

    @Override
    public boolean generatePassengerFlow(String id) throws JsonProcessingException, ParseException {
        //查询店铺是否存在
        QueryWrapper<Store> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("id", id);
        Store store = storeMapper.selectOne(objectQueryWrapper);
        if(store == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"没有该店铺");
        }

        // 获取当前日期
        LocalDate currentDate = LocalDate.now();

        // 生成一周内的日期
        List<Date> dates = new ArrayList<>();
        for (int i = 0; i < 28; i++) {
//            LocalDate date = currentDate.plusDays(i);
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            java.util.Date newDate = dateFormat.parse(String.valueOf(date));
//            java.sql.Date sqlDate = new java.sql.Date(newDate.getTime());
//            dates.add(sqlDate);
            LocalDate date = currentDate.plusDays(i);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            java.util.Date newDate = dateFormat.parse(String.valueOf(date));
            java.sql.Date sqlDate = new java.sql.Date(newDate.getTime());
            dates.add(sqlDate);
        }
        // 将数据转化为json格式
        String data = "{\"1\": 0, \"2\": 0.1, \"3\": 1.3, \"4\": 5.7, \"5\": 11.1, \"6\": 13.4, \"7\": 13.3, \"8\": " +
                "17.3, \"9\": 18.1, \"10\": 22.8, \"11\": 26.9, \"12\": 21.6, \"13\": 18.3, \"14\": 17.2, \"15\": 11.6, \"16\": 8.3, \"17\": 8.3, \"18\": 7.2, \"19\": 5.6, \"20\": 5.6, \"21\": 2.5, \"22\": 2.1, \"23\": 0.1, \"24\": 0.1}";
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = objectMapper.writeValueAsString(data);
        // 插入数据
        int count = 0;
        for (Date date : dates) {
            PassengerFlow passengerFlow = new PassengerFlow();
            passengerFlow.setId(id);
            passengerFlow.setDate(date);
            passengerFlow.setData(JSONObject.parse(jsonData));

            int result = 0;
//            try {
//                result = passengerFlowMapper.insert(passengerFlow);
//            } catch (BusinessException e) {
//                e.printStackTrace();
//            }
//            count += result;

            try {
                // 判断是否存在对应id和date的记录，如果存在则更新，否则插入
                QueryWrapper<PassengerFlow> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("id", id).eq("date", date);
                int exist = Math.toIntExact(passengerFlowMapper.selectCount(queryWrapper));
                if (exist > 0) {
                    // 更新数据
                    UpdateWrapper<PassengerFlow> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("id", id).eq("date", date)
                            .set("data", JSONObject.parse(jsonData));
                    result = passengerFlowMapper.update(null, updateWrapper);
                } else {
                    // 插入数据
                    result = passengerFlowMapper.insert(passengerFlow);
                }
            } catch (DuplicateKeyException e) {
                // 处理重复键异常，例如更新现有记录
                UpdateWrapper<PassengerFlow> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", id).eq("date", date)
                        .set("data", JSONObject.parse(jsonData));
                result = passengerFlowMapper.update(null, updateWrapper);
            }
            count += result;
        }
        return true;
    }

    @Override
    public List<GetWeekPassengerFlow> getPassengerFlowOfWeek(String id, Date date) {
        QueryWrapper<Store> storeQueryWrapper = new QueryWrapper<>();
        storeQueryWrapper.eq("id", id);
        Store store = storeMapper.selectOne(storeQueryWrapper);
        if(store == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"该店铺不存在");
        }

        List<PassengerFlow> passengerFlowMapperByWeek = passengerFlowMapper.findByWeek(id, date);
        List<GetWeekPassengerFlow> passengerFlowWeek = new ArrayList<>();
        Map<Week, Double> weekMap = new HashMap<>();
        for(PassengerFlow passengerFlow : passengerFlowMapperByWeek){
            GetWeekPassengerFlow weekPassengerFlow = new GetWeekPassengerFlow();
            //设置星期
            LocalDate dateOfSql = passengerFlow.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            DayOfWeek dayOfWeek = dateOfSql.getDayOfWeek();

            Week week = Week.valueOf(dayOfWeek.toString().toUpperCase());
            weekMap.put(week, 0.0);

            String dayOfWeekString = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault());
            weekPassengerFlow.setWeek(dayOfWeekString);

            //顾客流动数
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
            weekPassengerFlow.setFlow(sum);
            passengerFlowWeek.add(weekPassengerFlow);
        }


        // 遍历枚举类型中的所有值，如果对应的值不存在于map中，则在passengerFlowWeek中插入一个值为0的星期
        for (Week week : Week.values()) {
            if (!weekMap.containsKey(week)) {
                GetWeekPassengerFlow weekPassengerFlow = new GetWeekPassengerFlow();
                weekPassengerFlow.setWeek(week.toString());
                weekPassengerFlow.setFlow(0.0);
                passengerFlowWeek.add(weekPassengerFlow);
            }
        }

        // 将passengerFlowWeek转换为流并进行排序，根据Week的顺序排序
//        List<GetWeekPassengerFlow> sortedPassengerFlowWeek = passengerFlowWeek.stream()
//                .sorted((pw1, pw2) -> Week.valueOf(pw1.getWeek()).compareTo(Week.valueOf(pw2.getWeek())))
//                .collect(Collectors.toList());

        return passengerFlowWeek;
    }


}




