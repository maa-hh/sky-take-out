package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("user/shoppingCart")
@Api(tags="购物车")
@Slf4j
public class ShoppingCartController {
    @Autowired
    ShoppingCartService shoppingCartService;
    @PostMapping("/add")
    @ApiOperation(value = "添加商品到购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }
    @GetMapping("/list")
    @ApiOperation(value = "查看该用户购物车内容")
    public Result<List<ShoppingCart>> show(){
        List<ShoppingCart> shoppingCarts=shoppingCartService.show();
        return Result.success(shoppingCarts);

    }
    @DeleteMapping("/clean")
    @ApiOperation(value = "清空购物车")
    public Result delete (){
        shoppingCartService.deleteById();
        return Result.success();
    }
}
