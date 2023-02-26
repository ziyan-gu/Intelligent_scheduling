package com.backend.dao;

import com.backend.model.Scheduling;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SchedulingDao extends BaseMapper<Scheduling> {
}
