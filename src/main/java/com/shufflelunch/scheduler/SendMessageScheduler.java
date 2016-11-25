package com.shufflelunch.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.shufflelunch.service.MessagePushService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author seisuke
 */
@Component
@Slf4j
public class SendMessageScheduler {

    @Autowired
    MessagePushService messagePushService;

    //I want scheduled without public holiday
    //@Scheduled(cron = "0 0 12 * * 1-5", zone = "Asia/Tokyo")
    @Scheduled(cron = "0 0 12 * * *", zone = "Asia/Tokyo")
    public void pushJoinMessage() {
        log.info("start pushJoinMessage");
        messagePushService.pushJoinMessageToAllUsers();
        log.info("end pushJoinMessage ");
    }

    //@Scheduled(cron = "0 50 12 * * 1-5", zone = "Asia/Tokyo")
    @Scheduled(cron = "0 50 12 * * *", zone = "Asia/Tokyo")
    public void pushFixedGroupMessage() {
        log.info("start pushFixedGroupMessage");
        messagePushService.pushFixedGroupToAllUsers();
        log.info("end pushFixedGroupMessage");
    }

}
