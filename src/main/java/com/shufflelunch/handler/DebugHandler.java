package com.shufflelunch.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.shufflelunch.model.Group;
import com.shufflelunch.model.Participant;
import com.shufflelunch.model.User;
import com.shufflelunch.service.GroupService;
import com.shufflelunch.service.ParticipantService;
import com.shufflelunch.service.UserService;

import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;

@Component
public class DebugHandler {

    @Autowired
    UserService userService;

    @Autowired
    ParticipantService participantService;

    @Autowired
    GroupService groupService;

    public List<Message> handleDebug() {
        List<Message> ret = new ArrayList<>();
        ret.add(userList());
        ret.add(participantList());
        ret.add(groupList());

        return ret;
    }

    private Message userList() {
        StringBuilder sb = new StringBuilder("Users list :  ");
        Optional<List<User>> users = userService.getAllUserList();
        users.ifPresent(list -> {
            list.stream().forEach(user -> sb.append(" ** ").append(user.getName()));
        });

        return new TextMessage(sb.toString());

    }

    private Message participantList() {
        StringBuilder sb = new StringBuilder("Participant list :  ");
        Optional<List<Participant>> users = participantService.getAllParticipantList();
        users.ifPresent(list -> {
            list.stream().forEach(p -> sb.append(" ** ").append(p.getUser().getName()));
        });

        return new TextMessage(sb.toString());

    }

    private Message groupList() {
        StringBuilder sb = new StringBuilder("Group list :  ");
        Optional<List<Group>> group = groupService.getAllGroupList();
        group.ifPresent(list -> {
            list.stream().forEach(g -> sb.append(" ** ").append(g.getName()));
        });

        return new TextMessage(sb.toString());

    }
}
