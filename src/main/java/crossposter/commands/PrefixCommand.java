package crossposter.commands;

import crossposter.CrossPoster;
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
			Guild guild = event.getGuild();
			String guildId = guild.getId();
			String oldPrefix = CrossPoster.EventHandler.getServerPrefix(guildId);
			String prefix = context.getParsedResult(0);
			ServerDataHandler.writePrefix(guildId, prefix);
			EventHandler.updateBotNickname(guild, oldPrefix);
			this.sendMessage(channel, MessageUtil.createSuccessfulMessage("Set new command prefix to " + "`" + prefix + '`'));
		}
	}

}