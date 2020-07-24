package disparser;

import javax.annotation.Nullable;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * Used to read {@link Argument}s from a message
 * <p> Stores the channel of the message and stores the message down into its individual components </p>
 * @author Luke Tonon
 */
public final class ArgumentReader {
	private final TextChannel channel;
	private final String[] messageComponents;
	private int currentComponent;
	
	private ArgumentReader(TextChannel channel, String[] messageComponents) {
		this.channel = channel;
		this.messageComponents = messageComponents;
	}
	
	/**
	 * Creates an ArgumentReader for a message
	 * @param message - {@link Message} for this ArgumentReader
	 * @return {@link ArgumentReader} for the message
	 */
	public static ArgumentReader create(final Message message) {
		return new ArgumentReader(message.getTextChannel(), message.getContentRaw().split(" "));
	}
	
	public TextChannel getChannel() {
		return this.channel;
	}
	
	public String[] getMessageComponents() {
		return this.messageComponents;
	}
	
	public int getCurrentComponent() {
		return this.currentComponent;
	}
	
	@Nullable
	public Integer nextInt() {
		try {
			return Integer.parseInt(this.nextArgument());
		} catch (NumberFormatException exception) {
			return null;
		}
	}
	
	@Nullable
	public Long nextLong() {
		try {
			return Long.parseLong(this.nextArgument());
		} catch (NumberFormatException exception) {
			return null;
		}
	}
	
	@Nullable
	public Character nextChar() {
		String nextArg = this.nextArgument();
		return nextArg.length() > 1 ? null : nextArg.charAt(0);
	}
	
	@Nullable
	public Short nextShort() {
		try {
			return Short.parseShort(this.nextArgument());
		} catch (NumberFormatException exception) {
			return null;
		}
	}
	
	@Nullable
	public Byte nextByte() {
		try {
			return Byte.parseByte(this.nextArgument());
		} catch (NumberFormatException exception) {
			return null;
		}
	}
	
	@Nullable
	public Float nextFloat() {
		try {
			return Float.parseFloat(this.nextArgument());
		} catch (NumberFormatException exception) {
			return null;
		}
	}
	
	@Nullable
	public Double nextDouble() {
		try {
			return Double.parseDouble(this.nextArgument());
		} catch (NumberFormatException exception) {
			return null;
		}
	}
	
	public boolean nextBoolean() {
		return Boolean.parseBoolean(this.nextArgument());
	}
	
	/**
	 * Used to convert strings to non-primitive type arguments.
	 * @param reader - {@link Parser} for the object
	 * @return The object({@link A}) read from the reader
	 */
	public <A> ParsedArgument<A> parseNextArgument(final Parser<A> parser) {
		return parser.parse(this.nextArgument());
	}

	public <A> ParsedArgument<A> tryToParseArgument(Argument<A> argument) {
		ParsedArgument<A> parsedArgument = this.hasNextArg() ? argument.parse(new ArgumentReader(this.channel, new String[] {"", this.messageComponents[this.currentComponent + 1]})) : ParsedArgument.parseWithError(null, "Missing argument");
		if (parsedArgument.getErrorMessage() == null) {
			this.nextArgument();
		}
		return parsedArgument;
	}
	
	/**
	 * Gets the next argument in the message's components.
	 * <p> Only call this once in {@link Argument#parse()} </p>
	 * @return The next argument.
	 */
	public String nextArgument() {
		this.currentComponent++;
		if (this.currentComponent > this.messageComponents.length - 1) {
			throw new IndexOutOfBoundsException(this.currentComponent + MessageUtil.getOrdinalForInteger(this.currentComponent) + " component doesn't exist!");
		}
		return this.messageComponents[this.currentComponent];
	}
	
	/**
	 * If the reader has a next argument in its components
	 * @return If the reader has a next argument in its components
	 */
	public boolean hasNextArg() {
		return this.currentComponent + 1 <= this.messageComponents.length - 1;
	}
	
	@FunctionalInterface
	public interface Parser<A> {
		public ParsedArgument<A> parse(String string);
	}
}