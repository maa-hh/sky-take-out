package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
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
    @Autowired
    UserMapper userMapper;
    @Autowired
    WeChatPayUtil weChatPayUtil;
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

        Orders orders=new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(new String().valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setUserId(BaseContext.getCurrentId());
        orders.setConsignee(addressBook.getConsignee());
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
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getByOpenid(String.valueOf(userId));

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }
}
