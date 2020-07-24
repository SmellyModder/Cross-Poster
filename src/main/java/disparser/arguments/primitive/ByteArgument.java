package disparser.arguments.primitive;

import disparser.Argument;
import disparser.ArgumentReader;
import disparser.ParsedArgument;

public class ByteArgument implements Argument<Byte> {

	private ByteArgument() {}
	
	public static ByteArgument get() {
		return new ByteArgument();
	}
	
	@Override
	public ParsedArgument<Byte> parse(ArgumentReader reader) {
		Byte nextByte = reader.nextByte();
		return nextByte != null ? ParsedArgument.parse(nextByte) : ParsedArgument.parseWithError(nextByte, "`" + reader.getMessageComponents()[reader.getCurrentComponent()] + "`" + " is not a valid byte");
	}

}
