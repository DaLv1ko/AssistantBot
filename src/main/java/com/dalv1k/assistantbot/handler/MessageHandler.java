package com.dalv1k.assistantbot.handler;

import com.dalv1k.assistantbot.bot.Bot;
import com.dalv1k.assistantbot.service.BinanceService;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Service;

import static com.dalv1k.assistantbot.util.Emoji.GEAR_EMOJI;

@Service
public class MessageHandler {

    private final BinanceService binanceService;

    private Message message;

    public MessageHandler(BinanceService binanceService) {
        this.binanceService = binanceService;
    }

    public void handle(Update update) {
        message = update.message();

        if (message.text() != null) {
            switch (message.text()) {
                case "/menu": {
                    Bot.deleteMessage(message.messageId());
                    showMenu();
                    break;
                }
                case "/deletetrackmessages": {
                    Bot.deleteMessage(message.messageId());
                    binanceService.deleteTrackMessages();
                }
            }
        }
    }

    private void showMenu() {
        InlineKeyboardButton monobankButton = new InlineKeyboardButton("Monobank").callbackData("monobank");
        InlineKeyboardButton binanceButton = new InlineKeyboardButton("Binance").callbackData("binance");
        Keyboard keyboard = new InlineKeyboardMarkup(monobankButton, binanceButton);
        SendMessage sendMessage = new SendMessage(message.chat().id(), "Use menu ".concat(GEAR_EMOJI));
        sendMessage.replyMarkup(keyboard);
        Bot.sendMessage(sendMessage);
    }
}
