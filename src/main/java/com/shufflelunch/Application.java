package com.shufflelunch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.linecorp.bot.client.LineSignatureValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static Path downloadedContentDir;

    // TODO: Remove this
    static {
        System.setProperty("line.bot.channelSecret", "SECRET");
        System.setProperty("line.bot.channelToken", "TOKEN");
    }

    public static void main(String[] args) throws IOException {
        downloadedContentDir = Files.createTempDirectory("line-bot");
        SpringApplication.run(Application.class, args);
    }
}
