package com.backend.intelligent_scheduling_login.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName store
 */
@TableName(value ="store")
@Data
public class Store implements Serializable {
    /**
     * 
     */
    @TableId
    private String id;

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private String address;

    /**
     * 
     */
    private Double size;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}