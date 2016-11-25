package com.shufflelunch.handler;

import java.io.IOException;
import java.util.Optional;

import com.shufflelunch.model.Participant;
import com.shufflelunch.service.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.shufflelunch.model.User;
import com.shufflelunch.service.UserService;

import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.message.TextMessageContent;
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
            return new TextMessage("You already joined today's lunch");
        }

        participantService.addParticipant(new Participant(user));
        return new TextMessage("Added " + user.getName() + " to the participant list.");
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
            return new TextMessage("You didn't sign up to lunch today.");
        }

        participantService.deleteParticipant(participant.get());
        return new TextMessage("Removed " + user.getName() + " from the participant list.");
    }

    public Message handleJoinRequest(Event event, TextMessageContent content) {
        ConfirmTemplate confirmTemplate = new ConfirmTemplate(
                "Join Shuffle Lunch?",
                new PostbackAction("Yes", "join_yes"),
                new PostbackAction("No", "join_no")
        );
        return new TemplateMessage("Join Shuffle Lunch?", confirmTemplate);
    }

    public Message handleUnSubscribe(Event event) throws IOException {
        //get user
        return handleNotJoinConfirmation(event);
    }
}
