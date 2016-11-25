package com.shufflelunch.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Data
@RequiredArgsConstructor
public class Group {

    @NonNull
    String name;

    Language language = Language.JAPANESE;

    @Setter(AccessLevel.PRIVATE)
    List<User> userList = new ArrayList<>();

    public void addUser(User user) {
        userList.add(user);
    }
}
