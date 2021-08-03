package com.dalv1k.assistantbot.handler;

import com.dalv1k.assistantbot.bot.BotState;
import com.dalv1k.assistantbot.model.entity.BotData;
import com.dalv1k.assistantbot.model.repository.BotDataRepository;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UpdateHandler {

    private final CallBackHandler callBackHandler;
    private final MessageHandler messageHandler;
    private final BotDataRepository botDataRepository;
    private final BinanceSearchHandler binanceSearchHandler;

    public UpdateHandler(CallBackHandler callBackHandler, MessageHandler messageHandler, BotDataRepository botDataRepository, BinanceSearchHandler binanceSearchHandler) {
        this.callBackHandler = callBackHandler;
        this.messageHandler = messageHandler;
        this.botDataRepository = botDataRepository;
        this.binanceSearchHandler = binanceSearchHandler;
    }

    public void handleUpdate(Update update) {

        Optional<BotData> botDataOptional = botDataRepository.findById(1L);
        BotData botData;
        if(!botDataOptional.isPresent()){
             botData = new BotData(BotState.DEFAULT);
            botDataRepository.save(botData);
        }else {
             botData = botDataOptional.get();
             if(botData.getBotState().equals(BotState.SEARCH)){
                binanceSearchHandler.handle(update);

             } else {
                 if (update.message() != null) {
                     messageHandler.handle(update);
                 }
                 if (update.callbackQuery() != null) {
                     callBackHandler.handle(update);
                 }
             }
        }


    }

}
