package crossposter.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import disparser.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class CrossPosterCommands {
	private static final Map<String, String> COMMAND_DESCRIPTIONS = Collections.synchronizedMap(new HashMap<>());
	public static final Map<String, MessageEmbed> COMMAND_INFOS = Collections.synchronizedMap(new HashMap<>());
	
	public static final PrefixCommand PREFIX = createCommand(new PrefixCommand(), 
		"assigns a new command prefix to the bot",
		createInfo(
			"Prefix",
			"This command assigns a new command prefix for the bot. It takes in one argument, a set of characters",
			"`1st Argument (String)` - The prefix to assign"
		)
	);
	public static final ConfigureCrosspostCommand ENABLE = createCommand(new ConfigureCrosspostCommand(false),
		"enables crossposting to a channel",
		createInfo(
			"Enable Crossposting",
			"This command enables crossposting to another channel. It takes in one argument, a Text Channel ID.",
			"`1st Argument (Text Channel ID)` - The id of the text channel to make the current channel crosspost to"
		)
	);
	public static final ConfigureCrosspostCommand DISABLE = createCommand(new ConfigureCrosspostCommand(true),
		"disables crossposting to a channel",
		createInfo(
			"Disable Crossposting",
			"This command disables crossposting to another channel. It takes in one argument, a Text Channel ID.",
			"`1st Argument (Text Channel ID)` - The id of the text channel to make the current channel no longer crosspost to"
		)
	);
	public static final RequireAttachmentCommand REQUIRE_ATTACHMENT = createCommand(new RequireAttachmentCommand(),
		"sets whether a crossposting channel should need its messages to contain an attachment to crosspost (images, videos, etc)",
		createInfo(
			"Require Attachment",
			"This command sets whether a crossposting channel should need its messages to contain an attachment to crosspost (images, videos, etc). It takes in one argument, a boolean (true, false)",
			"`1st Argument (Boolean)` - If it should require an attachment"
		)
	);
	public static final ShowCommand SHOW = createCommand(new ShowCommand(),
		"crossposts a message if the current channel crossposts to a channel",
		createInfo(
			"Show",
			"This command crossposts a message to the current channel's crosspost channel. This command has no arguments."
		)
	);
	
	private static <C extends Command> C createCommand(C command, String description, MessageEmbed info) {
		String name = command.getName();
		COMMAND_DESCRIPTIONS.put(name, description);
		COMMAND_INFOS.put(name, info);
		return command;
	}
	
	private static MessageEmbed createInfo(String title, String description, String... args) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle(title + " Command Info");
		builder.setDescription(description);
		builder.addField("Arguments", args.length == 0 ? "None" : formatArgs(args), true);
		builder.setColor(7506394);
		return builder.build();
	}
	
	private static String formatArgs(String... args) {
		StringBuilder builder = new StringBuilder();
		for (String arg : args) {
			builder.append(arg + "\n" + "\n");
		}
		return builder.toString();
	}
	
	public static String getCommandDescriptions() {
		StringBuilder builder = new StringBuilder();
		COMMAND_DESCRIPTIONS.forEach((name, desc) -> builder.append("`" + name + "`" + " - " + desc + "\n" + "\n"));
		builder.append("`info` - " + "shows info for the bot or a command");
		return builder.toString();
	}
}