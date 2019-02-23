package com.hlw.demo.mapper;

import com.hlw.demo.bean.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

@Mapper
@Component("UserMapper")
public interface UserMapper {
    User getUser(@Param("userId") Integer userId);
}
