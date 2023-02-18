package dev.struchkov.godfather.telegram.starter;

import dev.struchkov.godfather.telegram.domain.config.ProxyConfig;
import dev.struchkov.godfather.telegram.domain.config.TelegramConnectConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class TelegramBotConfigAutoconfiguration {

    @Bean
    @ConfigurationProperties("telegram-bot.proxy-config")
    @ConditionalOnProperty(prefix = "telegram-bot.proxy-config", name = "enable", havingValue = "true")
    public ProxyConfig proxyConfig() {
        return new ProxyConfig();
    }

    @Bean
    @ConfigurationProperties("telegram-bot")
    @ConditionalOnProperty(prefix = "telegram-bot", name = "bot-username")
    public TelegramConnectConfig telegramConfig() {
        return new TelegramConnectConfig();
    }

}
