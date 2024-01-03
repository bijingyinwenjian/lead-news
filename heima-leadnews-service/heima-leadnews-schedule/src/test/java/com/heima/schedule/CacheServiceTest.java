package com.heima.schedule;

import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.service.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Set;

@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class CacheServiceTest {

    @Resource
    private CacheService cacheService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private TaskService taskService;

    @Test
    public void Test1(){
        //在list的左边添加元素
        Long list_001 = stringRedisTemplate.opsForList().leftPush("list_001", "hello,redis1");

        //在list的右边获取元素，并删除
        String list_0011 = stringRedisTemplate.opsForList().rightPop("list_001");
        System.out.println(list_0011);
    }


    @Test
    public void Test2(){
        //在list的左边添加元素
        stringRedisTemplate.opsForZSet().add("zset_key_001", "hello zset 001",1000);
        stringRedisTemplate.opsForZSet().add("zset_key_001", "hello zset 002",8888);
        stringRedisTemplate.opsForZSet().add("zset_key_001", "hello zset 003",7777);
        stringRedisTemplate.opsForZSet().add("zset_key_001", "hello zset 004",10000);

        Set<String> zset_key_001 = stringRedisTemplate.opsForZSet().rangeByScore("zset_key_001", 0, 8888);
        System.out.println(zset_key_001);
    }



    @Test
    public void Test3(){
        Task task = new Task();
        task.setTaskType(3);
        task.setExecuteTime(System.currentTimeMillis() - 10000000);
        String param = "username,password";
        task.setParameters(param.getBytes());
        task.setPriority(3);
        taskService.addTask(task);

    }

    @Test
    public void Test4(){
        Task task = new Task();
        task.setTaskType(12);
        task.setExecuteTime(System.currentTimeMillis()+2000);
        String param = "username,password,email";
        task.setParameters(param.getBytes());
        task.setPriority(12);
        taskService.addTask(task);

    }

}
