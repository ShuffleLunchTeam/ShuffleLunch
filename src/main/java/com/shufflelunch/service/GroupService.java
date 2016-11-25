package com.shufflelunch.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shufflelunch.dao.FireBaseDao;
import com.shufflelunch.model.Group;
import com.shufflelunch.model.User;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GroupService {

    @Autowired
    FireBaseDao fireBaseDao;

    private final Queue<String> groupNames = new ConcurrentLinkedQueue<>(
            Arrays.asList("Cony", "Brown", "Sally", "Jessica", "James", "Choco"));

    private final Queue<String> usedGroupNames = new ConcurrentLinkedQueue<String>();

    public Optional<Group> getGroupForUser(User user) {
        Optional<Group> ret = Optional.empty();

        //TODO most inefficient code ever
        if (getAllGroupList().isPresent()) {
            groups:
            for (Group group : getAllGroupList().get()) {
                for (User u : group.getUserList()) {
                    if (u.getMid().equals(user.getMid())) {
                        ret = Optional.of(group);
                        break groups;
                    }
                }
            }
        }

        return ret;

    }

    public void clearGroupNames() {
        groupNames.addAll(usedGroupNames);
        usedGroupNames.clear();
    }

    private String popGroupName() {
        String name = groupNames.poll();
        usedGroupNames.add(name);
        return name;
    }

    private void pushGroupName(String name) {
        if (usedGroupNames.remove(name)) {
            groupNames.add(name);
        }
    }

    public Group createGroup(List<User> users) {
        Group ret = new Group(popGroupName());
        ret.addUserList(users);

        return ret;
    }

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
        pushGroupName(group.getName());
        return deleteGroup(group.getName());
    }

    public boolean deleteGroup(String name) {
        return fireBaseDao.delete("groups/" + name);
    }

    public boolean deleteAllGroup() {
        clearGroupNames();
        return fireBaseDao.delete("groups/");
    }

    public List<Integer> calcMemberNumber(int memberSize, int defaultSize) {
        int resultNum = memberSize;

        if (resultNum <= defaultSize + 1) {
            return Arrays.asList(resultNum);
        }

        List<Integer> memberNum = new ArrayList<>();

        while (resultNum > 0) {
            if (resultNum >= defaultSize) {
                memberNum.add(defaultSize);
                resultNum -= defaultSize;
            } else {
                memberNum.add(resultNum);
                resultNum = 0;
            }
        }

        while (!validGrouping(memberNum)) {
            memberNum = rebalance(memberNum);
        }

        return memberNum;
    }

    List<Integer> rebalance(List<Integer> memberNum) {
        int maxIndex = memberNum.indexOf(Collections.max(memberNum));
        int minIndex = memberNum.indexOf(Collections.min(memberNum));
        memberNum.set(maxIndex, memberNum.get(maxIndex) - 1);
        memberNum.set(minIndex, memberNum.get(minIndex) + 1);
        return memberNum;
    }

    boolean validGrouping(List<Integer> memberList) {
        int max = Collections.max(memberList);
        int min = Collections.min(memberList);
        return max - min < 2;
    }

    public List<List<User>> grouping(List<User> userList, int defaultSize, boolean shuffle) {
        List<List<User>> result = new ArrayList<>();
        List<Integer> memberNumList = calcMemberNumber(userList.size(), defaultSize);
        if (shuffle) {
            Collections.shuffle(userList);
        }

        int startIndex = 0;
        for (Integer num : memberNumList) {
            List<User> subList = userList.subList(startIndex, startIndex + num);
            result.add(subList);
            startIndex += num;
        }

        return result;
    }
}
