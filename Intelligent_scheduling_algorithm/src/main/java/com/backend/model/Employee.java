package com.backend.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Employee {
    @TableId(value = "key_id", type = IdType.AUTO)
    private int keyId;
    private String id;
    private String name;
    private String email;
    private int position;
    private String store;
    private String preferenceValue;
}
