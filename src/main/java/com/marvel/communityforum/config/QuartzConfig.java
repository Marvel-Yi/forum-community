package com.marvel.communityforum.config;

import com.marvel.communityforum.quartz.PostGradingJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

@Configuration
public class QuartzConfig {
    // Post Grading Job
    @Bean
    public JobDetailFactoryBean postGradingJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostGradingJob.class);
        factoryBean.setName("postGradingJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean postGradingTrigger(JobDetail postGradingJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postGradingJobDetail);
        factoryBean.setName("postGradingTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        factoryBean.setRepeatInterval(1000 * 60 * 60); // 1 hour interval
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }
}
