package com.backend.intelligent_scheduling_user_service.service;

import com.backend.intelligent_scheduling_user_service.model.Store;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 86136
* @description 针对表【store】的数据库操作Service
* @createDate 2023-02-28 23:22:31
*/
public interface StoreService extends IService<Store> {

    /**
     * 查询所有店铺
     * @return 返回店铺信息
     */
    List<Store> getStoresById(String id);

    /**
     * 根据品牌名字查询所有店铺
     * @return 返回店铺信息
     */
    List<Store> getStoresByName(String name);


}
