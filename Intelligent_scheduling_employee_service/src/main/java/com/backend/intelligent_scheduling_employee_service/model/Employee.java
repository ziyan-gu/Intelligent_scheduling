package com.backend.intelligent_scheduling_employee_service.model;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @TableName employee
 */
@TableName(value ="employee")
@Data
@AllArgsConstructor
@NoArgsConstructor
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
    @TableLogic
    private Integer isdelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}