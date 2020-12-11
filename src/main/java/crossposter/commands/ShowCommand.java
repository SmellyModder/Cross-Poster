package crossposter.commands;

import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import crossposter.ServerDataHandler;
import crossposter.ServerDataHandler.ChannelData;
import crossposter.ServerDataHandler.ServerData;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.smelly.disparser.Command;
import net.smelly.disparser.CommandContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public final class ShowCommand extends Command {
	private static final AllowedMentions ALLOWED_MENTIONS = AllowedMentions.none();

	public ShowCommand() {
		super("show");
	}

	@Override
	public void processCommand(CommandContext context) {
		GuildMessageReceivedEvent event = context.getEvent();
		Guild guild = event.getGuild();
		TextChannel channel = event.getChannel();
		Message message = event.getMessage();
		String guildId = event.getGuild().getId();
		ServerData serverData = ServerDataHandler.getServerData(guildId);
		if (serverData != null) {
			ChannelData channelData = ChannelData.getCrosspostChannel(serverData.channelData, channel.getIdLong());
			if (channelData == null) return;
			TextChannel crosspostChannel = guild.getTextChannelById(channelData.crosspostChannelId);
			if (crosspostChannel != null) {
				Webhook webhook = this.getWebhookForGuild(channel, serverData);
				if (webhook != null) {
					webhook.getManager().setChannel(crosspostChannel).queue(manager -> {
						ServerDataHandler.writeWebhook(guildId, webhook);

						WebhookClientBuilder builder = new WebhookClientBuilder(webhook.getUrl());
						builder.setThreadFactory((job) -> {
							Thread thread = new Thread(job);
							thread.setName("Cross-Poster");
							thread.setDaemon(true);
							return thread;
						});
						builder.setWait(true);

						WebhookMessageBuilder messageBuilder = new WebhookMessageBuilder();
						Member messageSender = message.getMember();
						String nickname = messageSender != null ? messageSender.getNickname() : null;
						messageBuilder.setUsername(nickname != null ? nickname : message.getAuthor().getName());
						messageBuilder.setAvatarUrl(message.getAuthor().getAvatarUrl());
						messageBuilder.setContent(getShowcaseMessage(message));
						messageBuilder.setAllowedMentions(ALLOWED_MENTIONS);

						List<Attachment> messageAttachments = message.getAttachments();
						if (channelData.requiresAttachment && messageAttachments.isEmpty()) {
							return;
						}

						AtomicInteger attachmentsRetrieved = new AtomicInteger();
						for (Attachment attachment : messageAttachments) {
							attachment.retrieveInputStream().thenAccept(stream -> {
								messageBuilder.addFile(attachment.getFileName(), stream);
								attachmentsRetrieved.getAndIncrement();
							}).exceptionally(throwable -> {
								attachmentsRetrieved.getAndIncrement();
								return null;
							});
						}

						int attachmentsSize = messageAttachments.size();
						while (true) {
							if (attachmentsRetrieved.get() >= attachmentsSize) {
								builder.build().send(messageBuilder.build());
								break;
							}
						}
					});
				}
			}
		}
	}

	@Nullable
	private Webhook getWebhookForGuild(TextChannel channel, ServerData serverData) {
		try {
			List<Webhook> webhooks = channel.getGuild().retrieveWebhooks().submit().get();
			if (webhooks.isEmpty()) {
				return channel.createWebhook("Cross-Poster").submit().get();
			} else {
				for (Webhook webhook : webhooks) {
					if (webhook.getUrl().equals(ServerData.getWebhookURL(serverData))) {
						return webhook;
					}
				}
			}
			return channel.createWebhook("Cross-Poster").submit().get();
		} catch (InterruptedException | InsufficientPermissionException | ExecutionException e) {
			return null;
		}
	}

	public static String getShowcaseMessage(Message message) {
		String messageContent = message.getContentRaw();
		for (int i = 0; i < messageContent.length(); i++) {
			if (Character.isWhitespace(messageContent.charAt(i))) {
				return messageContent.substring(i) + String.format(" (**Source:** [Jump](<%s>))", message.getJumpUrl());
			}
		}
		return String.format(" (**Source:** [Jump](<%s>))", message.getJumpUrl());
	}
}