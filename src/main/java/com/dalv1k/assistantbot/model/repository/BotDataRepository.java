package com.dalv1k.assistantbot.model.repository;

import com.dalv1k.assistantbot.model.entity.BotData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BotDataRepository extends JpaRepository<BotData,Long> {
}
