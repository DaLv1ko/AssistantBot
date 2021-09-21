package com.dalv1k.assistantbot.handler;

import com.dalv1k.assistantbot.bot.Bot;
import com.dalv1k.assistantbot.bot.BotState;
import com.dalv1k.assistantbot.model.entity.BotData;
import com.dalv1k.assistantbot.model.entity.Currency;
import com.dalv1k.assistantbot.model.repository.BotDataRepository;
import com.dalv1k.assistantbot.service.BinanceService;
import com.dalv1k.assistantbot.service.MonobankService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.dalv1k.assistantbot.util.Emoji.CARD_EMOJI;
import static com.dalv1k.assistantbot.util.Emoji.MAG;

@Service
public class CallBackHandler {

    private List<Currency> currencies = new ArrayList<>();
    private static int currentPage = 0;

    private final MonobankService monobankService;
    private final BinanceService binanceService;
    private final BotDataRepository botDataRepository;


    public CallBackHandler(MonobankService monobankService, BinanceService binanceService,
                           BotDataRepository botDataRepository) {
        this.monobankService = monobankService;
        this.binanceService = binanceService;
        this.botDataRepository = botDataRepository;
    }

    //TODO fix GAVNOKOD
    public void handle(Update update) {
        String data = update.callbackQuery().data();
        long chatId = update.callbackQuery().message().chat().id();
        int messageId = update.callbackQuery().message().messageId();
        switch (data) {
            case "close": {
                Bot.deleteMessage(messageId);
                break;
            }
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
            case "binance.track.search": {
                Bot.deleteMessage(messageId);
                SendMessage sendMessage = new SendMessage(chatId, "Write currency name");
                Optional<BotData> botDataOptional = botDataRepository.findById(1L);
                if (botDataOptional.isPresent()) {
                    BotData botData = botDataOptional.get();
                    botData.setBotState(BotState.SEARCH);
                    botDataRepository.save(botData);
                }
                Bot.sendMessage(sendMessage);
                break;
            }
        }
        if (data.contains("binance.track")) {
            if (currencies.isEmpty()) {
                currencies = binanceService.getAll();
            } else if (data.equals("binance.track.next")) {
                currentPage++;
            } else if (data.equals("binance.track.back")) {
                currentPage--;
            }
            sortCoins();
            AtomicBoolean search = new AtomicBoolean(false);
            if (data.contains("binance.track.select.")) {
                String currencyId = data.replace("binance.track.select.", "").replace("one.", "");
                currencies.forEach(currency -> {
                    if (currency.getId() == Long.parseLong(currencyId)) {
                        currency.setTracking(!currency.isTracking());
                        binanceService.update(currency);
                        if (data.contains("binance.track.select.one.")) {
                            EditMessageText editMessageText = new EditMessageText(chatId, messageId, "Found:");

                            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(
                                    new InlineKeyboardButton(currency.getName().concat(binanceService.isTracked(currency)))
                                            .callbackData("binance.track.select.one.".concat(String.valueOf(currency.getId()))));
                            inlineKeyboardMarkup.addRow(new InlineKeyboardButton(" <--- Back to menu").callbackData("binance.track"));
                            editMessageText.replyMarkup(inlineKeyboardMarkup);
                            Bot.editMessageText(editMessageText);
                            search.set(true);
                        }
                    }
                });
                sortCoins();
            }
            if (!search.get()) {
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
                                .callbackData("binance.track.select.".concat(String.valueOf(currency.getId()))));
                    }
                }
                for (int i = 3; i < 6; i++) {
                    j = i + 9 * currentPage;
                    if (currencies.size() > j) {
                        Currency currency = currencies.get(j);
                        row2.add(new InlineKeyboardButton(currency.getName().concat(binanceService.isTracked(currency)))
                                .callbackData("binance.track.select.".concat(String.valueOf(currency.getId()))));
                    }
                }
                for (int i = 6; i < 9; i++) {
                    j = i + 9 * currentPage;
                    if (currencies.size() > j) {
                        Currency currency = currencies.get(j);
                        row3.add(new InlineKeyboardButton(currency.getName().concat(binanceService.isTracked(currency)))
                                .callbackData("binance.track.select.".concat(String.valueOf(currency.getId()))));
                    }
                }

                if (currentPage == 0) {
                    row4.add(new InlineKeyboardButton("Search " + MAG).callbackData("binance.track.search"));
                    row4.add(new InlineKeyboardButton("---->").callbackData("binance.track.next"));
                } else if (Math.ceil((double) currencies.size() / 9) == (double) currentPage + 1) {
                    row4.add(new InlineKeyboardButton("<----").callbackData("binance.track.back"));
                    row4.add(new InlineKeyboardButton("Search " + MAG).callbackData("binance.track.search"));
                } else {
                    row4.add(new InlineKeyboardButton("<----").callbackData("binance.track.back"));
                    row4.add(new InlineKeyboardButton("Search " + MAG).callbackData("binance.track.search"));
                    row4.add(new InlineKeyboardButton("---->").callbackData("binance.track.next"));
                }

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                inlineKeyboardMarkup.addRow(row1.toArray(new InlineKeyboardButton[0]));
                inlineKeyboardMarkup.addRow(row2.toArray(new InlineKeyboardButton[0]));
                inlineKeyboardMarkup.addRow(row3.toArray(new InlineKeyboardButton[0]));
                inlineKeyboardMarkup.addRow(row4.toArray(new InlineKeyboardButton[0]));
                inlineKeyboardMarkup.addRow(new InlineKeyboardButton("Close").callbackData("close"));
                int pages = (int) (Math.ceil((double) currencies.size() / 9));
                int tempPage = currentPage + 1;
                EditMessageText messageText = new EditMessageText(chatId, messageId, " === Currencies list. Page "
                        + tempPage + "/" +
                        --pages + " === ")
                        .replyMarkup(inlineKeyboardMarkup);
                Bot.editMessageText(messageText);
            }
        }
    }

    public static void closeMenu() {
        currentPage = 0;
    }

    private void sortCoins() {
        currencies.sort((o1, o2) -> Boolean.compare(o2.isTracking(), o1.isTracking()));
    }
}
