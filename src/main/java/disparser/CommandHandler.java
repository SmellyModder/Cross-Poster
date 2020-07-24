package disparser;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandHandler extends ListenerAdapter {
	private final Map<String, Command> COMMANDS = Collections.synchronizedMap(new HashMap<String, Command>());
	private String prefix = "!";
	
	public CommandHandler() {}
	
	public CommandHandler(String prefix) {
		this.prefix = prefix;
	}
	
	public CommandHandler(String prefix, List<Registrable<Command>> commands) {
		this(prefix);
		this.registerCommands(commands);
	}
	
	public CommandHandler(String prefix, Command... commands) {
		this(prefix, Arrays.asList(commands));
	}
	
	protected void registerCommands(List<Registrable<Command>> commands) {
		synchronized (COMMANDS) {
			commands.forEach(command -> {
				COMMANDS.put(command.getName(), command.get());
			});
		}
	}
	
	protected void registerCommand(String commandName, Command command) {
		synchronized (COMMANDS) {
			COMMANDS.put(commandName, command);
		}
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		ArgumentReader reader = ArgumentReader.create(event.getMessage());
		String firstComponent = reader.getMessageComponents()[0];
		String prefix = this.getPrefix(event.getGuild());
		if (firstComponent.startsWith(prefix)) {
			synchronized (COMMANDS) {
				Command command = COMMANDS.get(firstComponent.substring(prefix.length()).toLowerCase());
				if (command != null) CommandContext.createContext(event, command, reader).ifPresent(command::processCommand);
			}
		}
	}
	
	public String getPrefix(Guild guild) {
		return this.prefix;
	}
}