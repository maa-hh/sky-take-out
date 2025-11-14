package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    private WorkspaceService workspaceService;
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

    @Override
    public SalesTop10ReportVO salesTop10Statistics(LocalDate begin, LocalDate end) {
        List<GoodsSalesDTO> goodsSalesDTO;
        goodsSalesDTO=orderDetailMapper.dishsalsenum(LocalDateTime.of(begin,LocalTime.MIN),LocalDateTime.of(end,LocalTime.MAX));
        List<String> name;
        List<Integer> num;
//        for(GoodsSalesDTO dto:goodsSalesDTO){
//            name.add(dto.getName());
//            num.add(dto.getNumber());
//        }
        name=goodsSalesDTO.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        num=goodsSalesDTO.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        return SalesTop10ReportVO.builder().
                nameList(StringUtils.join(name,","))
                .numberList(StringUtils.join(num,","))
                .build();
    }

    @Override
    public void export(HttpServletResponse response) {
        LocalDate begin=LocalDate.now().minusDays(30);
        LocalDate end=LocalDate.now().minusDays(1);
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));
        Workbook workbook;
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("template/e1");
        try{
            workbook=new XSSFWorkbook(resourceAsStream);
            Sheet sheet=workbook.getSheetAt(0);
            sheet.getRow(1).getCell(1).setCellValue("时间"+begin+"至"+end);
            sheet.getRow(3).getCell(2).setCellValue(businessData.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(businessData.getOrderCompletionRate());
            sheet.getRow(3).getCell(6).setCellValue(businessData.getNewUsers());

            sheet.getRow(4).getCell(2).setCellValue(businessData.getValidOrderCount());
            sheet.getRow(4).getCell(4).setCellValue(businessData.getUnitPrice());

            for(int i=0;i<30;i++){
                LocalDate date=begin.plusDays(i);
                businessData=workspaceService.getBusinessData(LocalDateTime.of(date,LocalTime.MIN),LocalDateTime.of(date,LocalTime.MAX));
                Row row=workbook.getSheetAt(0).getRow(7+i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            ServletOutputStream outputStream=response.getOutputStream();
            workbook.write(outputStream);
            outputStream.close();
            workbook.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
