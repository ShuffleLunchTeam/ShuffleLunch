package com.shufflelunch;

import com.linecorp.bot.client.LineSignatureValidator;
import lombok.NonNull;

public class FakeLineSignatureValidator extends LineSignatureValidator{

    @Override
    public boolean validateSignature(@NonNull byte[] content, @NonNull String headerSignature) {
        return true;
    }

    public FakeLineSignatureValidator() {
        super(new byte[1]);
    }
}
