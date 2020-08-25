package crossposter;

import crossposter.ServerDataHandler.ServerData;
import crossposter.commands.CrossPosterCommands;
import disparser.CommandHandler;
import disparser.InfoCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import java.util.Arrays;

/**
 * @author Luke Tonon
 */
@SuppressWarnings("unused")
public final class CrossPoster {
	private static JDA BOT;
	
	public static void main(String[] args) throws LoginException {
		ServerDataHandler.initialize();
		JDABuilder botBuilder = JDABuilder.create(args[0], GatewayIntent.getIntents(GatewayIntent.DEFAULT));
		botBuilder.setStatus(OnlineStatus.ONLINE);
		botBuilder.setActivity(Activity.of(ActivityType.DEFAULT, "Cross-posting Channels"));
		botBuilder.addEventListeners(new EventHandler());
		BOT = botBuilder.build();
	}

	/**
	 * Unused currently, but possibly could use in the future.
	 */
	public static JDA getBOT() {
		return BOT;
	}
	
	public static class EventHandler extends CommandHandler {
		private static final String[] IMAGE_LINKS = {
			"https://cdn.discordapp.com/avatars/727309426549063732/158e4ff08e5ebf32b6d8468f82356712.png?size=128",
			"https://images-ext-1.discordapp.net/external/MgtiHcVMIDdYiKJmLtfnooDxo9MaSauSDvOJNIoHc9A/https/cdn.discordapp.com/avatars/347143578096500736/a_33c085f65b80b06586dc680d4252409f.gif"
		};
		
		public EventHandler() {
			this.applyAnnotations(CrossPosterCommands.class);
			this.registerCommands(
				Arrays.asList(
					CrossPosterCommands.PREFIX,
					CrossPosterCommands.DISABLE,
					CrossPosterCommands.ENABLE,
					CrossPosterCommands.REQUIRE_ATTACHMENT,
					CrossPosterCommands.SHOW
				)
			);
		}
		
		@Override
		public void onGuildJoin(GuildJoinEvent event) {
			updateBotNickname(event.getGuild(), null);
		}
		
		@Override
		public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
			this.registerCommand("info", new InfoCommand(this.createMainInfoMessage(event.getGuild())).setCommandInfoMessages(CrossPosterCommands.COMMAND_INFOS));
			super.onGuildMessageReceived(event);
		}
		
		@Override
		public String getPrefix(Guild guild) {
			return getServerPrefix(guild.getId());
		}
		
		private MessageEmbed createMainInfoMessage(Guild guild) {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setTitle("Cross-Poster Information");
			builder.setThumbnail(IMAGE_LINKS[0]);
			builder.appendDescription("**Prefix:** " + "`" + this.getPrefix(guild) + "`");
			builder.appendDescription("\n" + "Cross-Poster is a bot that can crosspost messages to other channels in a configurable way");
			builder.addField("Commands", CrossPosterCommands.getCommandDescriptions(), true);
			builder.setColor(7506394);
			builder.setFooter("Created by Luke Tonon, @Smelly²#3450", IMAGE_LINKS[1]);
			return builder.build();
		}
		
		@Nullable
		private static ServerData getServerData(String serverId) {
			return ServerDataHandler.getServerData(serverId);
		}
		
		/**
		 * Gets the bot prefix for a server
		 * @param serverId - The guild/server's id {@link Guild#getId()}
		 * @return The bot prefix for a server
		 */
		public static String getServerPrefix(String serverId) {
			ServerData data = getServerData(serverId);
			return data != null && data.prefix != null ? data.prefix : "cp!";
		}
		
		public static void updateBotNickname(Guild guild, @Nullable String oldPrefix) {
			Member bot = guild.getMemberByTag("Cross-Poster", "5404");
			String nickname = bot.getNickname();
			String prefix = getServerPrefix(guild.getId());
			if (nickname == null || oldPrefix == null) {
				nickname = "Cross-Poster" + " [" + prefix + "]";
			} else {
				StringBuilder builder = new StringBuilder();
				String[] splitNickname = nickname.split(" ");
				int length = splitNickname.length;
				int charAfterOldPrefix = oldPrefix.length() + 1;
				boolean foundPrefix = false;
				for (int i = 0; i < length; i++) {
					String currentString = splitNickname[i];
					if (i == length - 1 && currentString.length() == charAfterOldPrefix + 1 && currentString.charAt(0) == '[' && currentString.charAt(charAfterOldPrefix) == ']') {
						splitNickname[i] = "[" + prefix + "]";
						foundPrefix = true;
					}
					builder.append(" " + splitNickname[i]);
				}
				
				if (!foundPrefix) {
					builder.append(String.format(" [%s]", prefix));
				}
				
				nickname = builder.toString();
			}
			guild.modifyNickname(bot, nickname).queue();
		}
	}
}