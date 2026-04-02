package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {




    void insertBatch(List<DishFlavor> flavors);


    //根据菜品id来删除口味数据
    @Delete("delete from dish_flavor where dish_id =#{dishId}")
    void deleteByDishID(Long id);


}
