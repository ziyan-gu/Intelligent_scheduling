package com.backend.intelligent_scheduling_employee_service.service;

import com.backend.intelligent_scheduling_employee_service.model.Store;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 86136
* @description 针对表【store】的数据库操作Service
* @createDate 2023-03-01 23:34:59
*/
public interface StoreService extends IService<Store> {

    /**
     * 查询店铺名
     * @param id  用户id
     * @return 返回店铺名
     */
    List<String> getStoreByUserId(String id);
}
