package com.shufflelunch.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.shufflelunch.model.User;
import com.shufflelunch.service.GroupService;
import com.shufflelunch.service.MessageService;
import com.shufflelunch.service.UserService;

@Component
public class MessagePushHandler {

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
                            .getFixedGroupRequest(groupService.getMyGroup(user).get(), user.getLanguage())));
        }
    }
}
