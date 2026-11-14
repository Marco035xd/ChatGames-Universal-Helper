package dev.clares.chatgames.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.List;

/** Unlike Brigadier's unquoted word parser, durations also allow a decimal comma. */
final class DurationArgument implements ArgumentType<String> {
    private static final SimpleCommandExceptionType INVALID=new SimpleCommandExceptionType(
        new LiteralMessage("Escribe entre 0 y 60 segundos, por ejemplo 2.1, 2,9 o 500ms."));
    @Override public String parse(StringReader reader) throws CommandSyntaxException {
        int start=reader.getCursor();
        while(reader.canRead() && !Character.isWhitespace(reader.peek()))reader.skip();
        String value=reader.getString().substring(start,reader.getCursor());
        try {TimeCommand.parseDuration(value);return value;}
        catch(IllegalArgumentException e){reader.setCursor(start);throw INVALID.createWithContext(reader);}
    }
    @Override public Collection<String> getExamples(){return List.of("2.1","2,9","2.1s","500ms");}
}
