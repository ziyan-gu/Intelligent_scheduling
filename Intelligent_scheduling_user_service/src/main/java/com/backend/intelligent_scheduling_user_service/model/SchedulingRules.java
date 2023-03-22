package com.backend.intelligent_scheduling_user_service.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.language.bm.RuleType;

/**
 * 
 * @TableName scheduling_rules
 */
@TableName(value ="scheduling_rules")
@Data
public class SchedulingRules implements Serializable {
    /**
     * 
     */

    public SchedulingRules(String ruleType){
        this.ruleType = ruleType;
    }
    @TableId(type = IdType.AUTO)
    private Integer ruleId;

    /**
     * 
     */
    private String ruleType;

    /**
     * 
     */
    private String store;

    /**
     * 
     */
    private Object ruleValue;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}