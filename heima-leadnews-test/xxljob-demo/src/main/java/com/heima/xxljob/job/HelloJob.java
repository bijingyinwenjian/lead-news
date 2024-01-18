package com.heima.xxljob.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class HelloJob {


    @XxlJob("demoJobHandler")
    public void demoJobHandler() throws Exception {
        System.out.println("demoJobHandler");

    }
}
