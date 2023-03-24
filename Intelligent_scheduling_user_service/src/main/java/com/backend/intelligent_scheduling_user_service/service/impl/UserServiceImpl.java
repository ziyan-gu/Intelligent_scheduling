package com.backend.intelligent_scheduling_user_service.service.impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.backend.intelligent_scheduling_user_service.common.ErrorCode;
import com.backend.intelligent_scheduling_user_service.common.UserInfoCheckUtil;
import com.backend.intelligent_scheduling_user_service.exception.BusinessException;
import com.backend.intelligent_scheduling_user_service.mapper.EmployeeMapper;
import com.backend.intelligent_scheduling_user_service.mapper.StoreMapper;
import com.backend.intelligent_scheduling_user_service.mapper.UserMapper;
import com.backend.intelligent_scheduling_user_service.model.Employee;
import com.backend.intelligent_scheduling_user_service.model.Store;
import com.backend.intelligent_scheduling_user_service.model.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.backend.intelligent_scheduling_user_service.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author 86136
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2023-02-20 23:21:58
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    public String USER_LOGIN_STATE = "userLoginState";

    @Resource
    private UserMapper userMapper;
    @Resource
    private FixedRulesServiceImpl fixedRulesService;

    @Resource
    private SchedulingRulesServiceImpl schedulerRulesService;
    @Resource
    private StoreMapper storeMapper;

    @Resource
    private EmployeeMapper employeeMapper;

    private final static String SALT = "wx";


    @Override
    public String userRegister(String account, String password, String checkPassword, String name) {
        if (StringUtils.isAnyBlank(account, password, checkPassword, name)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        //账户不能包含特殊字符
//        if(UserInfoCheckUtil.isValidUserAccount(account)){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户包含特殊字符");
//        }

        //邮箱匹配
        if (!UserInfoCheckUtil.isValidEmail(account)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }


        //密码和校验密码相同
        if (!password.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码输入不相同");
        }

        //账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", account);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该用户已存在");
        }

        //公司已经创建个数
        QueryWrapper<User> queryWrapperCount = new QueryWrapper<>();
        int companyCount = userMapper.countAdminUsers(queryWrapperCount);

        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());

        //添加默认排班规则

        String adminCount = String.valueOf(companyCount + 1);
        try {
            fixedRulesService.setDefaultValueFixed(adminCount);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        //插入数据
        User user = new User();
        user.setAccount(account);
        user.setPassword(encryptPassword);
        user.setType("admin");
        user.setId(adminCount);
        user.setName(name);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.NO_AUTH, "保存失败");
        }
        return user.getAccount();
    }

    @Override
    public User userLogin(String account, String password, HttpServletRequest request) {
        //1.校验
        if (StringUtils.isAnyBlank(account, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "存在空格");
        }
        if (account.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账户过短");
        }
        if (password.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }


        //账户不能包含特殊字符
//        if(UserInfoCheckUtil.isValidUserAccount(account)){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"存在特殊字符");
//        }


        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        //查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", account);
        queryWrapper.eq("password", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if (user == null) {
            //log.info("user login failed, account cannot match password");
            return null;
        }

        User safetyUser = getSafeUser(user);

        //4.记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);

        return safetyUser;
    }

    @Override
    public User getSafeUser(User oringinUser) {
        if (oringinUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "该用户不存在");
        }
        User safetyUser = new User();
        safetyUser.setId(oringinUser.getId());
        safetyUser.setType(oringinUser.getType());
        safetyUser.setName(oringinUser.getName());
        safetyUser.setAccount(oringinUser.getAccount());
        return safetyUser;
    }

    @Override
    public Integer changePassword(String account, String password, String newPassword) {
        QueryWrapper<User> objectQueryWrapper = new QueryWrapper<>();
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        objectQueryWrapper.eq("account", account).eq("password", encryptPassword);
        Long ifExist = userMapper.selectCount(objectQueryWrapper);
        if (ifExist == 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"未查询到该账户");
        }
        //修改密码
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        String encryptNewPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        updateWrapper.eq("account", account).set("password", encryptNewPassword);
        int result = userMapper.update(null, updateWrapper);
        return result;

    }

    @Override
    public Boolean isUser(String account, String password) {
        QueryWrapper<User> queryWrapper = Wrappers.query();
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        queryWrapper.eq("account", account).eq("password", encryptPassword);
        Long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean isEmployee(String account, String password) {
        QueryWrapper<Employee> queryWrapper = Wrappers.query();
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        queryWrapper.eq("email", account).eq("password",encryptPassword);
        Long count = employeeMapper.selectCount(queryWrapper);
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Transactional
    public String addStore(String account, String name, String company, String address, Float size) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", account).eq("name", name);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该用户已存在");
        }

        String password = "123456";
        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());

        //查询分店个数
//        QueryWrapper<Store> queryWrapperStore = new QueryWrapper<>();
//
//        queryWrapper.like("id", company + "_");
//
//        long countStore = storeMapper.selectCount(queryWrapperStore) + 1;
//        if (countStore <= 0){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "为查询到相关主公司");
//        }
//
//        String id = company + "_" + countStore;

        //查询分店最大值
        String maxId = userMapper.findMaxIdByPrefix(company);
        String id = null;
        if (maxId == null) {
            // 如果不存在，则返回 prefix_1
            id =  company + "_1";
        } else {
            // 否则在当前编号基础上加 1
            int max = Integer.parseInt(maxId.substring(maxId.lastIndexOf("_") + 1));
            id =  company + "_" + (max + 1);
        }

        //添加默认值
        try {
            schedulerRulesService.setDefaultValueScheduling(id);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        //插入数据
        User user = new User();
        user.setAccount(account);
        user.setPassword(encryptPassword);
        user.setType("store");
        user.setId(id);
        user.setName(name);

        Store store = new Store();
        store.setName(name);
        store.setCompany(company);
        store.setSize(size);
        store.setAddress(address);
        store.setId(id);

        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "user保存失败");
        }

        int saveResultForStore = storeMapper.insert(store);
        if(saveResultForStore == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"store保存失败");
        }
        return user.getAccount();
    }


}




