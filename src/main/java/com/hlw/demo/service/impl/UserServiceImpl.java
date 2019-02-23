package com.hlw.demo.service.impl;

import com.hlw.demo.bean.User;
import com.hlw.demo.mapper.UserMapper;
import com.hlw.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;
    @Override
    public User getUser(Integer userId) {
        return userMapper.getUser(userId);
    }
}
