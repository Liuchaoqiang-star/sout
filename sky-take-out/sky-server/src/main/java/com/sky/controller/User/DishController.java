package com.sky.controller.User;

import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 根据分类id查询起售中的菜品，并带出口味数据。
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    @SuppressWarnings("unchecked")
    public Result<List<DishVO>> list(Long categoryId) {
        log.info("C端根据分类id查询菜品：{}", categoryId);

        // 一个分类对应一份菜品缓存，例如 dish_11 表示分类id为11的菜品列表
        String key = "dish_" + categoryId;

        // 先查Redis，缓存命中就不用再查MySQL
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if (list != null) {
            log.info("命中菜品缓存：{}", key);
            return Result.success(list);
        }

        // Redis没有数据时再查数据库
        list = dishService.listWithFlavor(categoryId);

        // 查询结果放入Redis，后续相同分类的请求可以直接从缓存返回
        redisTemplate.opsForValue().set(key, list);

        return Result.success(list);
    }
}
