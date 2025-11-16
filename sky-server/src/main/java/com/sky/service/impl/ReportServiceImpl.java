package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    private WorkspaceService workspaceService;
    /**
     * 工具：生成 [begin, end] 日期列表
     */
    private List<LocalDate> buildDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> list = new ArrayList<>();
        for (LocalDate p = begin; !p.isAfter(end); p = p.plusDays(1)) {
            list.add(p);
        }
        return list;
    }

    // =====================================================================
    // 1. 营业额统计
    // =====================================================================
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {

        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();

        List<LocalDate> dateList = buildDateList(begin, end);
        List<Double> turnoverList = new ArrayList<>();

        turnoverReportVO.setDateList(StringUtils.join(dateList, ","));

        for (LocalDate p : dateList) {
            Map<String, Object> param = new HashMap<>();
            param.put("begin", LocalDateTime.of(p, LocalTime.MIN));
            param.put("end", LocalDateTime.of(p, LocalTime.MAX));
            param.put("status", Orders.COMPLETED);

            Double sum = orderMapper.sumamount(param);
            turnoverList.add(sum == null ? 0.0 : sum);
        }

        turnoverReportVO.setTurnoverList(StringUtils.join(turnoverList, ","));

        log.info("Turnover statistics: {}", JSON.toJSONString(turnoverReportVO));
        return turnoverReportVO;
    }

    // =====================================================================
    // 2. 用户统计
    // =====================================================================
    @Override
    @Transactional
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {

        UserReportVO userReportVO = new UserReportVO();

        List<LocalDate> dateList = buildDateList(begin, end);
        userReportVO.setDateList(StringUtils.join(dateList, ","));

        List<Integer> totalList = new ArrayList<>();
        List<Integer> newList = new ArrayList<>();

        for (LocalDate p : dateList) {

            // 总用户数（从开始到当前日）
            Map<String, Object> totalMap = new HashMap<>();
            totalMap.put("begin", null);
            totalMap.put("end", LocalDateTime.of(p, LocalTime.MAX));

            Integer totalsum = userMapper.userStatistics(totalMap);
            totalList.add(totalsum == null ? 0 : totalsum);

            // 当天新增用户
            Map<String, Object> newMap = new HashMap<>();
            newMap.put("begin", LocalDateTime.of(p, LocalTime.MIN));
            newMap.put("end", LocalDateTime.of(p, LocalTime.MAX));

            Integer newUser = userMapper.userStatistics(newMap);
            newList.add(newUser == null ? 0 : newUser);
        }

        userReportVO.setTotalUserList(StringUtils.join(totalList, ","));
        userReportVO.setNewUserList(StringUtils.join(newList, ","));

        return userReportVO;
    }

    // =====================================================================
    // 3. 订单统计
    // =====================================================================
    @Override
    @Transactional
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {

        OrderReportVO orderReportVO = new OrderReportVO();

        List<LocalDate> dateList = buildDateList(begin, end);
        orderReportVO.setDateList(StringUtils.join(dateList, ","));

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        int totalAll = 0;
        int validAll = 0;

        for (LocalDate p : dateList) {
            Map<String, Object> param = new HashMap<>();
            param.put("begin", LocalDateTime.of(p, LocalTime.MIN));
            param.put("end", LocalDateTime.of(p, LocalTime.MAX));

            // 当日总订单数
            param.put("status", null);
            Integer total = orderMapper.ordersumBymap(param);
            total = total == null ? 0 : total;

            // 当日有效订单数
            param.put("status", Orders.COMPLETED);
            Integer valid = orderMapper.ordersumBymap(param);
            valid = valid == null ? 0 : valid;

            orderCountList.add(total);
            validOrderCountList.add(valid);

            totalAll += total;
            validAll += valid;
        }

        orderReportVO.setOrderCountList(StringUtils.join(orderCountList, ","));
        orderReportVO.setValidOrderCountList(StringUtils.join(validOrderCountList, ","));

        orderReportVO.setTotalOrderCount(totalAll);
        orderReportVO.setValidOrderCount(validAll);

        orderReportVO.setOrderCompletionRate(
                totalAll == 0 ? 0.0 : (validAll * 1.0 / totalAll)
        );

        return orderReportVO;
    }

    // =====================================================================
    // 4. 销售前 10 统计
    // =====================================================================
    @Override
    public SalesTop10ReportVO salesTop10Statistics(LocalDate begin, LocalDate end) {

        List<GoodsSalesDTO> goodsSalesDTO =
                orderDetailMapper.dishsalsenum(
                        LocalDateTime.of(begin, LocalTime.MIN),
                        LocalDateTime.of(end, LocalTime.MAX)
                );
//        类名::实例方法 会被推断为 (对象实例) -> 对象实例.方法()，因此 map 会对流中每个对象自动调用 getName()。
        List<String> nameList = goodsSalesDTO.stream()
                .map(GoodsSalesDTO::getName)
                .collect(Collectors.toList());

        List<Integer> numberList = goodsSalesDTO.stream()
                .map(GoodsSalesDTO::getNumber)
                .collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }

    @Override
    public void export(HttpServletResponse response) {

        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);

        // ---- 获取 30 天总计数据 ----
        BusinessDataVO totalData = workspaceService.getBusinessData(
                LocalDateTime.of(begin, LocalTime.MIN),
                LocalDateTime.of(end, LocalTime.MAX)
        );

        InputStream resourceAsStream =
                this.getClass().getClassLoader().getResourceAsStream("template/e1.xlsx");

        if (resourceAsStream == null) {
            throw new RuntimeException("模板文件 template/e1.xlsx 未找到！");
        }

        try (Workbook workbook = new XSSFWorkbook(resourceAsStream);
             ServletOutputStream outputStream = response.getOutputStream()) {

            Sheet sheet = workbook.getSheetAt(0);

            // 标题日期
            sheet.getRow(1).getCell(1).setCellValue("时间 " + begin + " 至 " + end);

            // 总计数据
            sheet.getRow(3).getCell(2).setCellValue(totalData.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(totalData.getOrderCompletionRate());
            sheet.getRow(3).getCell(6).setCellValue(totalData.getNewUsers());

            sheet.getRow(4).getCell(2).setCellValue(totalData.getValidOrderCount());
            sheet.getRow(4).getCell(4).setCellValue(totalData.getUnitPrice());

            // ---- 循环写入每天的数据 ----
            for (int i = 0; i < 30; i++) {

                LocalDate date = begin.plusDays(i);

                BusinessDataVO dayData = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX)
                );

                Row row = sheet.getRow(7 + i);

                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(dayData.getTurnover());
                row.getCell(3).setCellValue(dayData.getValidOrderCount());
                row.getCell(4).setCellValue(dayData.getOrderCompletionRate());
                row.getCell(5).setCellValue(dayData.getUnitPrice());
                row.getCell(6).setCellValue(dayData.getNewUsers());
            }

            // ---- 【必须加】下载头，否则浏览器不能正常下载 ----
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=report.xlsx");

            workbook.write(outputStream);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("导出报表失败：" + e.getMessage());
        }
    }

}
