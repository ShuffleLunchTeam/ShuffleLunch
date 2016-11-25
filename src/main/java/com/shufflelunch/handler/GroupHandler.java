package com.shufflelunch.handler;

import java.io.IOException;
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
import com.shufflelunch.service.MessageService;
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
    MessageService messageService;

    @Autowired
    TranslationService t;

    @Autowired
    private ParticipantService participantService;

    public List<Message> handleGroupRequest(Event event)
            throws IOException {

        Optional<User> user = userService.getUser(event.getSource().getUserId());
        if (user.isPresent()) {
            final User actualUser = user.get();
            Optional<Participant> participant = participantService.getParticipant(actualUser);
            if (participant.isPresent()) {
                Optional<Group> group = groupService.getGroupForUser(actualUser);
                if (group.isPresent()) {
                    Group g = group.get();

                    return ImmutableList.of(messageService.getFixedGroupRequest(actualUser, g));
//                    List<Message> ret = new ArrayList<>();
//                    ret.add(new TextMessage(t.getTranslation("group.info", ImmutableList.of(g.getName()),
//                                                             actualUser.getLanguage())));
//                    g.getUserList().forEach(u -> {
//                        if (u.getMid() != actualUser.getMid()) {
//                            ret.add(new TextMessage(" - " + u.getName()));
//                        }
//                    });
//                    return ret;
                } else {
                    return ImmutableList.of(
                            new TextMessage(t.getTranslation("group.not.shuffled", actualUser.getLanguage())));
                }
            } else {
                return ImmutableList.of(
                        new TextMessage(t.getTranslation("group.not.registered", actualUser.getLanguage())));
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

        return new TextMessage("shuffled !");
    }
}
