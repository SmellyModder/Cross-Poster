package crossposter.commands;

import crossposter.ServerDataHandler;
import crossposter.ServerDataHandler.ChannelData;
import crossposter.ServerDataHandler.ServerData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.smelly.disparser.Command;
import net.smelly.disparser.CommandContext;
import net.smelly.disparser.arguments.jda.TextChannelArgument;
import net.smelly.disparser.feedback.exceptions.DynamicCommandExceptionCreator;
import net.smelly.disparser.feedback.exceptions.SimpleCommandExceptionCreator;

public class ConfigureCrosspostCommand extends Command {
	private static final DynamicCommandExceptionCreator<TextChannel> DOES_NOT_CROSSPOST_TO_EXCEPTION = DynamicCommandExceptionCreator.createInstance(channel -> {
		return "This channel doesn't crosspost to " + channel.getAsMention();
	});
	private static final DynamicCommandExceptionCreator<TextChannel> ALREADY_CROSSPOSTS_TO_EXCEPTION = DynamicCommandExceptionCreator.createInstance(channel -> {
		if (channel != null) {
			return channel.getAsMention() + " crossposts to another channel";
		}
		return "Channel you want to crosspost to crossposts to another channel";
	});
	private static final SimpleCommandExceptionCreator CANNOT_CROSSPOST_TO_SELF_EXCEPTION = new SimpleCommandExceptionCreator("Cannot make this channel crosspost to itself!");
	private final boolean disable;
	
	public ConfigureCrosspostCommand(boolean disable) {
		super(disable ? "disable_crosspost" : "enable_crosspost", TextChannelArgument.get());
		this.disable = disable;
	}

	@Override
	public void processCommand(CommandContext context) throws Exception {
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
					throw DOES_NOT_CROSSPOST_TO_EXCEPTION.create(crosspostChannel);
				} else {
					ServerDataHandler.unwriteChannel(guild.getId(), foundChannel);
					context.getFeedbackHandler().sendSuccess("This channel now no longer crossposts to " + crosspostChannel.getAsMention());
				}
			} else {
				if (crosspostChannelId == channel.getIdLong()) {
					throw CANNOT_CROSSPOST_TO_SELF_EXCEPTION.create();
				}
					
				ServerData data = ServerDataHandler.getServerData(guild.getId());
				if (data != null) {
					for (ChannelData channelData : data.channelData) {
						if (channelData.channelId == crosspostChannelId) {
							throw ALREADY_CROSSPOSTS_TO_EXCEPTION.create(guild.getTextChannelById(channelData.channelId));
						}
					}
				}

				context.getFeedbackHandler().sendSuccess("This channel now crossposts images to " + crosspostChannel.getAsMention());
				ServerDataHandler.writeChannel(guild.getId(), new ChannelData(channel.getIdLong(), crosspostChannelId), channel);
			}
		}
	}

}