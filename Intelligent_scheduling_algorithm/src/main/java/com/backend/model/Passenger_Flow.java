package com.backend.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.sql.Date;

@Data
@TableName(value = "passenger_flow")
public class Passenger_Flow {
    private String id;
    private Date date;
    private String data;
}
