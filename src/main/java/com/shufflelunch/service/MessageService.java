package com.shufflelunch.service;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shufflelunch.model.User;

import com.linecorp.bot.model.message.TextMessage;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageService {
    @Autowired
    private TranslationService translationService;

    public TextMessage getWelcomeMessage(User user) {
        String translatedString = translationService.getTranslation("message.welcome", Arrays.asList(user.getName()), user.getLanguage());
        return new TextMessage(translatedString);
    }
}
