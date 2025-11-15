package com.sky.controller.admin;

import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminSetmealController")
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @PostMapping
    @ApiOperation(value = "新增套餐")
    @CacheEvict(cacheNames = "setmealCache",key = "#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO){
        setmealService.savewithDish(setmealDTO);
        return Result.success();
    }
    @GetMapping("/page")
    @ApiOperation(value = "分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        PageResult pageResult=setmealService.pagequery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }
    @DeleteMapping
    @ApiOperation(value = "批量删除")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result deleteids(@RequestParam List<Long> ids){
        setmealService.deleteids(ids);
        return Result.success();
    }
    @GetMapping("/list/{id}")
    @ApiOperation(value = "根据id查套餐")
    public Result<Setmeal> getById(@PathVariable long id){
        Setmeal setmeal= setmealService.getById(id);
        return Result.success(setmeal);
    }
    @PutMapping
    @ApiOperation(value = "修改套餐")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO){
        setmealService.update(setmealDTO);
        return Result.success();
    }
    @PostMapping("/status/{status}")
    @ApiOperation(value = "修改套餐状态")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result status(@PathVariable Integer status,Long id){
        SetmealDTO setmealDTO=new SetmealDTO();
        setmealDTO.setStatus(status);
        setmealDTO.setId(id);
        setmealService.update(setmealDTO);
        return Result.success();
    }
}
