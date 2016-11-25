package com.shufflelunch.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.shufflelunch.model.Group;
import com.shufflelunch.model.User;

import com.linecorp.bot.client.LineMessagingService;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.ConfirmTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageService {
    @Autowired
    private TranslationService translationService;

    @Autowired
    private LineMessagingService lineMessagingService;

    public TextMessage getWelcomeMessage(User user) {
        String translatedString = translationService.getTranslation("message.welcome",
                                                                    Arrays.asList(user.getName()),
                                                                    user.getLanguage());
        return new TextMessage(translatedString);
    }

    public void pushMessage(String mid, Message message) {
        PushMessage pushMessage = new PushMessage(mid, message);
        try {
            lineMessagingService
                    .pushMessage(pushMessage)
                    .execute();
        } catch (IOException e) {
            log.error("Failed to push message.", e);
        }
    }

    public Message getJoinRequest(String language) {
        ConfirmTemplate confirmTemplate = new ConfirmTemplate(
                translationService.getTranslation("join.opt_in", language),
                new PostbackAction("Yes", "join_yes"),
                new PostbackAction("No", "join_no")
        );
        return new TemplateMessage("[Join message] Please check on mobile.", confirmTemplate);
    }

    public Message getFixedGroupRequest(User requestingUser, Group group) {
        String imageUrl = getResourceName(group);
        String language = requestingUser.getLanguage();

        List<Action> actionList = group
                .getUserList()
                .stream()
                .filter(user -> !user.getMid().equals(requestingUser.getMid()))
                .map(user -> {
                    String data = translationService.getTranslation("message.group.member",
                                                                     Arrays.asList(user.getName()),
                                                                     language);
                    return new PostbackAction(user.getName(), data);
                })
                .collect(Collectors.toList());
        String title = translationService.getTranslation("message.group.title",
                                                         Arrays.asList(group.getName()), language);
        String content = translationService.getTranslation("message.group.content", language);
        ButtonsTemplate buttonsTemplate = new ButtonsTemplate(
                imageUrl,
                title,
                content,
                actionList);
        return new TemplateMessage("[Group message] Please check on mobile.", buttonsTemplate);
    }

    private String createUri(String path) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                                          .path(path).build()
                                          .toUriString();
    }

    private String getResourceName(Group group) {
        String ret = "https://httpsimage.com/img/cony.jpg";

        String groupName = group.getName();
        switch (groupName) {
            case "Brown":
                ret = "https://httpsimage.com/img/Brown.png";
                break;
            case "James":
                ret = "https://httpsimage.com/img/James.png";
                break;
            case "Jessica":
                ret = "https://httpsimage.com/img/Jessica.png";
                break;
            case "Sally":
                ret = "https://httpsimage.com/img/Sally.png";
                break;
            case "Boss":
                ret = "https://httpsimage.com/img/BossLine.png";
                break;
            case "Leonard":
                ret = "https://httpsimage.com/img/Leonard.png";
                break;
            case "Cony":
            default:
        }

        return ret;

    }
}
