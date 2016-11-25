package com.shufflelunch.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;

@Component
public class HelpHandler {

    public List<Message> handleHelp() {
        List<Message> ret = new ArrayList<>();
        ret.add(new TextMessage("Available commands are :"));
        infos().stream().forEach(s -> ret.add(new TextMessage(s)));
        return ret;
    }

    public List<String> infos() {
        return ImmutableList.of(" - help : displays this help message",
                                " - join : allow you to register for next lunch",
                                " - leave : remove your registration for the next lunch",
                                " - group : lets you know your group and meeting point for lunch");
    }
}
