package dev.struchkov.godfather.telegram.starter.config;

import dev.struchkov.godfather.telegram.domain.config.ProxyConfig;
import dev.struchkov.godfather.telegram.domain.config.TelegramBotConfig;
import dev.struchkov.godfather.telegram.starter.property.TelegramBotAutoresponderProperty;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static dev.struchkov.haiti.utils.Checker.checkNotNull;

@Configuration
public class TelegramBotPropertyConfiguration {

    @Bean
    @ConfigurationProperties("telegram.proxy")
    @ConditionalOnProperty(prefix = "telegram.proxy", name = "enable", havingValue = "true")
    public ProxyConfig proxyConfig() {
        return new ProxyConfig();
    }

    @Bean
    @ConfigurationProperties("telegram.bot")
    @ConditionalOnProperty(prefix = "telegram.bot", name = "token")
    public TelegramBotConfig telegramConfig(
            ObjectProvider<ProxyConfig> proxyConfigProvider
    ) {
        final TelegramBotConfig telegramBotConfig = new TelegramBotConfig();

        final ProxyConfig proxyConfig = proxyConfigProvider.getIfAvailable();
        if (checkNotNull(proxyConfig)) {
            telegramBotConfig.setProxyConfig(proxyConfig);
        }

        return telegramBotConfig;
    }

    @Bean
    @ConfigurationProperties("telegram.bot.autoresponder")
    public TelegramBotAutoresponderProperty telegramBotAutoresponderProperty() {
        return new TelegramBotAutoresponderProperty();
    }

}
