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

    public List<Message> handleGroupRequest(Event event) throws IOException {

        Optional<User> maybeUser = userService.getUser(event.getSource().getUserId());
        if (!maybeUser.isPresent()) {
            return ImmutableList.of(new TextMessage("Unknown user"));
        }

        User user = maybeUser.get();
        Optional<Participant> participant = participantService.getParticipant(user);
        if (!participant.isPresent()) {
            return ImmutableList.of(
                    new TextMessage(t.getTranslation("group.not.registered", user.getLanguage())));
        }

        Optional<Group> maybeGroup = groupService.getGroupForUser(user);
        if (!maybeGroup.isPresent()) {
            return ImmutableList.of(
                    new TextMessage(t.getTranslation("group.not.shuffled", user.getLanguage())));
        }

        Group group = maybeGroup.get();
        return ImmutableList.of(messageService.getFixedGroupRequest(user, group));
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
