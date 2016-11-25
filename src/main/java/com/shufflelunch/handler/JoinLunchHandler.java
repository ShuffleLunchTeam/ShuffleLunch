package com.shufflelunch.handler;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.shufflelunch.model.User;
import com.shufflelunch.service.LunchService;
import com.shufflelunch.service.UserService;

import com.linecorp.bot.client.LineMessagingService;
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
    private LunchService lunchService;

    public Message handleJoinConfirmation(Event event, LineMessagingService lineMessagingService)
            throws IOException {
        String userId = event.getSource().getUserId();
        String userName = "you";
        Optional<User> user = userService.getUser(userId);
        if (user.isPresent()) {
            userName = user.get().getName();
        }

        return new TextMessage("Registered " + userName + " for today's lunch.");
    }

    public Message handleJoinRequest(Event event, TextMessageContent content) {
        ConfirmTemplate confirmTemplate = new ConfirmTemplate(
                "Join Shuffle Lunch?",
                new PostbackAction("Yes", "join_yes"),
                new PostbackAction("No", "join_no")
        );
        return new TemplateMessage("Join Shuffle Lunch?", confirmTemplate);
    }

    public Message handleSubscribe(Event event, TextMessageContent content) {
        //get user
        Optional<User> user = userService.getUser(event.getSource().getUserId());
        if (user.isPresent()) {
            if (lunchService.hasSuscribedToLunch(user.get())) {
                return alreadySuscribed();
            } else {
                lunchService.subscribeToLunch(user.get());
                return suscribed();
            }
        } else {
            return new TextMessage("Unknown User");
        }
    }

    public Message handleUnSubscribe(Event event, TextMessageContent content) {
        //get user
        Optional<User> user = userService.getUser(event.getSource().getUserId());
        if (user.isPresent()) {

            if (lunchService.hasSuscribedToLunch(user.get())) {
                return notSuscribed();
            } else {
                lunchService.unSubscribeToLunch(user.get());
                return unSuscribed();

            }
        } else {
            return new TextMessage("Unknown User");
        }
    }

    private Message notSuscribed() {
        return new TextMessage("You did not subscribed for Today");
    }

    private Message alreadySuscribed() {
        return new TextMessage("You already subscribed to lunch for Today");
    }

    private Message suscribed() {
        return new TextMessage("You subscribed for today's lunch");
    }

    private Message unSuscribed() {
        return new TextMessage("You unsubscribed for today's lunch");
    }
}
