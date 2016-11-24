package com.shufflelunch.service;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import com.linecorp.bot.model.message.TextMessage;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageService {

    public TextMessage getWelcomeMessage(String userName) {
        MustacheFactory mf = new DefaultMustacheFactory();
        Map<String, Object> params = new HashMap<>();
        params.put("userName", userName);
        Mustache mustache = mf.compile("message/welcomeMessage.mustache");
        StringWriter stringWriter = new StringWriter();
        mustache.execute(stringWriter, params);

        return new TextMessage(stringWriter.toString());
    }
}
