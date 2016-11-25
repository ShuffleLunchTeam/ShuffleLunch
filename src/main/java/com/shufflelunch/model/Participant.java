package com.shufflelunch.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class Participant {

    @NonNull
    User user;

    Language language = Language.JAPANESE;
}
