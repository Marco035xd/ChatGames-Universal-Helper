package dev.clares.chatgames.config;

import java.nio.file.*;
import java.io.IOException;

/** Write in the same directory so a failed write cannot truncate the previous file. */
public final class AtomicFiles {
    private AtomicFiles() { }
    public static void write(Path destination,String text) throws IOException {
        Files.createDirectories(destination.getParent());
        Path temporary=Files.createTempFile(destination.getParent(),".chatgames-",".tmp");
        try {
            Files.writeString(temporary,text);
            try {Files.move(temporary,destination,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
            catch(AtomicMoveNotSupportedException e){Files.move(temporary,destination,StandardCopyOption.REPLACE_EXISTING);}
        } finally {Files.deleteIfExists(temporary);}
    }
}
