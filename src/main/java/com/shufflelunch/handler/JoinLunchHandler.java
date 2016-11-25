package com.shufflelunch.handler;

import java.io.IOException;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.shufflelunch.model.Participant;
import com.shufflelunch.service.ParticipantService;
import com.shufflelunch.service.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.shufflelunch.model.User;
import com.shufflelunch.service.UserService;

import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ConfirmTemplate;

@Component
public class JoinLunchHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private TranslationService translationService;

    public Message handleJoinConfirmation(Event event)
            throws IOException {
        String userId = event.getSource().getUserId();
        Optional<User> maybeUser = userService.getUser(userId);

        if (!maybeUser.isPresent()) {
            // TODO add to users DB
            return new TextMessage("Sorry, you are not a member of ShuffleLunch");
        }

        User user = maybeUser.get();

        if (participantService.getParticipant(user).isPresent()) {
            return new TextMessage(translationService.getTranslation("join.already", user.getLanguage()));
        }

        participantService.addParticipant(new Participant(user));
        String message = translationService.getTranslation("join.added", ImmutableList.of(user.getName()), user.getLanguage());
        return new TextMessage(message);
    }

    public Message handleNotJoinConfirmation(Event event)
            throws IOException {
        String userId = event.getSource().getUserId();
        Optional<User> maybeUser = userService.getUser(userId);

        if (!maybeUser.isPresent()) {
            // TODO add to users DB
            return new TextMessage("Sorry, you are not a member of ShuffleLunch");
        }

        User user = maybeUser.get();
        Optional<Participant> participant = participantService.getParticipant(user);

        if (!participant.isPresent()) {
            return new TextMessage(translationService.getTranslation("join.not", user.getLanguage()));
        }

        participantService.deleteParticipant(participant.get());
        String message = translationService.getTranslation("join.removed", ImmutableList.of(user.getName()), user.getLanguage());
        return new TextMessage(message);
    }

    public Message handleJoinRequest(Event event) {
        String userId = event.getSource().getUserId();
        Optional<User> maybeUser = userService.getUser(userId);
        if (!maybeUser.isPresent()) {
            // TODO add to users DB
            return new TextMessage("Sorry, you are not a member of ShuffleLunch");
        }

        User user = maybeUser.get();
        ConfirmTemplate confirmTemplate = new ConfirmTemplate(
                translationService.getTranslation("join.opt_in", user.getLanguage()),
                new PostbackAction("Yes", "join_yes"),
                new PostbackAction("No", "join_no")
        );
        return new TemplateMessage(translationService.getTranslation("join.opt_in", user.getLanguage()), confirmTemplate);
    }

    public Message handleUnSubscribe(Event event) throws IOException {
        //get user
        return handleNotJoinConfirmation(event);
    }
}
