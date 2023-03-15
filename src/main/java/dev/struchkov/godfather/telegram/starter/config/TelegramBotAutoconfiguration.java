package dev.struchkov.godfather.telegram.starter.config;

import dev.struchkov.godfather.main.domain.content.ChatMail;
import dev.struchkov.godfather.main.domain.content.Mail;
import dev.struchkov.godfather.simple.context.repository.PersonSettingRepository;
import dev.struchkov.godfather.simple.context.repository.StorylineRepository;
import dev.struchkov.godfather.simple.context.repository.UnitPointerRepository;
import dev.struchkov.godfather.simple.context.service.ErrorHandler;
import dev.struchkov.godfather.simple.context.service.EventHandler;
import dev.struchkov.godfather.simple.context.service.PersonSettingService;
import dev.struchkov.godfather.simple.context.service.UnitPointerService;
import dev.struchkov.godfather.simple.core.action.AnswerCheckAction;
import dev.struchkov.godfather.simple.core.action.AnswerSaveAction;
import dev.struchkov.godfather.simple.core.action.AnswerTextChatMailAction;
import dev.struchkov.godfather.simple.core.action.AnswerTextMailAction;
import dev.struchkov.godfather.simple.core.provider.ChatStoryLineHandler;
import dev.struchkov.godfather.simple.core.provider.PersonStoryLineHandler;
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
import dev.struchkov.godfather.telegram.simple.core.ChatMailAutoresponderTelegram;
import dev.struchkov.godfather.telegram.simple.core.MailAutoresponderTelegram;
import dev.struchkov.godfather.telegram.simple.core.TelegramConnectBot;
import dev.struchkov.godfather.telegram.simple.core.service.TelegramServiceImpl;
import dev.struchkov.godfather.telegram.simple.sender.TelegramSender;
import dev.struchkov.godfather.telegram.starter.ChatUnitConfiguration;
import dev.struchkov.godfather.telegram.starter.PersonUnitConfiguration;
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

    @Bean("mailStorylineService")
    @ConditionalOnBean(value = {UnitPointerService.class, StorylineRepository.class, PersonUnitConfiguration.class})
    public StorylineService<Mail> mailStorylineService(
            UnitPointerService unitPointerService,
            StorylineRepository storylineRepository,
            List<PersonUnitConfiguration> personUnitConfigurations
    ) {
        return new StorylineMailService<>(
                unitPointerService,
                storylineRepository,
                new ArrayList<>(personUnitConfigurations)
        );
    }

    @Bean("chatMailStorylineService")
    @ConditionalOnBean(value = {UnitPointerService.class, StorylineRepository.class, ChatUnitConfiguration.class})
    public StorylineService<ChatMail> chatMailStorylineService(
            UnitPointerService unitPointerService,
            StorylineRepository storylineRepository,
            List<ChatUnitConfiguration> chatUnitConfigurations
    ) {
        return new StorylineMailService<>(
                unitPointerService,
                storylineRepository,
                new ArrayList<>(chatUnitConfigurations)
        );
    }

    @Bean
    @ConditionalOnBean(name = "chatMailStorylineService")
    public ChatMailAutoresponderTelegram chatMailAutoresponderTelegram(
            @Qualifier(AUTORESPONDER_EXECUTORS_SERVICE) ObjectProvider<ExecutorService> executorServiceProvider,
            PersonSettingService personSettingService,
            ObjectProvider<ErrorHandler> errorHandlerProvider,
            ObjectProvider<AnswerTextChatMailAction> answerTextActionProvider,

            TelegramSending telegramSending,
            StorylineService<ChatMail> storylineService
    ) {
        final ChatMailAutoresponderTelegram autoresponder = new ChatMailAutoresponderTelegram(personSettingService, storylineService);
        autoresponder.registrationActionUnit(new AnswerCheckAction(telegramSending));
        autoresponder.registrationActionUnit(new AnswerSaveAction<>());

        final AnswerTextChatMailAction answerTextAction = answerTextActionProvider.getIfAvailable();
        if (checkNotNull(answerTextAction)) {
            autoresponder.registrationActionUnit(answerTextAction);
        } else {
            autoresponder.registrationActionUnit(new AnswerTextChatMailAction(telegramSending));
        }

        final ExecutorService executorService = executorServiceProvider.getIfAvailable();
        if (checkNotNull(executorService)) {
            autoresponder.setExecutorService(executorService);
        }

        final ErrorHandler errorHandler = errorHandlerProvider.getIfAvailable();
        if (checkNotNull(errorHandler)) {
            autoresponder.setErrorHandler(errorHandler);
        }

        return autoresponder;
    }

    @Bean
    @ConditionalOnBean(name = "mailStorylineService")
    public MailAutoresponderTelegram messageAutoresponderTelegram(
            @Qualifier(AUTORESPONDER_EXECUTORS_SERVICE) ObjectProvider<ExecutorService> executorServiceProvider,
            TelegramSending sending,
            PersonSettingService personSettingService,
            ObjectProvider<ErrorHandler> errorHandlerProvider,
            ObjectProvider<AnswerTextMailAction> answerTextActionProvider,

            StorylineService<Mail> storylineService
    ) {
        final MailAutoresponderTelegram autoresponder = new MailAutoresponderTelegram(personSettingService, storylineService);
        autoresponder.registrationActionUnit(new AnswerCheckAction(sending));
        autoresponder.registrationActionUnit(new AnswerSaveAction<>());

        final AnswerTextMailAction answerTextAction = answerTextActionProvider.getIfAvailable();
        if (checkNotNull(answerTextAction)) {
            autoresponder.registrationActionUnit(answerTextAction);
        } else {
            autoresponder.registrationActionUnit(new AnswerTextMailAction(sending));
        }

        final ExecutorService executorService = executorServiceProvider.getIfAvailable();
        if (checkNotNull(executorService)) {
            autoresponder.setExecutorService(executorService);
        }

        final ErrorHandler errorHandler = errorHandlerProvider.getIfAvailable();
        if (checkNotNull(errorHandler)) {
            autoresponder.setErrorHandler(errorHandler);
        }

        return autoresponder;
    }

    @Bean
    @ConditionalOnBean(MailAutoresponderTelegram.class)
    public EventHandler<Mail> personStoryLineHandler(MailAutoresponderTelegram mailAutoresponderTelegram) {
        return new PersonStoryLineHandler(mailAutoresponderTelegram);
    }

    @Bean
    @ConditionalOnBean(ChatMailAutoresponderTelegram.class)
    public EventHandler<ChatMail> chatStoryLineHandler(ChatMailAutoresponderTelegram mailAutoresponderTelegram) {
        return new ChatStoryLineHandler(mailAutoresponderTelegram);
    }

    @Bean
    @ConditionalOnBean(TelegramConnectBot.class)
    public EventDistributor eventDistributor(
            TelegramConnectBot telegramConnect, List<? extends EventHandler> eventProviders
    ) {
        return new EventDistributorService(telegramConnect, (List<EventHandler>) eventProviders);
    }

}
