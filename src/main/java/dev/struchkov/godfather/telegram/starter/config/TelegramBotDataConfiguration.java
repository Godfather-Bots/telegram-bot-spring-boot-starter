package dev.struchkov.godfather.telegram.starter.config;

import dev.struchkov.godfather.simple.context.repository.PersonSettingRepository;
import dev.struchkov.godfather.simple.context.repository.StorylineContext;
import dev.struchkov.godfather.simple.context.repository.StorylineRepository;
import dev.struchkov.godfather.simple.context.repository.UnitPointerRepository;
import dev.struchkov.godfather.simple.core.service.StorylineContextMapImpl;
import dev.struchkov.godfather.simple.data.repository.impl.PersonSettingLocalRepository;
import dev.struchkov.godfather.simple.data.repository.impl.StorylineMapRepository;
import dev.struchkov.godfather.simple.data.repository.impl.UnitPointLocalRepository;
import dev.struchkov.godfather.telegram.simple.context.repository.SenderRepository;
import dev.struchkov.godfather.telegram.simple.core.TelegramConnectBot;
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
    @ConditionalOnBean(TelegramConnectBot.class)
    @ConditionalOnMissingBean(UnitPointerRepository.class)
    public UnitPointerRepository unitPointerRepository() {
        return new UnitPointLocalRepository();
    }

    @Bean
    @ConditionalOnBean(TelegramConnectBot.class)
    @ConditionalOnMissingBean(PersonSettingRepository.class)
    public PersonSettingRepository personSettingRepository() {
        return new PersonSettingLocalRepository();
    }

    @Bean
    @ConditionalOnBean(TelegramConnectBot.class)
    @ConditionalOnMissingBean(StorylineRepository.class)
    public StorylineRepository storylineRepository() {
        return new StorylineMapRepository();
    }

    @Bean
    @ConditionalOnMissingBean(SenderRepository.class)
    public SenderRepository senderRepository() {
        return new SenderMapRepository();
    }

    @Bean
    @ConditionalOnBean(TelegramConnectBot.class)
    public StorylineContext storylineContext() {
        return new StorylineContextMapImpl();
    }

}
