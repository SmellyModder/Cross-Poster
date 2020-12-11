package crossposter.commands;

import crossposter.ServerDataHandler;
import crossposter.ServerDataHandler.ChannelData;
import crossposter.ServerDataHandler.ServerData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.smelly.disparser.Command;
import net.smelly.disparser.CommandContext;
import net.smelly.disparser.arguments.java.BooleanArgument;
import net.smelly.disparser.feedback.exceptions.SimpleCommandExceptionCreator;

public class RequireAttachmentCommand extends Command {
	private static final SimpleCommandExceptionCreator CHANNEL_CROSSPOST_EXCEPTION = new SimpleCommandExceptionCreator("This channel doesn't crosspost");

	public RequireAttachmentCommand() {
		super("require_attachment", BooleanArgument.get());
	}

	@Override
	public void processCommand(CommandContext context) throws Exception {
		GuildMessageReceivedEvent event = context.getEvent();
		TextChannel channel = event.getChannel();
		Guild guild = event.getGuild();
		boolean requireAttachment = context.getParsedResult(0);
		ServerData serverData = ServerDataHandler.getServerData(guild.getId());
		if (serverData != null && !serverData.channelData.isEmpty()) {
			ChannelData channelData = ChannelData.getCrosspostChannel(serverData.channelData, channel.getIdLong());
			if (channelData != null) {
				context.getFeedbackHandler().sendSuccess("This channel's messages " + (requireAttachment ? "now require an attachment to crosspost" : "no longer require an attachment to crosspost"));
				ServerDataHandler.writeChannel(guild.getId(), new ChannelData(channelData.channelId, channelData.crosspostChannelId, requireAttachment));
				return;
			}
		}
		throw CHANNEL_CROSSPOST_EXCEPTION.create();
	}
}