package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService  {
    public void  saveWithFlavor(DishDTO dishDTO);



    //菜品分页查询
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);



    //菜品批量删除
    void deleteBathch(List<Long> ids);



    //根据ID来查询菜品和对应的口味数据
    DishVO getByIdWithFlavor(Long id);
     //


    //根据ID修改菜品基本信息和对应的口味信息
    void updateWithFlavor(DishDTO dishDTO);

    List<DishVO> listWithFlavor(Long categoryId);
}
