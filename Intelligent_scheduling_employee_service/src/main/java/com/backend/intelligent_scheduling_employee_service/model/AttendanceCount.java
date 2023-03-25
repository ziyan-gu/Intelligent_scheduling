package com.backend.intelligent_scheduling_employee_service.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName attendance_count
 */
@TableName(value ="attendance_count")
@Data
public class AttendanceCount implements Serializable {
    /**
     * 
     */
    @TableId
    private String id;

    /**
     * 
     */
    private Integer count;

    /**
     * 
     */
    private Integer isdelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}