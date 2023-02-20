package com.backend.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "employee_preference", autoResultMap = true)
public class Employee_Preference {
    private String preferenceType;
    private String employee;
    private String preferenceValue;
}
