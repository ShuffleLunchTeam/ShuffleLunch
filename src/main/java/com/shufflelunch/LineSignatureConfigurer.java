package com.shufflelunch;

import com.linecorp.bot.client.LineSignatureValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class LineSignatureConfigurer {
    @Bean
    @Primary
    public LineSignatureValidator fakeLineSignatureValidator() {
        return new FakeLineSignatureValidator();
    }
}
