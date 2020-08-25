package crossposter.commands;

import crossposter.ServerDataHandler;
import crossposter.ServerDataHandler.ChannelData;
import crossposter.ServerDataHandler.ServerData;
import disparser.Command;
import disparser.CommandContext;
import disparser.MessageUtil;
import disparser.arguments.primitive.BooleanArgument;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class RequireAttachmentCommand extends Command {

	public RequireAttachmentCommand() {
		super("require_attachment", BooleanArgument.get());
	}

	@Override
	public void processCommand(CommandContext context) {
		GuildMessageReceivedEvent event = context.getEvent();
		TextChannel channel = event.getChannel();
		Guild guild = event.getGuild();
		boolean requireAttachment = context.getParsedResult(0);
		ServerData serverData = ServerDataHandler.getServerData(guild.getId());
		if (serverData != null && !serverData.channelData.isEmpty()) {
			ChannelData channelData = ChannelData.getCrosspostChannel(serverData.channelData, channel.getIdLong());
			if (channelData != null) {
				this.sendMessage(channel, MessageUtil.createSuccessfulMessage("This channel's messages " + (requireAttachment ? "now require an attachment to crosspost" : "no longer require an attachment to crosspost")));
				ServerDataHandler.writeChannel(guild.getId(), new ChannelData(channelData.channelId, channelData.crosspostChannelId, requireAttachment));
				return;
			}
		}
		this.sendMessage(channel, MessageUtil.createErrorMessage("This channel doesn't crosspost"));
	}

}