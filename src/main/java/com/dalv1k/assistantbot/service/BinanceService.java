package com.dalv1k.assistantbot.service;

import com.dalv1k.assistantbot.Bot;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.dalv1k.assistantbot.util.Emoji.NO;
import static com.dalv1k.assistantbot.util.Emoji.YES;

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
                if (currencyObj.getString("symbol").endsWith("BUSD")) {
                    Currency currency = new Currency();
                    currency.setName(currencyObj.getString("symbol").replace("BUSD", ""));
                    currency.setLastPrice(currencyObj.getDouble("price"));
                    currencies.add(currency);
                }
            }
            return currencies;
        } else {
            Bot.sendMessage("Error in response from Binance: "
                    .concat(response.getBody().toString()));
        }
        return Collections.emptyList();
    }

    public List<Currency> getAll() {
        List<Currency> currencies = currencyRepository.findAllByOrderByIdAsc();
        if (currencies.isEmpty()) {
            currencies = getCurrencies();
            currencies.forEach(currencyRepository::save);
        }
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
                    currency.getMargin() != 0 &&
                    Math.abs(currency.getLastPrice() - update.getLastPrice()) >= currency.getMargin()) {
                currency.setLastPrice(update.getLastPrice());
                currencyRepository.save(currency);
                TrackMessage trackMessage = new TrackMessage();
                trackMessage.setMessageId(Bot.sendMessage("New " + currency.getName() + " price: " + currency.getLastPrice())
                        .message().messageId());
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
