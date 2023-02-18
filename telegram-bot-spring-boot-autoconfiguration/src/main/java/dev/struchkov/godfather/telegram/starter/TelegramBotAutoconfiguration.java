package dev.struchkov.godfather.telegram.starter;

import dev.struchkov.godfather.main.domain.content.Mail;
import dev.struchkov.godfather.simple.context.service.ErrorHandler;
import dev.struchkov.godfather.simple.context.service.EventHandler;
import dev.struchkov.godfather.simple.context.service.PersonSettingService;
import dev.struchkov.godfather.simple.context.service.UnitPointerService;
import dev.struchkov.godfather.simple.core.action.AnswerTextAction;
import dev.struchkov.godfather.simple.core.provider.StoryLineHandler;
import dev.struchkov.godfather.simple.core.service.PersonSettingServiceImpl;
import dev.struchkov.godfather.simple.core.service.StorylineContextMapImpl;
import dev.struchkov.godfather.simple.core.service.StorylineMailService;
import dev.struchkov.godfather.simple.core.service.StorylineService;
import dev.struchkov.godfather.simple.core.service.UnitPointerServiceImpl;
import dev.struchkov.godfather.simple.data.StorylineContext;
import dev.struchkov.godfather.simple.data.repository.PersonSettingRepository;
import dev.struchkov.godfather.simple.data.repository.StorylineRepository;
import dev.struchkov.godfather.simple.data.repository.UnitPointerRepository;
import dev.struchkov.godfather.simple.data.repository.impl.PersonSettingLocalRepository;
import dev.struchkov.godfather.simple.data.repository.impl.StorylineMapRepository;
import dev.struchkov.godfather.simple.data.repository.impl.UnitPointLocalRepository;
import dev.struchkov.godfather.telegram.domain.config.TelegramConnectConfig;
import dev.struchkov.godfather.telegram.main.context.TelegramConnect;
import dev.struchkov.godfather.telegram.simple.consumer.EventDistributorService;
import dev.struchkov.godfather.telegram.simple.context.repository.SenderRepository;
import dev.struchkov.godfather.telegram.simple.context.service.EventDistributor;
import dev.struchkov.godfather.telegram.simple.context.service.TelegramSending;
import dev.struchkov.godfather.telegram.simple.context.service.TelegramService;
import dev.struchkov.godfather.telegram.simple.core.MailAutoresponderTelegram;
import dev.struchkov.godfather.telegram.simple.core.TelegramConnectBot;
import dev.struchkov.godfather.telegram.simple.core.service.SenderMapRepository;
import dev.struchkov.godfather.telegram.simple.core.service.TelegramServiceImpl;
import dev.struchkov.godfather.telegram.simple.sender.TelegramSender;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dev.struchkov.haiti.utils.Checker.checkNotNull;

@Configuration
public class TelegramBotAutoconfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "telegram-bot", name = "bot-username")
    public TelegramConnectBot telegramConnectBot(TelegramConnectConfig telegramConfig) {
        return new TelegramConnectBot(telegramConfig);
    }

//    @Bean
//    @ConditionalOnMissingBean(TelegramConnectBot.class)
//    @ConditionalOnProperty("telegram-bot.bot-username")
//    public TelegramConnect telegramDefaultConnect(TelegramConnectConfig telegramConfig) {
//        return new TelegramDefaultConnect(telegramConfig);
//    }

    @Bean("messageExecutorService")
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(3);
    }

    @Bean
    @ConditionalOnBean(TelegramConnect.class)
    public TelegramService telegramService(TelegramConnect telegramConnect) {
        return new TelegramServiceImpl(telegramConnect);
    }

    @Bean
    public StorylineContext storylineContext() {
        return new StorylineContextMapImpl();
    }

    @Bean
    public UnitPointerRepository unitPointerRepository() {
        return new UnitPointLocalRepository();
    }

    @Bean
    public UnitPointerService unitPointerService(UnitPointerRepository unitPointerRepository) {
        return new UnitPointerServiceImpl(unitPointerRepository);
    }

    @Bean
    public PersonSettingRepository personSettingRepository() {
        return new PersonSettingLocalRepository();
    }

    @Bean
    public PersonSettingService personSettingService(PersonSettingRepository personSettingRepository) {
        return new PersonSettingServiceImpl(personSettingRepository);
    }

    @Bean
    public StorylineRepository storylineRepository() {
        return new StorylineMapRepository();
    }

    @Bean
    public MailAutoresponderTelegram messageAutoresponderTelegram(
            @Qualifier("messageExecutorService") ObjectProvider<ExecutorService> executorServiceProvider,
            TelegramSending sending,
            PersonSettingService personSettingService,
            ObjectProvider<ErrorHandler> errorHandlerProvider,
            ObjectProvider<AnswerTextAction> answerTextActionProvider,

            StorylineService<Mail> storylineService
    ) {
        final MailAutoresponderTelegram autoresponder = new MailAutoresponderTelegram(
                sending, personSettingService, storylineService
        );

        final ExecutorService executorService = executorServiceProvider.getIfAvailable();
        if (checkNotNull(executorService)) {
            autoresponder.setExecutorService(executorService);
        }

        final ErrorHandler errorHandler = errorHandlerProvider.getIfAvailable();
        if (checkNotNull(errorHandler)) {
            autoresponder.setErrorHandler(errorHandler);
        }

        final AnswerTextAction answerTextAction = answerTextActionProvider.getIfAvailable();
        if (checkNotNull(answerTextAction)) {
            autoresponder.initTextAnswerActionUnit(answerTextAction);
        }
        return autoresponder;
    }

    @Bean
    public SenderRepository senderRepository() {
        return new SenderMapRepository();
    }

    @Bean
    @ConditionalOnBean(TelegramConnect.class)
    public TelegramSending sending(
            TelegramConnect telegramConnect,
            ObjectProvider<SenderRepository> senderRepositoryProvider
    ) {
        final TelegramSender telegramSender = new TelegramSender(telegramConnect);

        final SenderRepository senderRepository = senderRepositoryProvider.getIfAvailable();
        if (checkNotNull(senderRepository)) {
            telegramSender.setSenderRepository(senderRepository);
        }

        return telegramSender;
    }

    @Bean
    public StoryLineHandler storyLineHandler(MailAutoresponderTelegram mailAutoresponderTelegram) {
        return new StoryLineHandler(mailAutoresponderTelegram);
    }

    @Bean
    public EventDistributor eventDistributor(
            TelegramConnectBot telegramConnect, List<EventHandler> eventProviders
    ) {
        return new EventDistributorService(telegramConnect, eventProviders);
    }

    @Bean
    public StorylineService<Mail> storylineService(
            UnitPointerService unitPointerService,
            StorylineRepository storylineRepository,
            List<UnitConfiguration> unitConfigurations
    ) {
        return new StorylineMailService(
                unitPointerService,
                storylineRepository,
                new ArrayList<>(unitConfigurations)
        );
    }

}
