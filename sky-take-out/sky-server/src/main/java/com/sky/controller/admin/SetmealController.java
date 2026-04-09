package com.sky.controller.admin;


import com.sky.dto.SetmealDTO;
import com.sky.entity.SetmealDish;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController//1. 必须加，标识这是一个控制器并返回JSON
@RequestMapping("/admin/setmeal")//2.类级别路径映射，这一块所有的接口都以这个开头
@Api(tags = "套餐相关接口")//3.用于Swagger生成文档
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @PostMapping   //添加套餐
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        log.info("添加套餐");
        setmealService.saveWithDish(setmealDTO);
        return Result.success();

    }
}
