package com.hlw.demo;

import com.hlw.demo.bean.User;
import com.hlw.demo.mapper.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class) // SpringJUnit支持，由此引入Spring-Test框架支持！
@SpringBootTest
public class MapperTest {
    @Autowired
    UserMapper userMapper;

    @Test
    public void test(){
        User user = userMapper.getUser(1);
        System.out.println(user);
    }
}
