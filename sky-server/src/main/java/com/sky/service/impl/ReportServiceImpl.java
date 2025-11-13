package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
