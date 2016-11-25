package com.shufflelunch.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TranslationService {

    public String getTranslation(String key, List<String> params, String language) {
        String translation = getTranslation(key, language);
        for (int i = 0; i < params.size(); i++) {
            translation = translation.replace("%@", params.get(i));
        }
        return translation;
    }

    public String getTranslation(String key, String language) {

        log.info("getTranslation:{}", language);
        InputStream utf8in;
        if (language == Locale.ENGLISH.getLanguage()) {
            utf8in = getClass().getClassLoader().getResourceAsStream("translation_" + language + ".properties");
        } else {
            utf8in = getClass().getClassLoader().getResourceAsStream("translation.properties");
        }

        String result = "";
        try {
            Reader reader = new InputStreamReader(utf8in, "UTF-8");
            Properties props = new Properties();
            props.load(reader);
            result = props.getProperty(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
