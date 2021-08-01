package com.dalv1k.assistantbot.handler;

import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Service;

@Service
public class UpdateHandler {

    private final CallBackHandler callBackHandler;
    private final MessageHandler messageHandler;

    public UpdateHandler(CallBackHandler callBackHandler, MessageHandler messageHandler) {
        this.callBackHandler = callBackHandler;
        this.messageHandler = messageHandler;
    }

    public void handleUpdate(Update update) {

        if (update.message() != null) {
            messageHandler.handle(update);
        }
        if (update.callbackQuery() != null) {
            callBackHandler.handle(update);
        }

    }

}
