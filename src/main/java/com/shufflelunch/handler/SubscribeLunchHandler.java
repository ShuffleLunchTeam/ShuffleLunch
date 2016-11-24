package com.shufflelunch.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.shufflelunch.model.User;
import com.shufflelunch.service.LunchService;
import com.shufflelunch.service.UserService;

import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;

@Component
public class SubscribeLunchHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private LunchService lunchService;

    public Message handleSubscribe(Event event, TextMessageContent content) {
        //get user
        User user = userService.getUser(event.getSource());

        if (lunchService.hasSuscribedToLunch(user)) {
            return alreadySuscribed();
        } else {
            lunchService.subscribeToLunch(user);
            return suscribed();
        }
    }

    public Message handleUnSubscribe(Event event, TextMessageContent content) {
        //get user
        User user = userService.getUser(event.getSource());

        if (lunchService.hasSuscribedToLunch(user)) {
            return notSuscribed();
        } else {
            lunchService.unSubscribeToLunch(user);
            return unSuscribed();

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
