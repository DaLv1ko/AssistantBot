package com.dalv1k.assistantbot.service;

import com.dalv1k.assistantbot.bot.Bot;
import com.dalv1k.assistantbot.model.entity.Currency;
import com.dalv1k.assistantbot.model.entity.TrackMessage;
import com.dalv1k.assistantbot.model.repository.CurrencyRepository;
import com.dalv1k.assistantbot.model.repository.TrackMessageRepository;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.dalv1k.assistantbot.util.Emoji.*;

@Service
@Slf4j
public class BinanceService {

    private final CurrencyRepository currencyRepository;
    private final TrackMessageRepository trackMessageRepository;

    public BinanceService(CurrencyRepository currencyRepository, TrackMessageRepository trackMessageRepository) {
        this.currencyRepository = currencyRepository;
        this.trackMessageRepository = trackMessageRepository;
    }

    public List<Currency> getCurrencies() {
        List<Currency> currencies = new ArrayList<>();
        HttpResponse<JsonNode> response = Unirest.get("https://api.binance.com/api/v3/ticker/price").asJson();
        if (response.getStatus() == 200) {
            JSONArray data = response.getBody().getArray();
            for (int i = 0; i < data.length(); i++) {
                JSONObject currencyObj = data.getJSONObject(i);
                if (currencyObj.getString("symbol").endsWith("USDT")) {
                    Currency currency = new Currency();
                    currency.setName(currencyObj.getString("symbol").replace("USDT", ""));
                    currency.setLastPrice(currencyObj.getDouble("price"));
                    currency.setMargin((float) (currency.getLastPrice() / 10));

                    currencies.add(currency);
                }
            }
            return currencies;
        } else {
            Bot.sendMessage("Error in response from Binance: ".concat(response.getBody().toString()));
        }
        return Collections.emptyList();
    }

    public List<Currency> getAll() {
        List<Currency> currencies = currencyRepository.findAllByOrderByIdAsc();
        if (currencies.isEmpty()) currencyRepository.saveAll(getCurrencies());
        return currencies;
    }

    public void update(Currency currency) {
        currencyRepository.save(currency);
    }

    public String isTracked(Currency currency) {
        return currency.isTracking() ? YES : NO;
    }

    public void checkCurrencies() {
        List<Currency> currencies = currencyRepository.findAllByTrackingTrue();
        List<Currency> updates = getCurrencies();
        currencies.forEach(currency -> updates.forEach(update -> {
            if (currency.getName().equals(update.getName()) &&
                    Math.abs(currency.getLastPrice() - update.getLastPrice()) >= currency.getMargin()) {
                TrackMessage trackMessage;
                if (currency.getLastPrice() > update.getLastPrice()) {
                    trackMessage = new TrackMessage(Bot
                            .sendMessage(RED_SQUARE + " " + currency.getName() + " dropped to: " + update.getLastPrice()
                                    + "\n" + MAX_PRICE + " Max price: " + currency.getMaxPrice()
                                    + "\n" + TARGET + "Target price: " + currency.getTargetPrice())
                            .message().messageId());
                } else {
                    trackMessage = new TrackMessage(Bot
                            .sendMessage(GREEN_SQUARE + " " + currency.getName() + " grown to: " + update.getLastPrice()
                                    + "\n" + MAX_PRICE + " Max price: " + currency.getMaxPrice()
                                    + "\n" + TARGET + "Target price: " + currency.getTargetPrice())
                            .message().messageId());
                }
                if (currency.getMaxPrice() < update.getLastPrice()) {
                    currency.setMaxPrice(update.getLastPrice());
                }
                currency.setLastPrice(update.getLastPrice());
                currency.setMargin(update.getMargin());
                currencyRepository.save(currency);
                trackMessageRepository.save(trackMessage);
            }
        }));
    }

    public void deleteTrackMessages() {
        List<TrackMessage> trackMessages = trackMessageRepository.findAll();
        trackMessages.forEach(trackMessage -> {
            Bot.deleteMessage(trackMessage.getMessageId());
            trackMessageRepository.delete(trackMessage);
        });
    }
}
