package com.dalv1k.assistantbot.handler;

import com.dalv1k.assistantbot.bot.Bot;
import com.dalv1k.assistantbot.bot.BotState;
import com.dalv1k.assistantbot.model.entity.BotData;
import com.dalv1k.assistantbot.model.entity.Currency;
import com.dalv1k.assistantbot.model.entity.DeleteMessage;
import com.dalv1k.assistantbot.model.repository.BotDataRepository;
import com.dalv1k.assistantbot.model.repository.CurrencyRepository;
import com.dalv1k.assistantbot.model.repository.DeleteMessageRepository;
import com.dalv1k.assistantbot.service.BinanceService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BinanceSearchHandler {

    private final CurrencyRepository currencyRepository;
    private final BinanceService binanceService;
    private final BotDataRepository botDataRepository;
    private final DeleteMessageRepository deleteMessageRepository;

    public BinanceSearchHandler(CurrencyRepository currencyRepository, BinanceService binanceService,
                                BotDataRepository botDataRepository, DeleteMessageRepository deleteMessageRepository) {
        this.currencyRepository = currencyRepository;
        this.binanceService = binanceService;
        this.botDataRepository = botDataRepository;
        this.deleteMessageRepository = deleteMessageRepository;
    }

    public void handle(Update update) {
        String currencyName = update.message().text().toUpperCase();
        Currency currency = currencyRepository.findByName(currencyName);
        SendMessage sendMessage;
        InlineKeyboardMarkup inlineKeyboardMarkup;
        if(currency!=null){
            sendMessage = new SendMessage(update.message().chat().id(), "Found:");
            inlineKeyboardMarkup = new InlineKeyboardMarkup(
                    new InlineKeyboardButton(currency.getName().concat(binanceService.isTracked(currency)))
                            .callbackData("binance.track.select.one.".concat(String.valueOf(currency.getId()))));
            inlineKeyboardMarkup.addRow(new InlineKeyboardButton(" <--- Back to menu").callbackData("binance.track"));
        }else {
            sendMessage = new SendMessage(update.message().chat().id(), "Currency not found =(");
            inlineKeyboardMarkup = new InlineKeyboardMarkup(
                    new InlineKeyboardButton("<--- Back to track-list")
                            .callbackData("binance.track"));
        }
        sendMessage.replyMarkup(inlineKeyboardMarkup);
        DeleteMessage deleteMessage = new DeleteMessage(Bot.sendMessage(sendMessage).message().messageId());
        deleteMessageRepository.save(deleteMessage);
        Optional<BotData> botDataOptional = botDataRepository.findById(1L);
        if(botDataOptional.isPresent()){
            BotData botData = botDataOptional.get();
            botData.setBotState(BotState.DEFAULT);
            botDataRepository.save(botData);
        }
    }
}
