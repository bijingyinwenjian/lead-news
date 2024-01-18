package com.heima.kafka.controller;

import com.alibaba.fastjson.JSON;
import com.heima.kafka.domain.User;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class HelloController {

    @Resource
    private KafkaTemplate<String,String> kafkaTemplate;

    @GetMapping("/hello")
    public String hello(){
        kafkaTemplate.send("itcast-topic","黑马程序员");
        return "ok";
    }

    @GetMapping("/hello1")
    public String hello1(){
        User user = new User();
        user.setName("张三");
        user.setAge(13);
        kafkaTemplate.send("user-topic", JSON.toJSONString(user));
        return "ok";
    }

}
