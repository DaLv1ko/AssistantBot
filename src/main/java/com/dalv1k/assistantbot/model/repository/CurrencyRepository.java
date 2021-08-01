package com.dalv1k.assistantbot.model.repository;


import com.dalv1k.assistantbot.model.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    List<Currency> findAllByOrderByIdAsc();
    List<Currency> findAllByTrackingTrue();
}
