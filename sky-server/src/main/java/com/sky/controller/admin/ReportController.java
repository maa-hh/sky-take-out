package com.sky.controller.admin;

import com.sky.dto.GoodsSalesDTO;
import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/report")
@Api(tags = "数据统计")
public class ReportController {
    @Autowired
    ReportService reportService;
    @GetMapping("/turnoverStatistics")
    @ApiOperation(value = "营业额统计")
    public Result<TurnoverReportVO> turnoverStatistics(@DateTimeFormat(pattern = "YYYY-mm-dd")LocalDate begin ,
                                                       @DateTimeFormat(pattern = "YYYY-mm-dd")LocalDate end ){
        TurnoverReportVO turnoverReportVO=reportService.turnoverStatistics(begin,end);
        return Result.success(turnoverReportVO);
}
    @GetMapping("/userStatistics")
    @ApiOperation(value = "用户数量统计")
    public Result<UserReportVO> userStatistics(@DateTimeFormat(pattern = "YYYY-mm-dd")LocalDate begin ,
                                               @DateTimeFormat(pattern = "YYYY-mm-dd")LocalDate end){
        UserReportVO userReportVO=reportService.userStatistics(begin,end);
        return Result.success(userReportVO);
    }
    @GetMapping("/orderStatistics")
    @ApiOperation(value = "订单数量统计")
    public Result<OrderReportVO> orderStatistics(@DateTimeFormat(pattern = "YYYY-mm-dd")LocalDate begin ,
                                                 @DateTimeFormat(pattern = "YYYY-mm-dd")LocalDate end){
        OrderReportVO orderReportVO=reportService.orderStatistics(begin,end);
        return Result.success(orderReportVO);
    }
    @GetMapping("/salesStatistics")
    @ApiOperation(value = "销量数量统计")
    public Result<SalesTop10ReportVO> salesTop10Statistics(@DateTimeFormat(pattern = "YYYY-mm-dd")LocalDate begin ,
                                                           @DateTimeFormat(pattern = "YYYY-mm-dd")LocalDate end){
        SalesTop10ReportVO goodsSales=reportService.salesTop10Statistics(begin,end);
        return Result.success(goodsSales);
    }
    @GetMapping("/export")
    @ApiOperation(value = "导出xsl运营数据报表")
    public void export(HttpServletResponse response){
        reportService.export(response);
    }
}
