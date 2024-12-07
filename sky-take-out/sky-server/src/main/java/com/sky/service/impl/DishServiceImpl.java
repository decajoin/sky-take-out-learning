package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品，同时插入对应的口味数据，需要操作两张表: dish, dish_flavor
     * 注解 Transactional 保证事务管理的原子性
     * @param dishDTO
     */
    @SuppressWarnings("AlibabaTransactionMustHaveRollback")
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {

        // DishDTO 中还有口味相关属性，在菜品表中是不需要的
        // 所以需要将 DishDTO 重新封装为 Dish 对象
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        // 向菜品表插入 1 条数据
        dishMapper.insert(dish);

        // 获取 insert 语句生成的主键值
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            // 向口味表插入 n 条数据
            dishFlavorMapper.insertBatch(flavors);

        }

    }
}
