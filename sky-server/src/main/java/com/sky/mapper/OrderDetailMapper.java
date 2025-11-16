package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderDetailMapper {
    void insert(OrderDetail orderDetail);

    void batchinsert(List<OrderDetail> orderDetailList);

    List<GoodsSalesDTO> dishsalsenum(LocalDateTime begin, LocalDateTime end);
    @Select("select * from order_detail where order_id=#{id}")
    List<OrderDetail> getById(Long id);
}
