package crossposter.commands;

import crossposter.ServerDataHandler;
import crossposter.ServerDataHandler.ChannelData;
import crossposter.ServerDataHandler.ServerData;
import disparser.Command;
import disparser.CommandContext;
import disparser.MessageUtil;
import disparser.arguments.jda.TextChannelArgument;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ConfigureCrosspostCommand extends Command {
	private final boolean disable;
	
	public ConfigureCrosspostCommand(boolean disable) {
		super(disable ? "disable_crosspost" : "enable_crosspost", TextChannelArgument.get());
		this.disable = disable;
	}

	@Override
	public void processCommand(CommandContext context) {
		GuildMessageReceivedEvent event = context.getEvent();
		TextChannel channel = event.getChannel();
		Message message = event.getMessage();
		Guild guild = message.getGuild();
		if (message.getMember() != null) {
			TextChannel crosspostChannel = context.getParsedResult(0);
			long crosspostChannelId = crosspostChannel.getIdLong();
			if (this.disable) {
				ServerData data = ServerDataHandler.getServerData(guild.getId());
				ChannelData foundChannel = null;
				boolean noData = data == null;
				if (!noData) {
					for (ChannelData channelData : data.channelData) {
						if (channelData.channelId == channel.getIdLong() && channelData.crosspostChannelId == crosspostChannelId) {
							foundChannel = channelData;
						}
					}
					noData = foundChannel == null;
				}
				
				if (noData) {
					this.sendMessage(channel, MessageUtil.createErrorMessage("This channel doesn't crosspost to that channel"));
				} else {
					ServerDataHandler.unwriteChannel(guild.getId(), foundChannel);
					this.sendMessage(channel, MessageUtil.createSuccessfulMessage("This channel now no longer crossposts to " + crosspostChannel.getAsMention()));
				}
			} else {
				if (crosspostChannelId == channel.getIdLong()) {
					this.sendMessage(channel, MessageUtil.createErrorMessage("Cannot make channel crosspost to itself"));
					return;
				}
					
				ServerData data = ServerDataHandler.getServerData(guild.getId());
				if (data != null) {
					for (ChannelData channelData : data.channelData) {
						if (channelData.channelId == crosspostChannelId) {
							this.sendMessage(channel, MessageUtil.createErrorMessage("Channel you want to crosspost to already crossposts to another channel"));
							return;
						}
					}
				}
					
				this.sendMessage(channel, MessageUtil.createSuccessfulMessage("This channel now crossposts images to " + crosspostChannel.getAsMention()));
				ServerDataHandler.writeChannel(guild.getId(), new ChannelData(channel.getIdLong(), crosspostChannelId), channel);
			}
		}
	}

}