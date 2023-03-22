package com.backend.intelligent_scheduling_user_service.service.impl;

import com.backend.intelligent_scheduling_user_service.mapper.StoreMapper;
import com.backend.intelligent_scheduling_user_service.model.Store;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.backend.intelligent_scheduling_user_service.service.StoreService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
* @author 86136
* @description 针对表【store】的数据库操作Service实现
* @createDate 2023-02-28 23:22:31
*/
@Service
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store>
    implements StoreService{

    @Resource
    private StoreMapper storeMapper;
    @Override
    public List<Store> getStoresById(String id) {

        QueryWrapper<Store> queryWrapper = new QueryWrapper<>();

        queryWrapper.like("id", id + "_");
        List<Store> stores = storeMapper.selectList(queryWrapper);

        return stores;
    }

    @Override
    public List<Store> getStoresByName(String name) {
        QueryWrapper<Store> queryWrapper = new QueryWrapper<>();

        queryWrapper.like("name", name);
        List<Store> stores = storeMapper.selectList(queryWrapper);

        return stores;
    }
}




