package com.heima.schedule;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
    public void Test1() {
        //在list的左边添加元素
        Long list_001 = stringRedisTemplate.opsForList().leftPush("list_001", "hello,redis1");

        //在list的右边获取元素，并删除
        String list_0011 = stringRedisTemplate.opsForList().rightPop("list_001");
        System.out.println(list_0011);
    }


    @Test
    public void Test2() {
        //在list的左边添加元素
        stringRedisTemplate.opsForZSet().add("zset_key_001", "hello zset 001", 1000);
        stringRedisTemplate.opsForZSet().add("zset_key_001", "hello zset 002", 8888);
        stringRedisTemplate.opsForZSet().add("zset_key_001", "hello zset 003", 7777);
        stringRedisTemplate.opsForZSet().add("zset_key_001", "hello zset 004", 10000);

        Set<String> zset_key_001 = stringRedisTemplate.opsForZSet().rangeByScore("zset_key_001", 0, 8888);
        System.out.println(zset_key_001);
    }


    @Test
    public void Test3() {
        Task task = new Task();
        task.setTaskType(3);
        task.setExecuteTime(System.currentTimeMillis() - 10000000);
        String param = "username,password";
        task.setParameters(param.getBytes());
        task.setPriority(3);
        taskService.addTask(task);

    }

    @Test
    public void Test4() {
        Task task = new Task();
        task.setTaskType(12);
        task.setExecuteTime(System.currentTimeMillis() + 2000);
        String param = "username,password,email";
        task.setParameters(param.getBytes());
        task.setPriority(12);
        taskService.addTask(task);
    }

    @Test
    public void testPiple1() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            Task task = new Task();
            task.setTaskType(1001);
            task.setPriority(1);
            task.setExecuteTime(new Date().getTime());
            cacheService.lLeftPush("1001_1", JSON.toJSONString(task));
        }
        System.out.println("耗时" + (System.currentTimeMillis() - start));
    }

    @Test
    public void testPiple2() {
        long start = System.currentTimeMillis();
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (int i = 0; i < 10000; i++) {
                Task task = new Task();
                task.setTaskType(1001);
                task.setPriority(1);
                task.setExecuteTime(new Date().getTime());
                connection.lPush("1001_1".getBytes(), JSON.toJSONString(task).getBytes());
            }
            return null;
        });
        System.out.println("耗时" + (System.currentTimeMillis() - start));
    }



    @Test
    public void testPiple11() {
        String s = cacheService.tryLock("1", System.currentTimeMillis());
        System.out.println(s);
        String lock = cacheService.tryLock("1", System.currentTimeMillis());
        System.out.println(lock);
    }

    @Resource
    private TaskinfoMapper taskinfoMapper;

    @Test
    public void testPiple121() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE,5);
        System.out.println(calendar);
        List<Taskinfo> allTasks = taskinfoMapper.selectList(Wrappers.<Taskinfo>lambdaQuery().lt(Taskinfo::getExecuteTime, calendar.getTime()));
        System.out.println(allTasks);
    }


}
