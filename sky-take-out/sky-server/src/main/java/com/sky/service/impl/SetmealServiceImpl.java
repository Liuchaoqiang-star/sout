package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
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
    public void deleteBatch(List<Long> ids) {
        //根据ids去数据库去查询，是不是在起售卖中
        for (Long id : ids) {
            Setmeal setmeal=setmealMapper.getById(id);
            if(setmeal.getStatus()==1){
                throw new DeletionNotAllowedException("起售中的泰餐不能删除哦！");
            }

        }
        //没什么问题就删除套餐
        setmealMapper.deleteByIdBatch(ids);
        //删除菜品关系
        setmealDishMapper.deleteBySetmealIds(ids);

    }














    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {

        // 1. 设置分页参数（PageHelper）
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());

        // 2. 调用 Mapper（这时 Mapper 应该会报错，因为你还没定义它）
        Page<SetmealVO> page = setmealMapper.Query(setmealPageQueryDTO);

        // 3. 拿到结果，塞进 PageResult 并返回
        return new PageResult(page.getTotal(), page.getResult());
    }









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
