package com.backend.intelligent_scheduling_user_service.model;

import com.baomidou.mybatisplus.annotation.*;

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
    @TableLogic
    private Integer isdelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}