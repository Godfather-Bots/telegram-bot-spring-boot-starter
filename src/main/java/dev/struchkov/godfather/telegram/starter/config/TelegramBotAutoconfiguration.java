package dev.struchkov.godfather.telegram.starter.config;

import dev.struchkov.godfather.main.domain.content.Mail;
import dev.struchkov.godfather.simple.context.repository.PersonSettingRepository;
import dev.struchkov.godfather.simple.context.repository.StorylineRepository;
import dev.struchkov.godfather.simple.context.repository.UnitPointerRepository;
import dev.struchkov.godfather.simple.context.service.ErrorHandler;
import dev.struchkov.godfather.simple.context.service.EventHandler;
import dev.struchkov.godfather.simple.context.service.PersonSettingService;
import dev.struchkov.godfather.simple.context.service.UnitPointerService;
import dev.struchkov.godfather.simple.core.action.AnswerTextAction;
import dev.struchkov.godfather.simple.core.provider.StoryLineHandler;
import dev.struchkov.godfather.simple.core.service.PersonSettingServiceImpl;
import dev.struchkov.godfather.simple.core.service.StorylineMailService;
import dev.struchkov.godfather.simple.core.service.StorylineService;
import dev.struchkov.godfather.simple.core.service.UnitPointerServiceImpl;
import dev.struchkov.godfather.telegram.main.context.TelegramConnect;
import dev.struchkov.godfather.telegram.simple.consumer.EventDistributorService;
import dev.struchkov.godfather.telegram.simple.context.repository.SenderRepository;
import dev.struchkov.godfather.telegram.simple.context.service.EventDistributor;
import dev.struchkov.godfather.telegram.simple.context.service.TelegramSending;
import dev.struchkov.godfather.telegram.simple.context.service.TelegramService;
import dev.struchkov.godfather.telegram.simple.core.MailAutoresponderTelegram;
import dev.struchkov.godfather.telegram.simple.core.TelegramConnectBot;
import dev.struchkov.godfather.telegram.simple.core.service.TelegramServiceImpl;
import dev.struchkov.godfather.telegram.simple.sender.TelegramSender;
import dev.struchkov.godfather.telegram.starter.UnitConfiguration;
import dev.struchkov.godfather.telegram.starter.property.TelegramBotAutoresponderProperty;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dev.struchkov.godfather.telegram.starter.TelegramBotBeanName.AUTORESPONDER_EXECUTORS_SERVICE;
import static dev.struchkov.haiti.utils.Checker.checkNotNull;

@Configuration
@AutoConfigureAfter(TelegramBotDataConfiguration.class)
public class TelegramBotAutoconfiguration {

    @ConditionalOnBean(TelegramConnectBot.class)
    @Bean(AUTORESPONDER_EXECUTORS_SERVICE)
    public ExecutorService executorService(
            TelegramBotAutoresponderProperty autoresponderProperty
    ) {
        return Executors.newFixedThreadPool(autoresponderProperty.getThreads());
    }

    @Bean
    @ConditionalOnBean(TelegramConnect.class)
    public TelegramService telegramService(TelegramConnect telegramConnect) {
        return new TelegramServiceImpl(telegramConnect);
    }

    @Bean
    @ConditionalOnBean(UnitPointerRepository.class)
    public UnitPointerService unitPointerService(UnitPointerRepository unitPointerRepository) {
        return new UnitPointerServiceImpl(unitPointerRepository);
    }

    @Bean
    @ConditionalOnBean(PersonSettingRepository.class)
    public PersonSettingService personSettingService(PersonSettingRepository personSettingRepository) {
        return new PersonSettingServiceImpl(personSettingRepository);
    }

    @Bean
    @ConditionalOnBean(TelegramConnectBot.class)
    public MailAutoresponderTelegram messageAutoresponderTelegram(
            @Qualifier(AUTORESPONDER_EXECUTORS_SERVICE) ObjectProvider<ExecutorService> executorServiceProvider,
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
    @ConditionalOnBean(MailAutoresponderTelegram.class)
    public StoryLineHandler storyLineHandler(MailAutoresponderTelegram mailAutoresponderTelegram) {
        return new StoryLineHandler(mailAutoresponderTelegram);
    }

    @Bean
    @ConditionalOnBean(TelegramConnectBot.class)
    public EventDistributor eventDistributor(
            TelegramConnectBot telegramConnect, List<EventHandler> eventProviders
    ) {
        return new EventDistributorService(telegramConnect, eventProviders);
    }

    @Bean
    @ConditionalOnBean(value = {UnitPointerService.class, StorylineRepository.class})
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
