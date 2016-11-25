package com.shufflelunch.model;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Participant {

    @NonNull
    User user;

    Language language = Language.JAPANESS;
}
