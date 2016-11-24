package com.shufflelunch.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shufflelunch.dao.FireBaseDao;
import com.shufflelunch.model.User;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {

    @Autowired
    FireBaseDao fireBaseDao;

    public Optional<User> getUser(String mid) {
        return fireBaseDao.read("users/" + mid, User.class);
    }

    public Optional<List<User>> getAllUserList() {
        return fireBaseDao.readList("users", User.class);
    }

    public void addUser(User user) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> users = mapper.convertValue(user, new TypeReference<Map<String, Object>>() {});
        fireBaseDao.update("users/" + user.getMid(), users);
    }

}
