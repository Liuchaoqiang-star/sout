package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;

public interface SetmealService {

    void deleteBatch(List<Long> ids);

    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);



    void saveWithDish(SetmealDTO setmealDTO);

    void updateWithDish(SetmealDTO setmealDTO);
}
