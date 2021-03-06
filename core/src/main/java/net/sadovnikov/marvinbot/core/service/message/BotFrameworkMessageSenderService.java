package net.sadovnikov.marvinbot.core.service.message;

import com.google.inject.Inject;
import com.google.inject.Injector;
import net.sadovnikov.marvinbot.core.domain.message.Attachment;
import net.sadovnikov.marvinbot.core.domain.message.MessageToSend;
import net.sadovnikov.marvinbot.core.domain.message.SentMessage;
import net.sadovnikov.marvinbot.core.service.botframework.BotSelfAddressCachingService;
import net.sadovnikov.mbf4j.*;
import net.sadovnikov.mbf4j.http.Conversation;
import net.sadovnikov.mbf4j.http.HttpException;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.io.IOException;
import java.util.Optional;

public class BotFrameworkMessageSenderService extends MessageSenderService {

    protected Bot bot;
    protected Injector injector;
    protected BotSelfAddressCachingService botAddressCachingService;

    @Inject
    public BotFrameworkMessageSenderService(Bot bot, Injector injector) {
        this.bot = bot;
        this.injector = injector;
        this.botAddressCachingService = injector.getInstance(BotSelfAddressCachingService.class);
    }

    @Override
    public SentMessage send(MessageToSend message) throws MessageSenderException {
        try {
            String channelId = message.chat().channel().id();
            try {
                Channel.Types channelType = Channel.Types.valueOf(channelId);
                Channel channel = new Channel(channelType);
                Conversation conversation = new Conversation(message.chat().chatId(), "", message.chat().isGroupChat());
                net.sadovnikov.mbf4j.activities.outcoming.MessageToSend mbfMessage = new net.sadovnikov.mbf4j.activities.outcoming.MessageToSend(
                        channel,
                        conversation,
                        message.text()
                );
                Optional<Address> from = botAddressCachingService.getBotAddressForChannel(message.chat().channel());
                if (!from.isPresent()) {
                    throw new MessageSenderException("cannot find bot id for channel " + message.chat().channel().id());
                }
                mbfMessage.withFrom(from.get());

                try {
                    if (message.attachments().size() > 0) {
                        for (Attachment attachment : message.attachments()) {
                            AttachmentToUpload mbfAttachment = new AttachmentToUpload(attachment.bytes(), new MimeType(attachment.mimeType()));
                            UploadedAttachment mbfUploadedAttachment = bot.attachments().put(channel, conversation, mbfAttachment);
                            mbfMessage.addAttachment(mbfUploadedAttachment);
                        }
                    }
                } catch (IOException | MimeTypeParseException | RepositoryException e) {
                    throw new MessageSenderException(e);
                }

                net.sadovnikov.mbf4j.activities.outcoming.SentMessage sentMessage = bot.messageSender().send(mbfMessage);
                return new SentMessage(sentMessage.id(), message.chat(), message.text());

            } catch (IllegalArgumentException e) {
                throw new MessageSenderException("Unknown channel type " + channelId);
            }

        } catch (ApiException | HttpException e) {
            throw new MessageSenderException(e);
        }

    }
}
