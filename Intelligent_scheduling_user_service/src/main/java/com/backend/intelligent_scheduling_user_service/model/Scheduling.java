package com.backend.intelligent_scheduling_user_service.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 
 * @TableName scheduling
 */
@TableName(value ="scheduling")
@Data
public class Scheduling implements Serializable {
    /**
     * 
     */
    private String id;

    /**
     * 
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date date;

    /**
     * 
     */
    private Object data;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}