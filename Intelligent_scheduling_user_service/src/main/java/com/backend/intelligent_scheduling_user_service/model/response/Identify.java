package com.backend.intelligent_scheduling_user_service.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Identify {

    private static final long serialVersionUID = 1L;
    private String token;
}
