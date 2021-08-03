package com.dalv1k.assistantbot.model.entity;

import com.dalv1k.assistantbot.bot.BotState;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity
@NoArgsConstructor
public class BotData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private BotState botState;

    public BotData(BotState botState){
        this.botState = botState;
    }
}
