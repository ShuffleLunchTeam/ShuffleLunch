package com.shufflelunch.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shufflelunch.dao.FireBaseDao;
import com.shufflelunch.model.Group;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GroupService {

    @Autowired
    FireBaseDao fireBaseDao;

    public void addGroup(Group group) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> groups = mapper.convertValue(group, new TypeReference<Map<String, Object>>() {});
        fireBaseDao.update("groups/" + group.getName(), groups);
    }

    public Optional<Group> getGroup(String name) {
        return fireBaseDao.read("groups/" + name, Group.class);
    }

    public Optional<List<Group>> getAllGroupList() {
        return fireBaseDao.readList("groups", Group.class);
    }

    public boolean deleteGroup(Group group) {
        return deleteGroup(group.getName());
    }

    public boolean deleteGroup(String name) {
        return fireBaseDao.delete("groups/" + name);
    }

    public boolean deleteAllGroup() {
        return fireBaseDao.delete("groups/");
    }
}
