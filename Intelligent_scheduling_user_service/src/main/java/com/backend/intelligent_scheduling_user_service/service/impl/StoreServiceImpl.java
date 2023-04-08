package com.backend.intelligent_scheduling_user_service.service.impl;

import com.backend.intelligent_scheduling_user_service.common.ErrorCode;
import com.backend.intelligent_scheduling_user_service.exception.BusinessException;
import com.backend.intelligent_scheduling_user_service.mapper.EmployeeMapper;
import com.backend.intelligent_scheduling_user_service.mapper.SchedulingRulesMapper;
import com.backend.intelligent_scheduling_user_service.mapper.StoreMapper;
import com.backend.intelligent_scheduling_user_service.mapper.UserMapper;
import com.backend.intelligent_scheduling_user_service.model.Employee;
import com.backend.intelligent_scheduling_user_service.model.SchedulingRules;
import com.backend.intelligent_scheduling_user_service.model.Store;
import com.backend.intelligent_scheduling_user_service.model.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.backend.intelligent_scheduling_user_service.service.StoreService;
import io.swagger.annotations.Api;
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
    private EmployeeMapper employeeMapper;
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
//        if(resultStore == 0){
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Store表删除失败");
//        }
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("user.id", id);
        int resultUser = userMapper.delete(userWrapper);
//        if(resultUser == 0){
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"User表删除失败");
//        }
        QueryWrapper<SchedulingRules> scheduleWrapper = new QueryWrapper<>();
        scheduleWrapper.eq("store", id);
        int resultSchedule = schedulingRulesMapper.delete(scheduleWrapper);

        QueryWrapper<Employee> employeeQueryWrapper = new QueryWrapper<>();
        employeeQueryWrapper.eq("employee.store",id);
        int resultEmployee = employeeMapper.delete(employeeQueryWrapper);

        if(resultStore == 0){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Store表删除失败");
        }
        if(resultUser == 0){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"User表删除失败");
        }
        if(resultSchedule == 0){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Schedule表删除失败");
        }
//        if(resultEmployee == 0){
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Schedule表删除失败");
//        }
        return true;
    }

    @Override
    public boolean ModifyStoreById(String id, String name, String address, Float size) {
        //是否存在
        QueryWrapper<Store> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        Long count = storeMapper.selectCount(wrapper);
        if (count == null || count == 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"未查询到改店铺");
        }

        //更新信息
        UpdateWrapper<Store> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.eq("id", id).set("name", name).
                set("address", address).
                set("size", size);
        Store store = new Store();
        int update = storeMapper.update(store, objectUpdateWrapper);
        if(update == 0){
            return false;
        }
        return true;
    }

    @Override
    public Store getStoreByStoreId(String id) {
        QueryWrapper<Store> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        Store store = storeMapper.selectOne(wrapper);
        if(store == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"未查询到该店铺");
        }
        return store;
    }
}




