package dev.struchkov.godfather.telegram.starter.config;

import dev.struchkov.godfather.simple.context.repository.StorylineContext;
import dev.struchkov.godfather.simple.context.repository.StorylineHistoryRepository;
import dev.struchkov.godfather.simple.context.repository.UnitPointerRepository;
import dev.struchkov.godfather.simple.core.service.StorylineContextMapImpl;
import dev.struchkov.godfather.simple.data.repository.impl.StorylineMapHistoryRepository;
import dev.struchkov.godfather.simple.data.repository.impl.UnitPointLocalRepository;
import dev.struchkov.godfather.telegram.simple.context.repository.SenderRepository;
import dev.struchkov.godfather.telegram.simple.context.service.TelegramConnect;
import dev.struchkov.godfather.telegram.simple.core.service.SenderMapRepository;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(TelegramBotPropertyConfiguration.class)
public class TelegramBotDataConfiguration {

    @Bean
    @ConditionalOnBean(TelegramConnect.class)
    @ConditionalOnMissingBean(UnitPointerRepository.class)
    public UnitPointerRepository unitPointerRepository() {
        return new UnitPointLocalRepository();
    }


    @Bean
    @ConditionalOnBean(TelegramConnect.class)
    @ConditionalOnMissingBean(StorylineHistoryRepository.class)
    public StorylineHistoryRepository storylineHistoryRepository() {
        return new StorylineMapHistoryRepository();
    }

    @Bean
    @ConditionalOnMissingBean(SenderRepository.class)
    public SenderRepository senderRepository() {
        return new SenderMapRepository();
    }

    @Bean
    @ConditionalOnBean(TelegramConnect.class)
    public StorylineContext storylineContext() {
        return new StorylineContextMapImpl();
    }

}
