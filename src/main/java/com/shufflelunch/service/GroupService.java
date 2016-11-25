package com.shufflelunch.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shufflelunch.dao.FireBaseDao;
import com.shufflelunch.model.Group;
import com.shufflelunch.model.User;

@Service
public class GroupService {

    @Autowired
    FireBaseDao fireBaseDao;

    public void addGroup(Group group) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> groups = mapper.convertValue(group, new TypeReference<Map<String, Object>>() {});
        fireBaseDao.update("groups/" + group.getName(), groups);
    }

    public void addUser(Group group, User user) {
//        fireBaseDao.update("groups/" + group.getName() + "/userList", user);
    }

}
