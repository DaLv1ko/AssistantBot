package com.dalv1k.assistantbot.model.repository;

import com.dalv1k.assistantbot.model.entity.DeleteMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeleteMessageRepository extends JpaRepository<DeleteMessage,Long> {
    void deleteByMessageId(long messageId);
}
