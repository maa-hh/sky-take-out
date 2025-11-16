package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Autowired
    WebSocketServer webSocketServer;
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

        return orderSubmitVO;
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
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
        JSONObject jsonObject=new JSONObject();
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
        Map<String,String> m=new HashMap<>();
        m.put("type","1");
        m.put("orderId",ordersDB.getId().toString());
        m.put("content",outTradeNo);
        String message=JSON.toJSONString(m);
        webSocketServer.sendToAllClient(message);
    }

    @Override
    public void reminder(Long id) {
        Orders order=orderMapper.getById(id);
        if(order==null)
            throw new OrderBusinessException("该订单不存在");
        Map<String,String> m=new HashMap<>();
        m.put("type","2");
        m.put("orderId",order.getId().toString());
        m.put("content",order.getNumber());
        String message=JSON.toJSONString(m);
        webSocketServer.sendToAllClient(message);
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders> re=orderMapper.conditionSearch(ordersPageQueryDTO);
        PageResult pageResult=new PageResult(re.getTotal(), re.getResult());
        return pageResult;
    }

    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO orderStatisticsVO=new OrderStatisticsVO();
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 7退款
        orderStatisticsVO.setToBeConfirmed(orderMapper.orderstatusNum(2));
        orderStatisticsVO.setDeliveryInProgress(orderMapper.orderstatusNum(3));
        orderStatisticsVO.setDeliveryInProgress(orderMapper.orderstatusNum(4));
        return orderStatisticsVO;
    }

    @Override
    public OrderVO details(Long id) {
        OrderVO orderVO=new OrderVO();
        List<OrderDetail> l=orderDetailMapper.getById(id);
        StringBuilder s=new StringBuilder();
        for(OrderDetail orderDetail:l){
            s.append(orderDetail.getName());
            s.append(" ");
        }
        orderVO.setOrderDetailList(l);
        orderVO.setOrderDishes(s.toString());
        return orderVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        orderMapper.confirm(ordersConfirmDTO.getId(),Orders.CONFIRMED);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        orderMapper.reject(ordersRejectionDTO.getId(),ordersRejectionDTO.getRejectionReason(),Orders.CANCELLED);
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        orderMapper.cancel(ordersCancelDTO.getId(),ordersCancelDTO.getCancelReason(),Orders.CANCELLED);
    }

    @Override
    public void delivery(Long id) {
        orderMapper.delivery(id,Orders.DELIVERY_IN_PROGRESS);
    }

    @Override
    public void complete(Long id) {
        orderMapper.complete(id,Orders.COMPLETED);
    }

}
