package com.dalv1k.assistantbot;

import com.dalv1k.assistantbot.handler.UpdateHandler;
import com.dalv1k.assistantbot.service.BinanceService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class Bot {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.chatId}")
    private long chatId;

    private static String chatIdStatic;


    private static TelegramBot bot;

    private final UpdateHandler updateHandler;
    private final BinanceService binanceService;

    public Bot(UpdateHandler updateHandler, BinanceService binanceService) {
        this.updateHandler = updateHandler;
        this.binanceService = binanceService;
    }

    @PostConstruct
    public void init() {
        bot = new TelegramBot(botToken);
        bot.setUpdatesListener(updates -> {
            updates.forEach(update -> {
                if (authorize(update)) {
                    updateHandler.handleUpdate(update);
                } else sendMessage(new SendMessage(update.message().chat().id(), "You have no access to this bot"));
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }


    public static void sendMessage(SendMessage baseRequest) {
        bot.execute(baseRequest);
    }

    public static void deleteMessage(long id) {
        DeleteMessage deleteMessage = new DeleteMessage(chatIdStatic, (int) id);
        bot.execute(deleteMessage);
    }

    public static SendResponse sendMessage(String message) {
        SendMessage sendMessage = new SendMessage(chatIdStatic, message);
        return bot.execute(sendMessage);
    }

    public static void editMessageText(EditMessageText editMessageText) {
        bot.execute(editMessageText);
    }

    @Scheduled(fixedRate = 30 * 1000)
    private void binance() {
        binanceService.checkCurrencies();
    }

    private boolean authorize(Update update) {
        if (update.message() != null) {
            return update.message().chat().id() == chatId;
        } else if (update.callbackQuery() != null) {
            return update.callbackQuery().message().chat().id() == chatId;
        } else return false;
    }

    @Value("${telegram.bot.chatId}")
    public void setNameStatic(String name) {
        chatIdStatic = name;
    }

}
