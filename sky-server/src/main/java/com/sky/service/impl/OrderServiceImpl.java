package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    AddressBookMapper addressBookMapper;
    @Autowired
    ShoppingCartMapper shoppingCartMapper;
    @Override
    @Transactional
    public OrderSubmitVO order(OrdersSubmitDTO ordersSubmitDTO) {
        OrderSubmitVO orderSubmitVO=new OrderSubmitVO();

        AddressBook addressBook=new AddressBook();
        addressBook.setId(ordersSubmitDTO.getAddressBookId());
        List<AddressBook> list = addressBookMapper.list(addressBook);
        if(list==null||list.size()==0)
            throw new AddressBookBusinessException("地址为空或不存在");

        ShoppingCart shoppingCart=new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.get(shoppingCart);
        if(shoppingCarts==null||shoppingCarts.size()==0)
            throw new ShoppingCartBusinessException("购物车商品为空");
        int number =0;
        BigDecimal amount=BigDecimal.valueOf(0);
        for(ShoppingCart shoppingCart1:shoppingCarts){
            number+=shoppingCart1.getNumber();
            amount.add(shoppingCart1.getAmount());
        }

        Orders orders=new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(new String().valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setUserId(BaseContext.getCurrentId());
        orders.setConsignee(addressBook.getConsignee());
        orders.setAmount(amount);
        orders.setNumber(String.valueOf(number));
        orders.setAddress(addressBook.getDetail());
        orderMapper.insert(orders);

        List<OrderDetail> orderDetailList=new ArrayList<>();
        for(ShoppingCart shoppingCart1:shoppingCarts){
            OrderDetail orderDetail=new OrderDetail();
            BeanUtils.copyProperties(shoppingCart1,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.batchinsert(orderDetailList);
        shoppingCartMapper.delete(shoppingCart);

        orderSubmitVO.setId(orders.getId());
        orderSubmitVO.setOrderTime(LocalDateTime.now());
        orderSubmitVO.setOrderNumber(orders.getNumber());
        orderSubmitVO.getOrderAmount();
        return null;
    }
}
