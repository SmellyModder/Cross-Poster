package disparser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import disparser.arguments.primitive.StringArgument;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

public class InfoCommand extends Command {
	private final MessageEmbed mainInfoMessage;
	private Map<String, MessageEmbed> commandInfoMessages;
	
	public InfoCommand(MessageEmbed mainInfoMessage) {
		super("info", StringArgument.get().asOptional());
		this.mainInfoMessage = mainInfoMessage;
	}
	
	public InfoCommand setCommandInfoMessages(Map<String, MessageEmbed> commandInfoMessages) {
		this.commandInfoMessages = commandInfoMessages;
		return this;
	}
	
	public InfoCommand putCommandInfo(String commandName, MessageEmbed commandInfo) {
		if (this.commandInfoMessages == null) this.commandInfoMessages = Collections.synchronizedMap(new HashMap<String, MessageEmbed>());
		this.commandInfoMessages.put(commandName, commandInfo);
		return this;
	}

	@Override
	public void processCommand(CommandContext context) {
		TextChannel channel = context.getEvent().getChannel();
		if (this.commandInfoMessages != null) {
			String commandName = context.getParsedResult(0);
			if (commandName != null) {
				MessageEmbed commandDescription = this.commandInfoMessages.get(commandName);
				if (commandDescription != null) {
					channel.sendMessage(commandDescription).queue();
					return;
				}
			}
		}
		channel.sendMessage(this.mainInfoMessage).queue();
	}
}