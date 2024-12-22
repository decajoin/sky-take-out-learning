package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 指定区间的营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        // 当前集合用于存放从 begin 到 end 之间每天的日期
        List<LocalDate> dataList = new ArrayList<>();
        dataList.add(begin);
        for (LocalDate date = begin; !date.isEqual(end); ) {
            date = date.plusDays(1);
            dataList.add(date);
        }
        // 将 dataList 转换为字符串，以逗号分隔
        String dataListJoin = StringUtils.join(dataList, ",");


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
        // 将 turnoverList 转换为字符串，以逗号分隔
        String turnoverListJoin = StringUtils.join(turnoverList, ",");


        return new TurnoverReportVO(dataListJoin, turnoverListJoin);
    }
}
