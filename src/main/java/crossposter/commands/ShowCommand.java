package crossposter.commands;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import crossposter.ServerDataHandler;
import crossposter.ServerDataHandler.ChannelData;
import crossposter.ServerDataHandler.ServerData;
import disparser.Command;
import disparser.CommandContext;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class ShowCommand extends Command {

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
		if (!message.mentionsEveryone()) {
			ServerData serverData = ServerDataHandler.getServerData(guildId);
			if (serverData != null) {
				ChannelData channelData = ChannelData.getCrosspostChannel(serverData.channelData, channel.getIdLong());
				if (channelData == null) return;
				long crosspostChannelId = channelData.crosspostChannelId;
				TextChannel crosspostChannel = guild.getTextChannelById(crosspostChannelId);
				if (crosspostChannel != null) {
					Webhook webhook = this.getWebhookForGuild(channel, serverData);
					try {
						webhook.getManager().setChannel(crosspostChannel).queue();
					} catch (InsufficientPermissionException e) {
						return;
					}
						
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
					messageBuilder.setContent(this.getMessageWithoutShowPrefix(message));

					List<Attachment> messageAttachments = message.getAttachments();
					if (channelData.requiresAttachment && messageAttachments.isEmpty()) {
						return;
					}
						
					for (Attachment attachment : messageAttachments) {
						try {
							messageBuilder.addFile(attachment.downloadToFile().get());
						} catch (InterruptedException | ExecutionException e) {}
					}
						
					builder.build().send(messageBuilder.build());
				}
			}
		}
	}
	
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
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private String getMessageWithoutShowPrefix(Message message) {
		String messageContent = message.getContentRaw();
		StringBuilder builder = new StringBuilder();
		String[] split = messageContent.split(" ");
		split[0] = "";
		for (String component : split) {
			builder.append(" " + component);
		}
		return builder.toString();
	}

}