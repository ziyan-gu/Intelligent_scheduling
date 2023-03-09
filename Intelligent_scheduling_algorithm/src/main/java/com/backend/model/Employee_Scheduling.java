package com.backend.model;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.util.List;

@Data
public class Employee_Scheduling {
    private String id;
    List<JSONArray> monday;
    List<JSONArray> tuesday;
    List<JSONArray> wednesday;
    List<JSONArray> thursday;
    List<JSONArray> friday;
    List<JSONArray> saturday;
    List<JSONArray> sunday;
}
