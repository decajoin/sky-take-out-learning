package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 实现密码的 md5 加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus().equals(StatusConstant.DISABLE)) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public void save(EmployeeDTO employeeDTO) {

        Employee employee = new Employee();

        // 由于 EmployeeLoginDTO 的属性 Employee 中都有（属性名一致），所以可以直接使用 BeanUtils.copyProperties 方法进行拷贝
        // 对象属性拷贝，从源 employeeLoginDTO 到目标 employee 中
        BeanUtils.copyProperties(employeeDTO, employee);

        // 设置 Employee 中的其他属性
        // 设置账号的状态，默认正常状态，1 为正常，0 为锁定
        employee.setStatus(StatusConstant.ENABLE);

        // 设置账号的密码，默认密码为 123456，需要进行 md5 加密
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        // 设置账号的创建时间和更新时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // 设置账号的创建人 ID 和更新人 ID
        // 从 ThreadLocal 中获取当前登录的员工 ID，作为创建人和更新人
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }

    /**
     * 分页查询员工
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        // 原理就是基于 MySQL 的 SQL 语句（limit 关键字）进行分页查询
        // 开始分页查询
        // PageHelper 通过先查一遍数据库的 count，并保存到 ThreadLocal 中，然后根据要求会在 SQL 语句中添加 limit 关键字，实现分页查询
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        // 执行分页查询
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);

        long total = page.getTotal();
        List<Employee> records = page.getResult();

        return new PageResult(total, records);
    }

}
