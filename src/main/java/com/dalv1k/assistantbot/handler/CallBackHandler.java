package com.dalv1k.assistantbot.handler;

import com.dalv1k.assistantbot.Bot;
import com.dalv1k.assistantbot.model.entity.Currency;
import com.dalv1k.assistantbot.service.BinanceService;
import com.dalv1k.assistantbot.service.MonobankService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.dalv1k.assistantbot.util.Emoji.CARD_EMOJI;

@Service
public class CallBackHandler {

    private List<Currency> currencies = new ArrayList<>();
    private int currentPage = 0;

    private final MonobankService monobankService;
    private final BinanceService binanceService;

    public CallBackHandler(MonobankService monobankService, BinanceService binanceService) {
        this.monobankService = monobankService;
        this.binanceService = binanceService;
    }

    public void handle(Update update) {
        String data = update.callbackQuery().data();
        long chatId = update.callbackQuery().message().chat().id();
        int messageId = update.callbackQuery().message().messageId();
        switch (data) {
            case "monobank": {
                InlineKeyboardButton balance = new InlineKeyboardButton("Get card balance ".concat(CARD_EMOJI))
                        .callbackData("monobank.balance");
                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(balance);
                EditMessageText messageText = new EditMessageText(chatId, messageId, "Chose monobank option");
                messageText.replyMarkup(keyboard);
                Bot.editMessageText(messageText);
                break;
            }
            case "monobank.balance": {
                EditMessageText messageText = new EditMessageText(chatId, messageId, monobankService.getMonobankBalance());
                Bot.editMessageText(messageText);
                break;
            }
            case "binance": {
                InlineKeyboardButton track = new InlineKeyboardButton("Track currency ")
                        .callbackData("binance.track");
                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(track);
                EditMessageText messageText = new EditMessageText(chatId, messageId, "Chose binance option");
                messageText.replyMarkup(keyboard);
                Bot.editMessageText(messageText);
                break;
            }
        }
        if (data.contains("binance.track")) {
            if (data.equals("binance.track.next")) {
                currentPage++;
            } else if (data.equals("binance.track.back")) {
                currentPage--;
            } else if (currencies.isEmpty()) {
                currencies = binanceService.getAll();
            }
            if(data.contains("binance.track.select.")){
                String currencyIndex = data.replace("binance.track.select.","");
                Currency currency= currencies.get(Integer.parseInt(currencyIndex));
                currency.setTracking(!currency.isTracking());
                binanceService.update(currency);
            }
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            List<InlineKeyboardButton> row2 = new ArrayList<>();
            List<InlineKeyboardButton> row3 = new ArrayList<>();
            List<InlineKeyboardButton> row4 = new ArrayList<>();
            int j;
            for (int i = 0; i < 3; i++) {
                j = i + 9 * currentPage;
                if (currencies.size() > j) {
                    Currency currency = currencies.get(j);
                    row1.add(new InlineKeyboardButton(currency.getName().concat(binanceService.isTracked(currency)))
                            .callbackData("binance.track.select.".concat(String.valueOf(j))));
                }
            }
            for (int i = 3; i < 6; i++) {
                j = i + 9 * currentPage;
                if (currencies.size() > j) {
                    Currency currency = currencies.get(j);
                    row2.add(new InlineKeyboardButton(currency.getName().concat(binanceService.isTracked(currency)))
                            .callbackData("binance.track.select.".concat(String.valueOf(j))));
                }
            }
            for (int i = 6; i < 9; i++) {
                j = i + 9 * currentPage;
                if (currencies.size() > j) {
                    Currency currency = currencies.get(j);
                    row3.add(new InlineKeyboardButton(currency.getName().concat(binanceService.isTracked(currency)))
                            .callbackData("binance.track.select.".concat(String.valueOf(j))));
                }
            }

            if (currentPage == 0) {
                row4.add(new InlineKeyboardButton("---->").callbackData("binance.track.next"));
            } else if (Math.ceil((double) currencies.size() / 9) == (double) currentPage + 1) {
                row4.add(new InlineKeyboardButton("<----").callbackData("binance.track.back"));
            } else {
                row4.add(new InlineKeyboardButton("<----").callbackData("binance.track.back"));
                row4.add(new InlineKeyboardButton("---->").callbackData("binance.track.next"));
            }

            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.addRow(row1.toArray(new InlineKeyboardButton[0]));
            inlineKeyboardMarkup.addRow(row2.toArray(new InlineKeyboardButton[0]));
            inlineKeyboardMarkup.addRow(row3.toArray(new InlineKeyboardButton[0]));
            inlineKeyboardMarkup.addRow(row4.toArray(new InlineKeyboardButton[0]));

            EditMessageText messageText = new EditMessageText(chatId, messageId, "Curr:")
                    .replyMarkup(inlineKeyboardMarkup);
            Bot.editMessageText(messageText);

        }
    }


}
