package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    UserMapper userMapper;
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        TurnoverReportVO turnoverReportVO=new TurnoverReportVO();
        List<LocalDate> date=new ArrayList<>();
        List<Double> money=new ArrayList<>();
        for(LocalDate p=begin;p!=end;p.plusDays(1))
            date.add(p);
        turnoverReportVO.setDateList(StringUtils.join(date,","));
        for(LocalDate p=begin;p!=end;p.plusDays(1)){
            Map m=new HashMap<>();
            m.put("begin",LocalDateTime.of(p, LocalTime.MIN));
            m.put("end",LocalDateTime.of(p,LocalTime.MAX));
            m.put("status", Orders.COMPLETED);
            Double sum=orderMapper.sumbymap(m);
            if(sum==null) sum=0.0;
            money.add(sum);
        }
        turnoverReportVO.setTurnoverList(StringUtils.join(money,","));
        return turnoverReportVO;
    }

    @Override
    @Transactional
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        UserReportVO userReportVO=new UserReportVO();

        TurnoverReportVO turnoverReportVO=new TurnoverReportVO();
        List<LocalDate> date=new ArrayList<>();
        List<Integer> total=new ArrayList<>();
        List<Integer> newuser=new ArrayList<>();
        for(LocalDate p=begin;p!=end;p.plusDays(1))
            date.add(p);
        turnoverReportVO.setDateList(StringUtils.join(date,","));

        userReportVO.setDateList(StringUtils.join(date,","));

        for(LocalDate p=begin;p!=end;p.plusDays(1)){
            Map m=new HashMap<>();
            m.put("begin",null);
            m.put("end",LocalDateTime.of(p,LocalTime.MAX));
            Integer totalsum=userMapper.userStatistics(m);
            if(totalsum==null) totalsum=0;
            LocalDateTime.of(p, LocalTime.MIN);
            Integer newsum=userMapper.userStatistics(m);
            if(newsum==null) newsum=0;
            total.add(totalsum);
            newuser.add(newsum);
        }
        userReportVO.setNewUserList(StringUtils.join(newuser,","));
        userReportVO.setTotalUserList(StringUtils.join(total,","));
        return userReportVO;
    }

    @Override
    @Transactional
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {
        OrderReportVO orderReportVO=new OrderReportVO();
        List<LocalDate> date=new ArrayList<>();
        int total=0;
        int realsum=0;
        List<Integer> ordersum=new ArrayList<>();
        List<Integer> realordersum=new ArrayList<>();

        for(LocalDate p=begin;p!=end;p.plusDays(1))
            date.add(p);
        orderReportVO.setDateList(StringUtils.join(date,","));

        for(LocalDate p=begin;p!=end;p.plusDays(1)){
            Map m=new HashMap<>();
            m.put("begin",LocalDateTime.of(p,LocalTime.MIN));
            m.put("end",LocalDateTime.of(p,LocalTime.MAX));
            m.put("status",null);
            int s1=orderMapper.ordersumBymap(m);
            m.put("status",Orders.COMPLETED);
            int s2=orderMapper.ordersumBymap(m);
            total+=s1;
            realsum+=s2;
            ordersum.add(s1);
            realordersum.add(s2);
        }
        orderReportVO.setOrderCountList(StringUtils.join(ordersum,","));
        orderReportVO.setValidOrderCountList(StringUtils.join(realordersum,","));
        orderReportVO.setValidOrderCount(total);
        orderReportVO.setTotalOrderCount(realsum);
        if(total==0)
            orderReportVO.setOrderCompletionRate(0.0);
        else
            orderReportVO.setOrderCompletionRate(realsum*1.0/total);
        return orderReportVO;
    }
}
