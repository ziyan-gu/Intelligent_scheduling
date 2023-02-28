package com.backend.intelligent_scheduling_login.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.backend.intelligent_scheduling_login.common.BaseResponse;
import com.backend.intelligent_scheduling_login.common.ErrorCode;
import com.backend.intelligent_scheduling_login.common.ResultUtils;
import com.backend.intelligent_scheduling_login.exception.BusinessException;
import com.backend.intelligent_scheduling_login.model.Store;
import com.backend.intelligent_scheduling_login.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/store")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @GetMapping("/id/{id}")
    public BaseResponse<List<Store>> getStoresById(@PathVariable String id){
        if(id == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if (StringUtils.isAnyBlank(id)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"存在空格");
        }

        List<Store> stores = storeService.getStoresById(id);
        if (stores.size() == 0){
            throw  new BusinessException(ErrorCode.NULL_ERROR,"未查询到数据");
        }
        return ResultUtils.success(stores);

    }

//    @GetMapping("/name/{name}")
//    public BaseResponse<List<Store>> getStoresByName(@PathVariable String name){
//        if(name == null){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
//        }
//        if (StringUtils.isAnyBlank(name)){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"存在空格");
//        }
//
//        List<Store> stores = storeService.getStoresByName(name);
//        if (stores.size() == 0){
//            throw  new BusinessException(ErrorCode.NULL_ERROR,"未查询到数据");
//        }
//        return ResultUtils.success(stores);
//
//    }
}
