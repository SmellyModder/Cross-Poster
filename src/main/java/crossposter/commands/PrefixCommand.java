package crossposter.commands;

import crossposter.ServerDataHandler;
import crossposter.CrossPoster.EventHandler;
import disparser.Command;
import disparser.CommandContext;
import disparser.MessageUtil;
import disparser.arguments.primitive.StringArgument;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PrefixCommand extends Command {

	public PrefixCommand() {
		super("prefix", StringArgument.get());
	}

	@Override
	public void processCommand(CommandContext context) {
		GuildMessageReceivedEvent event = context.getEvent();
		Message message = event.getMessage();
		TextChannel channel = event.getChannel();
		if (this.testForAdmin(message)) {
			String prefix = context.getParsedResult(0);
			Guild guild = event.getGuild();
			ServerDataHandler.writePrefix(guild.getId(), prefix);
			EventHandler.updateBotNickname(guild);
			this.sendMessage(channel, MessageUtil.createSuccessfulMessage("Set new command prefix to " + "`" + prefix + '`'));
		}
	}

}