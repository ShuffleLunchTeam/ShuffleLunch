package com.shufflelunch.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author seisuke
 */
@Component
@Slf4j
public class SendMessageScheduler {
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Tokyo")
    public void buyHighPercentageReturnNotes() {
        log.info("schedule is running");
    }
}
