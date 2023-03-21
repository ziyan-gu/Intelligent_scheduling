package com.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.backend.service.scheduling_algorithm;

@RestController
//@RequestMapping("/user")
public class UserController {

    @Autowired
    private scheduling_algorithm scheduling_algorithm;

    @GetMapping("/getScheduling/{id}")
    public Object getScheduling(@PathVariable String id){
        Object o = scheduling_algorithm.generation_shift(id);
        return o;
    }
}
