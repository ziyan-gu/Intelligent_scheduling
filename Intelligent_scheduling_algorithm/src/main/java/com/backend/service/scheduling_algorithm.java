package com.backend.service;

import org.springframework.transaction.annotation.Transactional;


// 排班算法接口
@Transactional
public interface scheduling_algorithm {
    //id:店铺id
    //返回值为排班数据
    Object generation_shift(String id);
}
