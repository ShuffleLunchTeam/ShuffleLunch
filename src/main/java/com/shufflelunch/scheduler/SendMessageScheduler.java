package com.shufflelunch.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.shufflelunch.handler.GroupHandler;
import com.shufflelunch.service.GroupService;
import com.shufflelunch.service.MessagePushService;
import com.shufflelunch.service.ParticipantService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author seisuke
 */
@Component
@Slf4j
public class SendMessageScheduler {

    @Autowired
    MessagePushService messagePushService;

    @Autowired
    ParticipantService participantService;

    @Autowired
    GroupService groupService;

    @Autowired
    GroupHandler groupHandler; // TODO delete

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Tokyo")
    public void resetGroupsAndParticipants() {
        log.info("start deleteAllParticipant");
        participantService.deleteAllParticipant();
        log.info("end deleteAllParticipant");

        log.info("start delete groups");
        groupService.deleteAllGroup();
        log.info("end delete groups");
    }

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
    public void shuffleGroupsAndPushNotifications() {
        log.info("start handleShuffleGroup");
        groupHandler.handleShuffleGroup();
        log.info("end handleShuffleGroup");

        log.info("start pushFixedGroupMessage");
        messagePushService.pushFixedGroupToAllUsers();
        log.info("end pushFixedGroupMessage");
    }

}
