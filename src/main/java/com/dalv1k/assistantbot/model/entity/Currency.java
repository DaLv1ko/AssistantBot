package com.dalv1k.assistantbot.model.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private double lastPrice;

    private boolean tracking;

    private float margin;

    private double maxPrice;

    private double targetPrice;

}
