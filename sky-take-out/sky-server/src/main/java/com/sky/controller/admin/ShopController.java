package com.sky.controller.admin;


import com.sky.constant.StatusConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {

    public static final String SHOP_STATUS = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 修改店铺营业状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("修改店铺营业状态")
    public Result setStatus(@PathVariable("status") Integer status) {
        log.info("修改店铺营业状态：{}", status.equals(StatusConstant.ENABLE) ? "营业中" : "打烊中");

        redisTemplate.opsForValue().set(SHOP_STATUS, status);

        return Result.success();
    }

    /**
     * 查询店铺营业状态
     */
    @GetMapping("/status")
    @ApiOperation("查询店铺营业状态")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(SHOP_STATUS);

        if (status != null) {
            log.info("查询店铺营业状态：{}", status.equals(StatusConstant.ENABLE) ? "营业中" : "打烊中");
        }

        return  Result.success(status);
    }
}
