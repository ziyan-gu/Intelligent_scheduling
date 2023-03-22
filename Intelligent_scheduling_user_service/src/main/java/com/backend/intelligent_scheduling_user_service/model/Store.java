package com.backend.intelligent_scheduling_user_service.model;

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

    private String company;
    /**
     * 
     */
    private String address;

    /**
     * 
     */
    private Float size;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}