package com.shufflelunch.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
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

    //@Scheduled(cron = "0 * * * * *", zone = "Asia/Tokyo")
    public void pushJoinMessage() {
        log.info("start pushJoinMessage");
        messagePushService.pushJoinMessageToAllUsers();
        log.info("end pushJoinMessage ");
    }
}
