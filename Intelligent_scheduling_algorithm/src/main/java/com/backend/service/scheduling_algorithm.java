package com.backend.service;

import com.alibaba.fastjson.JSONObject;
import com.backend.model.Scheduling;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 排班算法接口
@Transactional
public interface scheduling_algorithm {
    // 生成班次
    Object generation_shift(String id);
    // 生成排班表
    Object generation_scheduling(Scheduling scheduling, List<Integer> up_down);
}
