package com.backend.intelligent_scheduling_login.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName employee
 */
@TableName(value ="employee")
@Data
public class Employee implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer keyId;

    /**
     * 
     */
    private String id;

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private String email;

    /**
     * 
     */
    private Integer position;

    /**
     * 
     */
    private String store;

    /**
     * 
     */
    private String password;

    /**
     * 
     */
    private Object preferenceValue;

    /**
     * 是否删除
     */
    private Integer isdelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}