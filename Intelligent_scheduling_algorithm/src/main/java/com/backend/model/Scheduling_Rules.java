package com.backend.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "scheduling_rules")
public class Scheduling_Rules {
    @TableId(value = "rule_id", type = IdType.AUTO)
    private int ruleId;
    private String ruleType;
    private String store;
    private String ruleValue;
}
