package dev.struchkov.godfather.telegram.starter.config;

import dev.struchkov.godfather.telegram.domain.config.ProxyConfig;
import dev.struchkov.godfather.telegram.domain.config.TelegramBotConfig;
import dev.struchkov.godfather.telegram.simple.context.service.TelegramConnect;
import dev.struchkov.godfather.telegram.simple.core.TelegramDefaultConnect;
import dev.struchkov.godfather.telegram.simple.core.TelegramPollingConnect;
import dev.struchkov.godfather.telegram.simple.core.TelegramWebhookConnect;
import dev.struchkov.godfather.telegram.starter.property.TelegramBotAutoresponderProperty;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "telegram.bot", name = "username")
    public TelegramConnect telegramConnectBot(TelegramBotConfig telegramConfig) {
        return new TelegramPollingConnect(telegramConfig);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "telegram.bot", name = "webhook-url")
    public TelegramConnect telegramWebhookConnect(TelegramBotConfig telegramConfig) {
        return new TelegramWebhookConnect(telegramConfig);
    }

    @Bean
    @ConditionalOnProperty(prefix = "telegram.bot", name = "token")
    public TelegramConnect telegramDefaultConnect(TelegramBotConfig telegramConfig) {
        return new TelegramDefaultConnect(telegramConfig);
    }

}
