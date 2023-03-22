package com.backend.intelligent_scheduling_user_service.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName fixed_rules
 */
@TableName(value ="fixed_rules")
@Data
public class FixedRules implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer ruleId;

    /**
     * 
     */
    private String ruleType;

    /**
     * 
     */
    private String admin;

    /**
     * 
     */
    private Object ruleValue;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}