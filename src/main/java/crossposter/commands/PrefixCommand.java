package crossposter.commands;

import crossposter.CrossPoster;
import crossposter.CrossPoster.EventHandler;
import crossposter.ServerDataHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.smelly.disparser.Command;
import net.smelly.disparser.CommandContext;
import net.smelly.disparser.arguments.java.StringArgument;

public class PrefixCommand extends Command {

	public PrefixCommand() {
		super("prefix", StringArgument.get());
	}

	@Override
	public void processCommand(CommandContext context) {
		GuildMessageReceivedEvent event = context.getEvent();
		Guild guild = event.getGuild();
		String guildId = guild.getId();
		String oldPrefix = CrossPoster.EventHandler.getServerPrefix(guildId);
		String prefix = context.getParsedResult(0);
		ServerDataHandler.writePrefix(guildId, prefix);
		EventHandler.updateBotNickname(guild, oldPrefix);
		context.getFeedbackHandler().sendSuccess("Set new command prefix to " + "`" + prefix + '`');
	}

}