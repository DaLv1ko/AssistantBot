package com.dalv1k.assistantbot.bot;

import com.dalv1k.assistantbot.handler.UpdateHandler;
import com.dalv1k.assistantbot.service.BinanceService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static com.dalv1k.assistantbot.util.Emoji.SAD;
import static com.dalv1k.assistantbot.util.Emoji.WINK;

@Service
@Slf4j
public class Bot {

    @PreDestroy
    private void preDestroy() {
        sendMessage("I'm off " + SAD + " See you later...");
    }

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.chatId}")
    private long chatId;

    @Value("${heroku.link}")
    private String herokuLink;

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
        sendMessage("I'm back buddy " + WINK);
    }


    public static SendResponse sendMessage(SendMessage baseRequest) {
        return bot.execute(baseRequest);
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

    @Scheduled(fixedRate = 60 * 1000)
    private void binanceUpdate() {
        binanceService.checkCurrencies();
    }

    @Scheduled(fixedRate = 60 * 1000)
    private void herokuLive() {
        Unirest.get(herokuLink).asJson();
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
