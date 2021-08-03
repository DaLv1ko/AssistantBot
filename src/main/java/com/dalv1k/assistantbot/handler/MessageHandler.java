package com.dalv1k.assistantbot.handler;

import com.dalv1k.assistantbot.bot.Bot;
import com.dalv1k.assistantbot.model.entity.DeleteMessage;
import com.dalv1k.assistantbot.model.repository.DeleteMessageRepository;
import com.dalv1k.assistantbot.service.BinanceService;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dalv1k.assistantbot.util.Emoji.GEAR_EMOJI;

@Service
public class MessageHandler {

    private final BinanceService binanceService;
    private final DeleteMessageRepository deleteMessageRepository;


    private Message message;

    public MessageHandler(BinanceService binanceService, DeleteMessageRepository deleteMessageRepository) {
        this.binanceService = binanceService;
        this.deleteMessageRepository = deleteMessageRepository;
    }

    public void handle(Update update) {
        message = update.message();

        if (message.text() != null) {
            switch (message.text()) {
                case "/menu": {
                    Bot.deleteMessage(message.messageId());
                    deleteDangerMessages();
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
        SendResponse sendResponse = Bot.sendMessage(sendMessage);
        deleteMessageRepository.save(new DeleteMessage(sendResponse.message().messageId()));
    }

    private void deleteDangerMessages(){
        List<DeleteMessage> listMessages = deleteMessageRepository.findAll();
        listMessages.forEach(message->{
            deleteMessageRepository.delete(message);
            Bot.deleteMessage(message.getMessageId());
        });
        CallBackHandler.closeMenu();
    }
}
