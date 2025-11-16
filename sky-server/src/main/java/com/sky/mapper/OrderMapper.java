package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);
    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);
    @Select("select * from orders where status=#{status} and order_time<#{ordertime}")
    List<Orders> getStatusOrdertime(Integer status, LocalDateTime ordertime);
    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    Double sumbymap(Map m);

    Integer ordersumBymap(Map m);

    Page<Orders> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    Integer orderstatusNum(int i);
    @Update("update orders set status = #{status} where id = #{id}")
    void confirm(@Param("id") Long id,@Param("status") Integer status);

    @Update("update orders set rejection_reason = #{rejectionReason}, status = #{status} where id = #{id}")
    void reject(@Param("id") Long id, @Param("rejectionReason") String rejectionReason,@Param("status") Integer status);

    @Update("update orders set cancel_reason = #{cancelReason}, status = #{status} where id = #{id}")
    void cancel(@Param("id") Long id, @Param("cancelReason") String cancelReason,@Param("status") Integer status);

    @Update("update orders set status = #{status} where id = #{id}")
    void delivery(@Param("id") Long id,@Param("status") Integer status);

    @Update("update orders set status = #{status} where id = #{id}")
    void complete(@Param("id") Long id,@Param("status") Integer status);

    Double sumamount(Map<String, Object> m);
}
