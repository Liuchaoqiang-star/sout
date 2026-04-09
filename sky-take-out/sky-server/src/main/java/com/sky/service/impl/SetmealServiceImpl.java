package com.sky.service.impl;

import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SetmealServiceImpl implements SetmealService {
    private final DishMapper dishMapper;

    public SetmealServiceImpl(DishMapper dishMapper) {
        this.dishMapper = dishMapper;
    }
    @Autowired
    private SetmealMapper setmealMapper; // 注意是小写开头的变量名

    @Autowired
    private SetmealDishMapper setmealDishMapper; // 注意是小写开头的变量名

    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        // 1. 先存主表：套餐表（这里存完，id 就回填到 setmeal 对象里了）
        // 注意：应该是 setmealMapper，而不是 SetmealMapper(类名)
        setmealMapper.insert(setmeal);

        // 2. 获取刚才生成的套餐 ID
        Long setmealId = setmeal.getId();

        // 3. 处理关联的菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            // 遍历，给每个关联菜品塞入套餐 ID
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });

            // 4. 批量存入关联表：setmeal_dish 表
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }
}
