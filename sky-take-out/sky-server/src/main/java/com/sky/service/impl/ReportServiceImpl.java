package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
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

    @Autowired
    private UserMapper userMapper;

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
}
