package com.workingbit.board.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class MessageBundle {

    private ResourceBundle messages;

    public MessageBundle(@Nullable String languageTag) {
        Locale locale = languageTag != null ? new Locale(languageTag) : Locale.ENGLISH;
        this.messages = ResourceBundle.getBundle("localization/messages", locale);
    }

    @NotNull
    public String get(@NotNull String message) {
        return messages.getString(message);
    }

    public final String get(@NotNull final String key, final Object... args) {
        return MessageFormat.format(get(key), args);
    }

}