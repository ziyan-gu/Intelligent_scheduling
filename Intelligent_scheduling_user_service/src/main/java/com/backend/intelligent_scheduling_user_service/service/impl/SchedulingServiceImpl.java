package com.backend.intelligent_scheduling_user_service.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.backend.intelligent_scheduling_user_service.common.ErrorCode;
import com.backend.intelligent_scheduling_user_service.exception.BusinessException;
import com.backend.intelligent_scheduling_user_service.model.response.GetProcessedLayoutData;
import com.backend.intelligent_scheduling_user_service.model.response.GetSchedulingByIdResponse;
import com.backend.intelligent_scheduling_user_service.model.response.GetAllProcessedLayoutResponse;
import com.backend.intelligent_scheduling_user_service.service.AttendanceCountService;
import com.backend.intelligent_scheduling_user_service.service.EmployeeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.backend.intelligent_scheduling_user_service.model.Scheduling;
import com.backend.intelligent_scheduling_user_service.service.SchedulingService;
import com.backend.intelligent_scheduling_user_service.mapper.SchedulingMapper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
* @author 86136
* @description 针对表【scheduling】的数据库操作Service实现
* @createDate 2023-03-20 23:27:05
*/
@Service
public class SchedulingServiceImpl extends ServiceImpl<SchedulingMapper, Scheduling>
    implements SchedulingService{

    @Autowired
    private SchedulingMapper schedulingMapper;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AttendanceCountService attendanceCountService;
    @Override
    public Object getScheduleByIdAndDate(String id, Date date) {
        QueryWrapper<Scheduling> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("date", date);
        Scheduling scheduling = schedulingMapper.selectOne(wrapper);
        return scheduling.getData();
    }

    @Override
    public String changeScheduleByIdAndDate(String id, Date date, Object data) throws JsonProcessingException {

        //数据格式处理
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = objectMapper.writeValueAsString(data);

        //是否存在
//        QueryWrapper<Scheduling> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("id", id).eq("date", date);
//        Long count = schedulingMapper.selectCount(queryWrapper);
//        if(count == 0){
//            Scheduling s = new Scheduling();
//            s.setId(id);
//            s.setDate(date);
//            s.setData(jsonData);
//            int insert = schedulingMapper.insert(s);
//            if(insert == 0){
//                throw new BusinessException(ErrorCode.PARAMS_ERROR,"原数据不存在，并且在尝试保存为新数据时失败");
//            }
//            attendanceCountService.updateAttendanceCount(s);
//            return "插入新数据成功";
//        }

        //修改
        UpdateWrapper<Scheduling> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", id).eq("date", date);
        Scheduling scheduling = new Scheduling();
        scheduling.setData(jsonData);


        int result = schedulingMapper.update(scheduling, wrapper);
        if (result == 0){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"保存失败，请重试");
        }
        return "保存成功";
    }

    @Override
    public List<GetSchedulingByIdResponse> getScheduleById(String id){
        QueryWrapper<Scheduling> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        List<Scheduling> schedulings = schedulingMapper.selectList(wrapper);
        if (schedulings.size() == 0){
            throw new BusinessException(ErrorCode.NULL_ERROR,"未找到相关排班");
        }

        schedulings.forEach(scheduling -> scheduling.setData(JSONObject.parse((String) scheduling.getData())));
        List<GetSchedulingByIdResponse> responses = new ArrayList<>();

        for (Scheduling scheduling : schedulings) {
            GetSchedulingByIdResponse response = new GetSchedulingByIdResponse();

            java.util.Date date = scheduling.getDate();
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());

            response.setDate(sqlDate);
            response.setData(scheduling.getData());
            responses.add(response);
        }
        return responses;
    }

    @Override
    public List<GetAllProcessedLayoutResponse> getProcessedScheduleById(String id) throws ParseException, IOException {
        //查询是否存在排班
        QueryWrapper<Scheduling> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        List<Scheduling> schedulings = schedulingMapper.selectList(wrapper);
        if (schedulings.size() == 0){
            throw new BusinessException(ErrorCode.NULL_ERROR,"未找到相关排班");
        }

        List<GetAllProcessedLayoutResponse> allProcessedLayouts = new ArrayList<>();
        // 处理每个日期的排班数据
        for (Scheduling scheduling : schedulings) {
            java.util.Date utilDate = scheduling.getDate();
            String dateString = new SimpleDateFormat("yyyy-MM-dd").format(utilDate);
            java.sql.Date sqlDate = java.sql.Date.valueOf(dateString);

            List<GetProcessedLayoutData> processedLayouts = new ArrayList<>();

//            Map<String, Object> data = new ObjectMapper().readValue((JsonParser) scheduling.getData(), new TypeReference<Map<String, Object>>(){});
            String jsonData = (String) scheduling.getData();
            JsonNode jsonNode = new ObjectMapper().readTree(jsonData);
            Map<String, Object> data = new ObjectMapper().convertValue(jsonNode, new TypeReference<Map<String, Object>>(){});


            for (Map.Entry<String, Object> entry : data.entrySet()) {
//                Object[] values = (Object[]) entry.getValue();
//                int start = (int) values[0];
//                int end = (int) values[1];
//                String employeeId = (String) values[2];
                if(Objects.equals(entry.getKey(), "total")){
                    continue;
                }
                List<Object> valuesList = (ArrayList<Object>) entry.getValue();
                int start = (int) valuesList.get(0);
                int end = (int) valuesList.get(1);
                String employeeId = (String) valuesList.get(2);

                String name = ""; // 从员工表中获取名称
                processedLayouts.add(new GetProcessedLayoutData(start, end, employeeId, name));
            }
            allProcessedLayouts.add(new GetAllProcessedLayoutResponse(sqlDate, processedLayouts));
        }

        return allProcessedLayouts;


    }


}




