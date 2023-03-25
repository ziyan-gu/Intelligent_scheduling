package com.backend.intelligent_scheduling_user_service.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 
 * @TableName passenger_flow
 */
@TableName(value ="passenger_flow")
@Data
public class PassengerFlow implements Serializable {
    /**
     * 
     */
    @TableId
    private String id;

    /**
     * 
     */

    private Date date;

    /**
     * 
     */
    private Object data;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}