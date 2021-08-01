package com.dalv1k.assistantbot.model.repository;

import com.dalv1k.assistantbot.model.entity.TrackMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackMessageRepository extends JpaRepository<TrackMessage, Long> {
}
