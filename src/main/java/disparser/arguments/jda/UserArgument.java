package disparser.arguments.jda;

import disparser.Argument;
import disparser.ArgumentReader;
import disparser.ParsedArgument;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An argument that can parse users by their ID or a mention of the user.
 * Define a JDA to get the User from or leave null to use the JDA of the message that was sent.
 * 
 * @author Luke Tonon
 */
public final class UserArgument implements Argument<User> {
	private static final Pattern MENTION_PATTERN = Pattern.compile("^<@!?(\\d+)>$");
	
	@Nullable
	private final JDA jda;
	
	private UserArgument(JDA jda) {
		this.jda = jda;
	}
	
	/**
	 * @return A default instance.
	 */
	public static UserArgument get() {
		return new UserArgument(null);
	}
	
	/**
	 * If you only want to get users of the guild that the message was sent from then use {@link #get()}.
	 * @param jda - JDA to get the user from.
	 * @return An instance of this argument with a JDA.
	 */
	public static UserArgument create(JDA jda) {
		return new UserArgument(jda);
	}
	
	@Override
	public ParsedArgument<User> parse(ArgumentReader reader) {
		return reader.parseNextArgument((arg) -> {
			try {
				long id = Long.parseLong(arg);
				User foundUser = this.findUserWithId(reader, id);
				if (foundUser != null) {
					return ParsedArgument.parse(foundUser);
				} else {
					return ParsedArgument.parseError("Member with id `%d` could not be found", id);
				}
			} catch (NumberFormatException exception) {
				Matcher matcher = MENTION_PATTERN.matcher(arg);
				
				if (matcher.matches()) {
					User foundUser = this.findUserWithId(reader, Long.parseLong(matcher.group(1)));
					if (foundUser != null) {
						return ParsedArgument.parse(foundUser);
					} else {
						return ParsedArgument.parseError("Member in mention could not be found");
					}
				}
				
				return ParsedArgument.parseError("`%s` is not a valid member id or valid user mention", arg);
			}
		});
	}
	
	@Nullable
	private User findUserWithId(ArgumentReader reader, long id) {
		return this.jda != null ? this.jda.getUserById(id) : reader.getChannel().getJDA().getUserById(id);
	}
}