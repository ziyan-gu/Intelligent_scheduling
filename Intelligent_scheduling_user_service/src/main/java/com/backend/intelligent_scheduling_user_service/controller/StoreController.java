package com.backend.intelligent_scheduling_user_service.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.backend.intelligent_scheduling_user_service.common.BaseResponse;
import com.backend.intelligent_scheduling_user_service.common.ErrorCode;
import com.backend.intelligent_scheduling_user_service.common.ResultUtils;
import com.backend.intelligent_scheduling_user_service.exception.BusinessException;
import com.backend.intelligent_scheduling_user_service.model.Store;
import com.backend.intelligent_scheduling_user_service.model.request.ModifyStoreRequest;
import com.backend.intelligent_scheduling_user_service.service.StoreService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/store")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @ApiOperation("根据管理员id查询店铺")
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

    @ApiOperation("根据店铺id删除店铺")
    @DeleteMapping("/delete/{id}")
    public BaseResponse<String> deleteStoresById(@PathVariable String id){
        if(id == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if (StringUtils.isAnyBlank(id)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"存在空格");
        }

        boolean result = storeService.deleteStoreById(id);
        if (!result){
            throw  new BusinessException(ErrorCode.NULL_ERROR,"删除失败");
        }
        return ResultUtils.success("ok");

    }

    @ApiOperation("根据店铺id修改店铺信息")
    @PutMapping("/modify/{id}")
    public BaseResponse<String> modifyStoresById(@PathVariable String id,
                                                 @RequestBody ModifyStoreRequest modifyStoreRequest){
        if(modifyStoreRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }

        String storeName = modifyStoreRequest.getName();
        String storeAddress = modifyStoreRequest.getAddress();
        Float storeSize = modifyStoreRequest.getSize();

        boolean result = storeService.ModifyStoreById(id, storeName, storeAddress, storeSize);
        if (!result){
            throw  new BusinessException(ErrorCode.NULL_ERROR,"修改失败");
        }
        return ResultUtils.success("ok");

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
