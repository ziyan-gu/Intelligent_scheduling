package com.backend.service;

import org.springframework.transaction.annotation.Transactional;

// 排班算法接口
@Transactional
public interface scheduling_algorithm {
    // 生成班次
    Object generation_shift(String id);
    // 生成排班表
    Object generation_scheduling();
}
