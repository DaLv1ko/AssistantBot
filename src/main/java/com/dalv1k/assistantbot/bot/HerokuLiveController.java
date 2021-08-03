package com.dalv1k.assistantbot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HerokuLiveController {

    @GetMapping
    public void live(){
    }
}
