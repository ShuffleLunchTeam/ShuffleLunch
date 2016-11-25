package com.shufflelunch.handler;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.shufflelunch.model.User;
import com.shufflelunch.service.MessageService;
import com.shufflelunch.service.ProfileService;
import com.shufflelunch.service.UserService;

import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FollowLunchHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private MessageService messageService;

    public Message handleFollow(Event event)
            throws IOException {

        String userId = event.getSource().getUserId();
        Optional<User> userMaybe = userService.getUser(userId);
        if (userMaybe.isPresent()) {
            // TODO: Welcome Back Message
            return messageService.getWelcomeMessage(userMaybe.get().getName());
        } else {

            Optional<UserProfileResponse> response = profileService.getProfile(userId);

            if (response.isPresent()) {
                // Add new User to DB
                UserProfileResponse profile = response.get();
                User user = new User(userId, profile.getDisplayName());
                userService.addUser(user);

                return messageService.getWelcomeMessage(user.getName());
            } else {
                return new TextMessage("Unknown user profile");
            }
        }
    }
}
