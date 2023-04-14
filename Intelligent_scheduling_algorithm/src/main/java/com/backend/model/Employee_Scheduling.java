package com.backend.model;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.util.List;

@Data
public class Employee_Scheduling {
    private String id;
    private List<JSONArray> monday;
    private List<JSONArray> tuesday;
    private List<JSONArray> wednesday;
    private List<JSONArray> thursday;
    private List<JSONArray> friday;
    private List<JSONArray> saturday;
    private List<JSONArray> sunday;
}
