package com.shufflelunch.service;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.linecorp.bot.client.LineMessagingService;
import com.linecorp.bot.model.profile.UserProfileResponse;

import lombok.extern.slf4j.Slf4j;
import retrofit2.Response;

@Service
@Slf4j
public class ProfileService {

    @Autowired
    LineMessagingService lineMessagingService;

    public Optional<UserProfileResponse> getProfile(String userId) throws IOException {
        Response<UserProfileResponse> response = lineMessagingService
                .getProfile(userId)
                .execute();

        if (response.isSuccessful()) {
            return Optional.of(response.body());
        } else {
            log.error(response.errorBody().string());
            return Optional.empty();
        }

    }

}
