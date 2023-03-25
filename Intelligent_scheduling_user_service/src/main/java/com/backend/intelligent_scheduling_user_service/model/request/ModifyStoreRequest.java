package com.backend.intelligent_scheduling_user_service.model.request;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class ModifyStoreRequest {
    private String name;

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
