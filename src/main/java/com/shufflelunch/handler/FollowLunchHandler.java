package com.shufflelunch.handler;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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

    public Optional<Message> handleFollow(Event event) throws IOException {
        String userId = event.getSource().getUserId();
        if (StringUtils.isEmpty(userId)) {
            log.error("HandleFollow: userId is empty: {}", event);
            return Optional.empty();
        }

        Optional<User> userMaybe = userService.getUser(userId);
        if (userMaybe.isPresent()) {
            TextMessage message = messageService.getWelcomeMessage(userMaybe.get());
            return Optional.of(message);
        } else {
            Optional<UserProfileResponse> userProfileMaybe = profileService.getProfile(userId);
            if (userProfileMaybe.isPresent()) {
                User user = new User(userId, userProfileMaybe.get().getDisplayName(), Locale.JAPANESE.getLanguage());
                userService.addUser(user);

                TextMessage message = messageService.getWelcomeMessage(user);
                return Optional.of(message);
            } else {
                TextMessage message = new TextMessage("Unknown user profile");
                return Optional.of(message);
            }
        }
    }
}
