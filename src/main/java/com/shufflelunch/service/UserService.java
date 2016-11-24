package com.shufflelunch.service;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.shufflelunch.model.User;

import com.linecorp.bot.model.event.source.Source;

@Service
//TODO real implementation
public class UserService {

    private final Cache<String, User> cache = CacheBuilder.newBuilder()
                                                          .maximumSize(100) // Taille Max
                                                          .build();

    public User getUser(Source src) {
        User ret = null;
        try {
            ret = cache.get(src.getUserId(), new Callable<User>() {
                public User call() {
                    return new User("brown", src.getUserId());
                }
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return ret;
    }

}
