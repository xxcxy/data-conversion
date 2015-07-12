package com.customtime.data.conversion.domain.util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzManager {

	private static SchedulerFactory gSchedulerFactory = new StdSchedulerFactory();  
    private static String JOB_GROUP_NAME = "TERMINATOR_JOBGROUP_NAME";  
    private static String TRIGGER_GROUP_NAME = "TERMINATOR_TRIGGERGROUP_NAME"; 
    
    public static void addJob(String jobName, Class<?> jobClass, String time,Map<String,Object> param) {  
        try {  
            Scheduler sched = gSchedulerFactory.getScheduler();
            if (!sched.isShutdown()&&!sched.isStarted()){  
                sched.start();  
            }
            if(sched.getJobDetail(jobName, JOB_GROUP_NAME)!=null){
            	jobName=jobName+System.currentTimeMillis(); 
            }
            JobDetail jobDetail = new JobDetail(jobName,JOB_GROUP_NAME,jobClass); 
            jobDetail.getJobDataMap().putAll(param);
            CronTrigger trigger = new CronTrigger(jobName, TRIGGER_GROUP_NAME); 
            trigger.setCronExpression(time);
            sched.scheduleJob(jobDetail, trigger);  
             
        } catch (Exception e) {  
            e.printStackTrace();  
            throw new RuntimeException(e);  
        }  
    } 
    
    @SuppressWarnings("unchecked")
	public static void modifyJobTime(String jobName, String time) {  
        try {  
            Scheduler sched = gSchedulerFactory.getScheduler();  
            CronTrigger trigger = (CronTrigger) sched.getTrigger(jobName, TRIGGER_GROUP_NAME);  
            if(trigger == null) {  
                return;  
            }  
            String oldTime = trigger.getCronExpression();  
            if (!oldTime.equalsIgnoreCase(time)) {  
                JobDetail jobDetail = sched.getJobDetail(jobName, JOB_GROUP_NAME);  
                Class<?> objJobClass = jobDetail.getJobClass();  
                removeJob(jobName);  
                addJob(jobName, objJobClass, time,new HashMap<String,Object>(jobDetail.getJobDataMap()));  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
            throw new RuntimeException(e);  
        }  
    }  
    public static String getCronTime(int time,TimeUnit tu){
    	long now = System.currentTimeMillis();
    	long then = now + tu.toMillis(time);
    	Calendar thenTime = Calendar.getInstance();
    	thenTime.setTimeInMillis(then);
    	StringBuffer sb = new StringBuffer(thenTime.get(Calendar.SECOND)+"").append(",").append(thenTime.get(Calendar.MINUTE)).append(",").append(thenTime.get(Calendar.HOUR_OF_DAY)).append(",").append(thenTime.get(Calendar.DATE)).append(",").append(thenTime.get(Calendar.MONTH)+1).append(",?,").append(thenTime.get(Calendar.YEAR));
    	return sb.toString();
    }
    
    public static void removeJob(String jobName) {  
        try {  
            Scheduler sched = gSchedulerFactory.getScheduler();  
            sched.pauseTrigger(jobName, TRIGGER_GROUP_NAME);// 停止触发器  
            sched.unscheduleJob(jobName, TRIGGER_GROUP_NAME);// 移除触发器  
            sched.deleteJob(jobName, JOB_GROUP_NAME);// 删除任务  
        } catch (Exception e) {  
            e.printStackTrace();  
            throw new RuntimeException(e);  
        }  
    }
}
