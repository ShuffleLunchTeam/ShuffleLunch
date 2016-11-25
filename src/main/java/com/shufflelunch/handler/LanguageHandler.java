package com.shufflelunch.handler;

import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.shufflelunch.model.User;
import com.shufflelunch.service.TranslationService;
import com.shufflelunch.service.UserService;

import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ConfirmTemplate;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LanguageHandler {

    @Autowired
    TranslationService translationService;

    @Autowired
    UserService userService;

    public Message handleLanguage() {
        ConfirmTemplate confirmTemplate = new ConfirmTemplate(
                "Language",
                new PostbackAction("日本語", "lang_ja"),
                new PostbackAction("English", "lang_en")
        );
        return new TemplateMessage("Language", confirmTemplate);
    }

    public Message handleLanguageChange(PostbackEvent event) {
        String message = "";

        userService.getUser(event.getSource().getUserId());
        String userId = event.getSource().getUserId();
        if (StringUtils.isEmpty(userId)) {
            log.error("handleLanguageChange: userId is empty: {}", event);
            return new TextMessage(message);
        }
        Optional<User> maybeUser = userService.getUser(userId);

        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            switch (event.getPostbackContent().getData()) {
                case "lang_ja": {
                    user.setLanguage(Locale.JAPANESE.getLanguage());
                    message = translationService.getTranslation("message.changedLanguage", Locale.JAPANESE.getLanguage());
                    break;
                }
                case "lang_en": {
                    user.setLanguage(Locale.ENGLISH.getLanguage());
                    message = translationService.getTranslation("message.changedLanguage", Locale.ENGLISH.getLanguage());
                    break;
                }

                default:
                    log.error("handleLanguageChange: Language not found:{}", event);
                    message = "Language not found";
            }
            userService.addUser(user);
        } else {
            log.error("handleLanguageChange: User not registered: {}", event);
            return new TextMessage(message);
        }

        return new TextMessage(message);
    }
}
