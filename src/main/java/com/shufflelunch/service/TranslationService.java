package com.shufflelunch.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Locale;
import java.util.Properties;

import org.springframework.stereotype.Service;

@Service
public class TranslationService {

    public String getTranslation(String key, Locale local) {

        InputStream utf8in;
        if (local == Locale.ENGLISH) {
            utf8in = getClass().getClassLoader().getResourceAsStream("translation_" + local.getLanguage() + ".properties");
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
