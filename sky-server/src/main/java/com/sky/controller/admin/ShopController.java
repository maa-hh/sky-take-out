package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("admin/shop")
@Slf4j
@Api(tags = "管理端商店相关接口")
public class ShopController {
    @Autowired
    RedisTemplate redisTemplate;
    public static final String status="shop_status";
    @PutMapping("/{s}")
    @ApiOperation(value = "修改店铺营业状态")
    public Result setStatus(@PathVariable String s){
        redisTemplate.opsForValue().set(status,s);
        return Result.success(s);
    }
    @GetMapping("/status")
    @ApiOperation(value = "管理端获取店铺状态")
    public Result<String> getStatus(){
        String status1=(String)redisTemplate.opsForValue().get(status);
        log.info(status1);

        return Result.success(status1);
    }
}
