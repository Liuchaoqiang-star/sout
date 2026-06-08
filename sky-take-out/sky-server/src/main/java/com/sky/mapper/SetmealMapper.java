package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

     void insert(Setmeal setmeal);

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);


    Page<SetmealVO> Query(SetmealPageQueryDTO setmealPageQueryDTO);

    @Select("select* from setmeal where id=#{id}")
    Setmeal getById(Long id);

    void deleteByIdBatch(List<Long> ids);


    void update(Setmeal setmeal);

    /**
     * 动态条件查询套餐，C端商品浏览会按分类和状态查询
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询套餐内菜品选项
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);
}
