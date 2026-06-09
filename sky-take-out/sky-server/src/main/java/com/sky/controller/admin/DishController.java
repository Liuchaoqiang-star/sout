package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

//菜品管理
@RequestMapping("/admin/dish")
@RestController()
@Api(tags="菜品相关接口")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品:",dishDTO );
        dishService.saveWithFlavor(dishDTO);

        // 新增菜品会影响某个分类下的菜品列表，需要清理对应分类缓存
        cleanCache("dish_" + dishDTO.getCategoryId());
        return Result.success();
    }
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询:{}",dishPageQueryDTO);
        PageResult pageResult=dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    @DeleteMapping
    @ApiOperation("菜品批量删除")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("菜品批量删除:{}",ids);
        // 删除前先查出这些菜品涉及的分类，删除后就查不到了
        List<Long> categoryIds = dishService.getCategoryIdsByIds(ids);
        dishService.deleteBathch(ids);

        // 只清理被删除菜品所在分类的缓存，不影响其他分类
        cleanCacheByCategoryIds(categoryIds);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据id查询菜品：{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品：{}", dishDTO);
        List<Long> categoryIds = new ArrayList<>();
        if (dishDTO.getId() != null) {
            // 先记录修改前所属分类，防止菜品被移动到新分类后旧分类缓存没清掉
            categoryIds.addAll(dishService.getCategoryIdsByIds(Collections.singletonList(dishDTO.getId())));
        }
        dishService.updateWithFlavor(dishDTO); // 补全入参

        if (dishDTO.getCategoryId() != null) {
            categoryIds.add(dishDTO.getCategoryId());
        }
        // 修改菜品名称、价格、口味、状态、分类等信息后，相关分类缓存都要清理
        cleanCacheByCategoryIds(categoryIds);
        return Result.success();
    }
    @GetMapping("/list")
    public Result<List<DishVO>> list(Long categoryId) {
        log.info("根据分类id查询菜品：{}", categoryId);
        List<DishVO> list = dishService.listWithFlavor(categoryId); // 这里的 service 方法名要跟你写的一致
        return Result.success(list);
    }

    /**
     * 清理菜品缓存。
     *
     * pattern可以是精确key，例如 dish_11；
     * 也可以是通配符，例如 dish_*，表示清理所有分类下的菜品缓存。
     */
    private void cleanCache(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && keys.size() > 0) {
            redisTemplate.delete(keys);
        }
    }

    private void cleanCacheByCategoryIds(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.size() == 0) {
            return;
        }

        categoryIds.stream()
                .distinct()
                .forEach(categoryId -> cleanCache("dish_" + categoryId));
    }

}
