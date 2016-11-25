package com.shufflelunch.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
import com.shufflelunch.model.Group;
import com.shufflelunch.model.Participant;
import com.shufflelunch.model.User;
import com.shufflelunch.service.GroupService;
import com.shufflelunch.service.ParticipantService;
import com.shufflelunch.service.TranslationService;
import com.shufflelunch.service.UserService;

import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GroupHandler {

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;

    @Autowired
    TranslationService t;

    @Autowired
    private ParticipantService participantService;

    public List<Message> handleGroupRequest(Event event)
            throws IOException {

        Optional<User> user = userService.getUser(event.getSource().getUserId());
        if (user.isPresent()) {
            Optional<Participant> participant = participantService.getParticipant(user.get());
            if (participant.isPresent()) {
                Optional<Group> group = groupService.getGroupForUser(user.get());
                if (group.isPresent()) {
                    Group g = group.get();
                    List<Message> ret = new ArrayList<>();
                    ret.add(new TextMessage("You are in group : " + g.getName() + "　with the users :"));
                    g.getUserList().forEach(u -> {
                        if (u.getMid() != user.get().getMid()) {
                            ret.add(new TextMessage(" - " + u.getName()));
                        }
                    });
                    return ret;
                } else {
                    return ImmutableList.of(new TextMessage("Group have not been shuffled yet"));
                }
            } else {
                return ImmutableList.of(new TextMessage("Not registered in any group"));
            }
        } else {
            return ImmutableList.of(new TextMessage("Unknown user"));
        }
    }

    public Message handleShuffleGroup() {

        Optional<List<Participant>> participants = participantService.getAllParticipantList();
        if (participants.isPresent()) {
            groupService.deleteAllGroup();
            List<User> joiningUsers = participants.get().stream()
                                                  .map(p -> p.getUser())
                                                  .collect(Collectors.toList());
            List<List<User>> groups = groupService.grouping(joiningUsers, 4, true);
            groups.forEach(users -> groupService.addGroup(groupService.createGroup(users)));
        }

        return new TextMessage("schuffled !");
    }
}
