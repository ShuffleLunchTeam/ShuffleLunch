package com.shufflelunch.controller;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.shufflelunch.model.User;
import com.shufflelunch.service.MessageService;
import com.shufflelunch.service.UserService;

import com.linecorp.bot.client.LineMessagingService;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.LeaveEvent;
import com.linecorp.bot.model.event.UnfollowEvent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Response;

@Slf4j
@LineMessageHandler
public class ShuffleLunchController {

    @Autowired
    private LineMessagingService lineMessagingService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @EventMapping
    public void handleUnfollowEvent(UnfollowEvent event) {
        log.info("Unfollowed this bot: {}", event);
    }

    @EventMapping
    public void handleFollowEvent(FollowEvent event) throws IOException {
        String replyToken = event.getReplyToken();

        String userId = event.getSource().getUserId();
        Optional<User> userMaybe = userService.getUser(userId);
        if (userMaybe.isPresent()) {
            // TODO: Welcome Back Message
            TextMessage message = messageService.getWelcomeMessage(userMaybe.get().getName());
            reply(replyToken, message);
        } else {
            Response<UserProfileResponse> response = lineMessagingService
                    .getProfile(userId)
                    .execute();

            if (response.isSuccessful()) {
                // Add new User to DB
                UserProfileResponse profile = response.body();
                User user = new User(userId, profile.getDisplayName());
                userService.addUser(user);

                TextMessage message = messageService.getWelcomeMessage(user.getName());
                reply(replyToken, message);
            } else {
                log.error(response.errorBody().string());
            }
        }
    }

    @EventMapping
    public void handleJoinEvent(JoinEvent event) {
        String replyToken = event.getReplyToken();
        reply(replyToken, new TextMessage("Joined " + event.getSource()));
    }

    @EventMapping
    public void handleLeaveEvent(LeaveEvent event) {
        String userId = event.getSource().getUserId();
        log.info("User:{} just left", userId);
    }

    private void reply(@NonNull String replyToken, @NonNull Message message) {
        reply(replyToken, Collections.singletonList(message));
    }

    private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        try {
            Response<BotApiResponse> apiResponse = lineMessagingService
                    .replyMessage(new ReplyMessage(replyToken, messages))
                    .execute();
            log.info("Sent messages: {}", apiResponse);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
