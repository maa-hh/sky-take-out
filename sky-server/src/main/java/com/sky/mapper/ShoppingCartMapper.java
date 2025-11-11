package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import lombok.Data;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    List<ShoppingCart> get(ShoppingCart shoppingCart);

    void update(ShoppingCart shoppingCart);

    void insert(ShoppingCart shoppingCart);
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void delete(ShoppingCart shoppingCart);
}
