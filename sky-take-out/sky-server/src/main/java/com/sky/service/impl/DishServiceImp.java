package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
@Slf4j
public class DishServiceImp implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private DishService dishService;


    @Autowired
    SetmealDishMapper setmealDishMapper;

    @Override
    @Transactional(rollbackFor = Exception.class) //要么全成功要么全失败
    public void saveWithFlavor(DishDTO dishDTO) {
        //向菜品表插入1条数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);

        //获取insert语句生成的主键值
        Long dishID = dish.getId();
        //向口味表插入N条数据
        List<DishFlavor> flavors=dishDTO.getFlavors();
        if (flavors!=null&&flavors.size()>0){
            flavors.forEach(dishFlavor->{
                dishFlavor.setDishId(dishID);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }
    //菜品分页查询
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page=dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public void deleteBathch(List<Long> ids) {
        //判断当前菜品是否能够删除---是否存在起售中的菜品？？
        for (Long id:ids){
            Dish dish=dishMapper.getById(id);
            if(dish.getStatus()==1){
                throw  new DeletionNotAllowedException("当前菜品处于起售中，不能删除");
            }
        }
        //判断当前菜品是否能够删除---是否被套餐关联了？？
       List<Long> setmealIds= setmealDishMapper.getSetmealIdsByDishIds(ids);
        if(setmealIds!=null&&setmealIds.size()>0){
            throw  new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //删除菜品表中的菜品数据
       for (Long id:ids){
           dishMapper.deleteById(id);
           //删除菜品关联的口味数据
           dishFlavorMapper.deleteByDishID(id);
       }
    }

}
