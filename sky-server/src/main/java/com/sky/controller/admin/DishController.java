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
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品")
@Slf4j
public class DishController {
    @Autowired
    DishService dishService;
    @Autowired
    RedisTemplate redisTemplate;
    @PostMapping
    @ApiOperation(value = "新增菜品")
    @CacheEvict(cacheNames = "dishcache",allEntries = true)
    public Result<String> save(@RequestBody DishDTO dishDTO){
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }
    @GetMapping("/page")
    @ApiOperation(value = "分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishDTO){
        PageResult pageResult=dishService.page(dishDTO);
        return Result.success(pageResult);
    }
    @DeleteMapping()
    @ApiOperation(value = "批量删除")
    @CacheEvict(cacheNames = "dishcache",allEntries = true)
    public Result<String> deleteDish(@RequestParam List<Long> ids){
        dishService.deleteBatch(ids);
       // deleteRedis("dish_*");
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation(value = "根据菜品id回显信息")
    public Result<DishVO> getDishId(@PathVariable Long id){
        log.info("");
        DishVO dishVO= dishService.getDishId(id);
        return Result.success(dishVO);
    }
    @PutMapping()
    @ApiOperation(value = "修改菜品及其口味")
    @CacheEvict(cacheNames = "dishcache",allEntries = true)
    public Result updateWithFlavor(@RequestBody DishDTO dishDTO){
        dishService.updateWithFlavor(dishDTO);
        //deleteRedis("dish_*");
        return Result.success();
    }

    private void deleteRedis(String pattern){
        redisTemplate.delete(pattern);
    }
}
