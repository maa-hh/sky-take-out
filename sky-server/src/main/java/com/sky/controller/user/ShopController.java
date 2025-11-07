package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("user/shop")
@Slf4j
@Api(tags = "管理端商店相关接口")
public class ShopController {
    @Autowired
    RedisTemplate redisTemplate;
    public static final String status="shop_status";
    @GetMapping("/status")
    @ApiOperation(value = "管理端获取店铺状态")
    public Result<Integer> getStatus(){
        return Result.success((Integer) redisTemplate.opsForValue().get(status));
    }
}
