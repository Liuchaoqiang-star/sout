package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;

import java.util.List;

public interface SetmealService {

    void deleteBatch(List<Long> ids);

    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);



    void saveWithDish(SetmealDTO setmealDTO);

    void updateWithDish(SetmealDTO setmealDTO);

    void startOrStop(Integer status, Long id);

    /**
     * C端根据条件查询起售中的套餐
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * C端根据套餐id查询套餐内包含的菜品
     */
    List<DishItemVO> getDishItemById(Long id);
}
