package com.shufflelunch.service;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.shufflelunch.model.User;

@Service
//TODO real implementation
public class LunchService {

    LoadingCache<String, Boolean> cacheSuscribeLunch = CacheBuilder.newBuilder()
                                                                   .maximumSize(100)
                                                                   .expireAfterWrite(4, TimeUnit.HOURS)
                                                                   .build(new CacheLoader<String, Boolean>() {
                                                                       @Override
                                                                       public Boolean load(String s) {
                                                                           return Boolean.FALSE;
                                                                       }
                                                                   });

    public boolean hasSuscribedToLunch(User user) {
        return cacheSuscribeLunch.getUnchecked(user.getMid());
    }

    public void subscribeToLunch(User user) {
        cacheSuscribeLunch.put(user.getMid(), Boolean.TRUE);
    }

    public void unSubscribeToLunch(User user) {
        cacheSuscribeLunch.put(user.getMid(), Boolean.FALSE);
    }

}
