package com.shufflelunch.handler;

import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import org.springframework.stereotype.Component;

@Component
public class LanguageHandler {

    public Message handleLanguage() {
        ConfirmTemplate confirmTemplate = new ConfirmTemplate(
                "Language",
                new PostbackAction("日本語", "lang_ja"),
                new PostbackAction("English", "lang_en")
        );
        return new TemplateMessage("Language", confirmTemplate);
    }
}
