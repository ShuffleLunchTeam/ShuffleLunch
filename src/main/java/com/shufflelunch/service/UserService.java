package com.shufflelunch.service;

import com.shufflelunch.model.User;

import com.linecorp.bot.model.event.source.Source;

public class UserService {
    public User getUser(Source src) {
        //TODO real implementation
        return new User(1234L, "Brown");
    }
}
