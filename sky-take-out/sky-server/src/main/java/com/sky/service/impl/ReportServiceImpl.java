package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 指定区间的营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        // 当前集合用于存放从 begin 到 end 之间每天的日期
        List<LocalDate> dataList = getDateList(begin, end);

        // 查询每一天的营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dataList) {
            // 将 LocalDate 转换为 LocalDateTime
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED);

            // 指定区间的营业额统计
            Double turnover = orderMapper.sumByMap(map);
            // 如果营业额为空，则赋值为0
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        // 将 dataList 转换为字符串，以逗号分隔
        String dataListJoin = StringUtils.join(dataList, ",");
        // 将 turnoverList 转换为字符串，以逗号分隔
        String turnoverListJoin = StringUtils.join(turnoverList, ",");


        return new TurnoverReportVO(dataListJoin, turnoverListJoin);
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {

        // 当前集合用于存放从 begin 到 end 之间每天的日期
        List<LocalDate> dataList = getDateList(begin, end);

        // 存放每天新增的用户数量
        List<Integer> newUserList = new ArrayList<>();

        // 存放总的用户数量
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dataList) {
            // 将 LocalDate 转换为 LocalDateTime
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("endTime", endTime);

            // 总的用户数量
            Integer totalUser = userMapper.countByMap(map);

            map.put("beginTime", beginTime);
            // 每天新增的用户数量
            Integer newUser = userMapper.countByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }

        // 将 dataList 转换为字符串，以逗号分隔
        String dataListJoin = StringUtils.join(dataList, ",");
        // 将 totalUserList 转换为字符串，以逗号分隔
        String totalUserListJoin = StringUtils.join(totalUserList, ",");
        // 将 newUserList 转换为字符串，以逗号分隔
        String newUserListJoin = StringUtils.join(newUserList, ",");

        return new UserReportVO(dataListJoin, totalUserListJoin, newUserListJoin);

    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {

        // 当前集合用于存放从 begin 到 end 之间每天的日期
        List<LocalDate> dataList = getDateList(begin, end);

        // 当前集合用于存放从 begin 到 end 之间每天的订单总数
        List<Integer> totalOrderCountList = new ArrayList<>();

        // 当前集合用于存放从 begin 到 end 之间每天的有效订单数
        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate date : dataList) {
            // 将 LocalDate 转换为 LocalDateTime
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);

            // 每天订单总数
            Integer totalOrderCount = orderMapper.countByMap(map);
            totalOrderCountList.add(totalOrderCount);

            // 每天有效订单数
            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = orderMapper.countByMap(map);
            validOrderCountList.add(validOrderCount);
        }

        // 计算时间区间内的订单总数
        Integer totalOrderCount = totalOrderCountList.stream().reduce(Integer::sum).get();

        // 计算时间区间内的有效订单数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        // 计算订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = (double) validOrderCount / totalOrderCount;
        }

        // 将 dataList 转换为字符串，以逗号分隔
        String dataListJoin = StringUtils.join(dataList, ",");
        // 将 totalOrderCountList 转换为字符串，以逗号分隔
        String totalOrderCountListJoin = StringUtils.join(totalOrderCountList, ",");
        // 将 validOrderCountList 转换为字符串，以逗号分隔
        String validOrderCountListJoin = StringUtils.join(validOrderCountList, ",");

        return new OrderReportVO(dataListJoin, totalOrderCountListJoin, validOrderCountListJoin, totalOrderCount, validOrderCount, orderCompletionRate);
    }

    /**
     * 销量 TOP10 统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {

        // 将 LocalDate 转换为 LocalDateTime
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);

        // 通过 stream 流获得 Top10 的名称集合
        List<String> nameList = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());

        // 通过 stream 流获得 Top10 的数量集合
        List<Integer> numberList = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        // 将 nameList 转换为字符串，以逗号分隔
        String nameListJoin = StringUtils.join(nameList, ",");
        // 将 numberList 转换为字符串，以逗号分隔
        String numberListJoin = StringUtils.join(numberList, ",");

        return new SalesTop10ReportVO(nameListJoin, numberListJoin);
    }

    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {

        // 当前集合用于存放从 begin 到 end 之间每天的日期
        List<LocalDate> dataList = new ArrayList<>();
        dataList.add(begin);
        for (LocalDate date = begin; !date.isEqual(end); ) {
            date = date.plusDays(1);
            dataList.add(date);
        }

        return dataList;
    }

    /**
     * 导出营业数据
     * @param httpServletResponse
     */
    @Override
    public void exportBusinessData(HttpServletResponse httpServletResponse) {
        // 1. 查询数据库，获得营业数据 -- 查询最近 30 天的营业数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        // 2. 通过 POI 将数据写入 Excel 文件
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/business_data_template.xlsx");

        // 基于模版文件创建一个新的 Excel 工作簿
        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);

            // 获取 Sheet 页
            XSSFSheet sheet1 = excel.getSheet("Sheet1");

            // 填充数据 -- 时间
            sheet1.getRow(1).getCell(1).setCellValue("时间： " + dateBegin + " 至 " + dateEnd);
            // 填充数据 -- 营业额
            sheet1.getRow(3).getCell(2).setCellValue("营业额： " + businessDataVO.getTurnover());
            // 填充数据 -- 订单完成率
            sheet1.getRow(3).getCell(4).setCellValue("订单完成率： " + businessDataVO.getOrderCompletionRate());
            // 填充数据 -- 新增用户数
            sheet1.getRow(3).getCell(6).setCellValue("新增用户数： " + businessDataVO.getNewUsers());
            // 填充数据 -- 有效订单数
            sheet1.getRow(4).getCell(2).setCellValue("有效订单数： " + businessDataVO.getValidOrderCount());
            // 填充数据 -- 平均客单价
            sheet1.getRow(4).getCell(4).setCellValue("平均客单价： " + businessDataVO.getUnitPrice());

            // 填充数据 -- 明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                // 查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                sheet1.getRow(i + 7).getCell(1).setCellValue(date.toString());
                sheet1.getRow(i + 7).getCell(2).setCellValue(businessData.getTurnover());
                sheet1.getRow(i + 7).getCell(3).setCellValue(businessData.getValidOrderCount());
                sheet1.getRow(i + 7).getCell(4).setCellValue(businessData.getOrderCompletionRate());
                sheet1.getRow(i + 7).getCell(5).setCellValue(businessData.getUnitPrice());
                sheet1.getRow(i + 7).getCell(6).setCellValue(businessData.getNewUsers());
            }

            // 3. 通过输出流将 Excel 文件输出到浏览器
            ServletOutputStream out = httpServletResponse.getOutputStream();
            excel.write(out);


            // 4. 关闭流
            out.close();
            excel.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }


}
