package com.backend.intelligent_scheduling_user_service.service.impl;

import com.backend.intelligent_scheduling_user_service.common.ErrorCode;
import com.backend.intelligent_scheduling_user_service.exception.BusinessException;
import com.backend.intelligent_scheduling_user_service.mapper.SchedulingRulesMapper;
import com.backend.intelligent_scheduling_user_service.mapper.StoreMapper;
import com.backend.intelligent_scheduling_user_service.mapper.UserMapper;
import com.backend.intelligent_scheduling_user_service.model.SchedulingRules;
import com.backend.intelligent_scheduling_user_service.model.Store;
import com.backend.intelligent_scheduling_user_service.model.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.backend.intelligent_scheduling_user_service.service.StoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Resource
    private UserMapper userMapper;
    @Resource
    private SchedulingRulesMapper schedulingRulesMapper;
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

    @Override
    @Transactional
    public boolean deleteStoreById(String id) {
        QueryWrapper<Store> storeWrapper = new QueryWrapper<>();
        storeWrapper.eq("store.id", id);
        int resultStore = storeMapper.delete(storeWrapper);
        if(resultStore == 0){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Store表删除失败");
        }
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("user.id", id);
        int resultUser = userMapper.delete(userWrapper);
        if(resultUser == 0){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"User表删除失败");
        }
        QueryWrapper<SchedulingRules> scheduleWrapper = new QueryWrapper<>();
        scheduleWrapper.eq("store", id);
        int resultSchedule = schedulingRulesMapper.delete(scheduleWrapper);
        if(resultSchedule == 0){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"User表删除失败");
        }
        return true;
    }
}




