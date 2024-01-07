package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class TaskServiceImpl extends ServiceImpl<TaskinfoMapper, Taskinfo> implements TaskService {

    @Resource
    private TaskinfoLogsMapper taskinfoLogsMapper;

    @Resource
    private TaskinfoMapper taskinfoMapper;

    @Resource
    private CacheService cacheService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 添加任务
     * @param task   任务对象
     * @return
     */
    @Override
    public long addTask(Task task) {
        //1.添加任务到数据库中
        boolean success = addTaskToDb(task);
        if (success) {
            //2.添加任务到redis
            addTaskToCache(task);
        }
        return task.getTaskId();
    }

    /**
     * 取消任务
     * @param taskId        任务id
     * @return
     */
    @Override
    public boolean cancelTask(long taskId) {
        boolean flag = false;

        //删除任务，更新日志
        Task task = updateDb(taskId, ScheduleConstants.CANCELLED);

        //删除redis的数据
        if (task != null) {
            removeTaskFromCache(task);
            flag = true;
        }
        return flag;
    }

    /**
     * 按照类型和优先级来拉取任务
     * @param type
     * @param priority
     * @return
     */
    @Override
    public Task poll(int type, int priority) {
        Task task = null;
        try {
            String key = type + "_" + priority;
            String taskJson = cacheService.lRightPop(ScheduleConstants.TOPIC + key);
            if (StringUtils.isNotBlank(taskJson)) {
                task = JSON.parseObject(taskJson, Task.class);
                updateDb(task.getTaskId(),ScheduleConstants.EXECUTED);
            }
        } catch (Exception e) {
            log.error("拉取任务失败:{}",e);
        }

        return task;
    }

    /**
     * 定时刷新zset将数据导入list
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void refresh(){
        // 获取分布式锁
        String token = cacheService.tryLock("FUTURE_TASK_SYNC", 3000 * 10);
        if (StringUtils.isNotBlank(token)){
            System.out.println(System.currentTimeMillis() / 1000 + "执行了定时任务");
            Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
            for (String futureKey : futureKeys) {
                String topicKey = ScheduleConstants.TOPIC + futureKey.substring(futureKey.indexOf("_"));
                Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());
                if (!tasks.isEmpty()){
                    cacheService.refreshWithPipeline(futureKey,topicKey,tasks);
                    System.out.println("成功的将" + futureKey + "下的当前需要执行的任务数据刷新到" + topicKey + "下");
                }
            }
        }
    }

    @PostConstruct
    @Scheduled(cron = "0 */5 * * * ?")
    public void reloadData(){
        // 清理缓存数据
        clearCache();
        log.info("数据库同步数据到redis");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE,5);
        List<Taskinfo> allTasks = taskinfoMapper.selectList(Wrappers.<Taskinfo>lambdaQuery().lt(Taskinfo::getExecuteTime, calendar.getTime()));
        if (allTasks != null && allTasks.size() > 0){
            for (Taskinfo taskinfo : allTasks) {
                Task task = new Task();
                BeanUtils.copyProperties(taskinfo,task);
                task.setExecuteTime(taskinfo.getExecuteTime().getTime());
                addTaskToCache(task);
            }
        }
    }

    private void clearCache() {
        Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
        Set<String> topicKeys = cacheService.scan(ScheduleConstants.TOPIC + "*");
        cacheService.delete(futureKeys);
        cacheService.delete(topicKeys);
    }

    //删除redis的数据
    private void removeTaskFromCache(Task task) {
        String key = task.getTaskType() + "_" + task.getPriority();
        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            cacheService.lRemove(ScheduleConstants.TOPIC + key, 0, JSON.toJSONString(task));
        } else {
            cacheService.zRemove(ScheduleConstants.FUTURE + key, JSON.toJSONString(task));
        }

    }

    //删除任务，更新日志
    private Task updateDb(long taskId, int executed) {
        Task task = null;
        try {
            removeById(taskId);
            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
            taskinfoLogs.setStatus(executed);
            taskinfoLogsMapper.updateById(taskinfoLogs);
            BeanUtils.copyProperties(taskinfoLogs, task);
            task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());
        } catch (Exception e) {
            log.error("删除任务，更新日志失败:{}", e);
        }

        return null;
    }

    private void addTaskToCache(Task task) {
        String key = task.getTaskType() + "_" + task.getPriority();
        //获取5分钟之后的时间  毫秒值
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        long nextScheduleTime = calendar.getTimeInMillis();

        //2.1 如果任务的执行时间小于等于当前时间，存入list
        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            cacheService.lLeftPush(ScheduleConstants.TOPIC + key, JSON.toJSONString(task));
        } else if (task.getExecuteTime() <= nextScheduleTime) {
            //2.2 如果任务的执行时间大于当前时间 && 小于等于预设时间（未来5分钟） 存入zset中
            cacheService.zAdd(ScheduleConstants.FUTURE + key, JSON.toJSONString(task), task.getExecuteTime());
        }
    }

    private boolean addTaskToDb(Task task) {
        Boolean flag = false;
        try {
            Taskinfo taskinfo = new Taskinfo();
            // 保存taskinfo
            BeanUtils.copyProperties(task, taskinfo);
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
            this.save(taskinfo);
            task.setTaskId(taskinfo.getTaskId());

            // 保存taskinfolog
            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            BeanUtils.copyProperties(task, taskinfoLogs);
            taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);
            taskinfoLogs.setVersion(1);
            taskinfoLogs.setExecuteTime(new Date(task.getExecuteTime()));
            taskinfoLogsMapper.insert(taskinfoLogs);
            flag = true;
        } catch (BeansException e) {
            log.error("添加任务失败:{}", e);
        }

        return flag;
    }
}
