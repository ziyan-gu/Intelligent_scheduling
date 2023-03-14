package com.backend.service;

import com.alibaba.fastjson.JSONObject;
import com.backend.model.Scheduling;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 排班算法接口
@Transactional
public interface scheduling_algorithm {
    Object generation_shift(String id);
}
