package disparser.arguments.primitive;

import disparser.Argument;
import disparser.ArgumentReader;
import disparser.ParsedArgument;

public class LongArgument implements Argument<Long> {

	private LongArgument() {}
	
	public static LongArgument get() {
		return new LongArgument();
	}
	
	@Override
	public ParsedArgument<Long> parse(ArgumentReader reader) {
		Long nextLong = reader.nextLong();
		return nextLong != null ? ParsedArgument.parse(nextLong) : ParsedArgument.parseWithError(nextLong, "`" + reader.getMessageComponents()[reader.getCurrentComponent()] + "`" + " is not a valid long");
	}

}