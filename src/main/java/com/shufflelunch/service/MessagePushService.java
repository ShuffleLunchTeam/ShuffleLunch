package com.shufflelunch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shufflelunch.model.User;

@Service
public class MessagePushService {

    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    @Autowired
    GroupService groupService;

    public void pushJoinMessageToAllUsers() {
        if (userService.getAllUserList().isPresent()) {
            for (User user : userService.getAllUserList().get()) {
                messageService.pushMessage(user.getMid(),
                                           messageService.getJoinRequest(user.getLanguage()));
            }
        }
    }

    public void pushFixedGroupToAllUsers() {
        if (userService.getAllUserList().isPresent()) {
            userService.getAllUserList().get().stream().filter(
                    user -> groupService.getMyGroup(user).isPresent()).forEach(
                    user -> messageService.pushMessage(user.getMid(), messageService
                            .getFixedGroupRequest(user, groupService.getMyGroup(user).get())));
        }
    }
}
