package disparser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.dv8tion.jda.internal.utils.tuple.Pair;

public final class ParsedArgument<A> {
	@Nullable
	private final A result;
	@Nullable
	private final String errorMessage;
	
	private ParsedArgument(@Nullable final A readArgument, @Nullable final String errorMessage) {
		this.result = readArgument;
		this.errorMessage = errorMessage;
	}
	
	public A getResult() {
		return this.result;
	}
	
	public String getErrorMessage() {
		return this.errorMessage;
	}
	
	public A getOrOtherResult(@Nonnull A other) {
		return this.result == null ? other : (A) this.result;
	}
	
	public static <A> ParsedArgument<A> parse(@Nonnull final A result) {
		return new ParsedArgument<>(result, null);
	}
	
	public static <A> ParsedArgument<A> parseWithError(final A result, final String errorMessage) {
		return new ParsedArgument<>(result, errorMessage);
	}
	
	public static <A> ParsedArgument<A> parseWithError(@Nonnull Pair<A, String> resultWithMessage) {
		return new ParsedArgument<>(resultWithMessage.getLeft(), resultWithMessage.getRight());
	}
}