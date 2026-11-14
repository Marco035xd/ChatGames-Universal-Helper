package dev.clares.chatgames.game;

import com.google.gson.reflect.TypeToken;
import dev.clares.chatgames.config.ConfigManager;
import dev.clares.chatgames.solver.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ServerLog121Test {
    @TempDir Path temp;
    record Round(String type,String question,String answer,String prompt,String style,
                 String sourceFile,int promptLine,int resultLine,String resultKind) { }

    private List<Round> rounds() throws Exception {
        try(var reader=new InputStreamReader(getClass().getResourceAsStream("/server-rounds-121.json"),StandardCharsets.UTF_8)) {
            return ConfigManager.GSON.fromJson(reader,new TypeToken<List<Round>>(){}.getType());
        }
    }

    @Test void octoberCorpusIsDetectedAndSolvedWithTheExactServerAnswers() throws Exception {
        ConfigManager config=new ConfigManager(temp);
        assertCorpus(new WordSolver(config.dir()),new TriviaSolver(config.dir()));
    }

    @Test void existingInstallationsReceiveNewAnswersWithoutReplacingPersonalData() throws Exception {
        Path dir=temp.resolve("config/chatgames-universal-helper");Files.createDirectories(dir);
        Files.writeString(dir.resolve("config.json"),"{\"configRevision\":9,\"enabled\":false,\"stats\":true,\"hud\":false,\"games\":{\"trivia\":false},\"delay\":{\"mode\":\"fixed\",\"minMs\":4200,\"maxMs\":4200},\"gameDelays\":{\"math\":{\"mode\":\"fixed\",\"minMs\":1700,\"maxMs\":1700}}}");
        Files.writeString(dir.resolve("words-es.txt"),"Personal\n");
        Files.writeString(dir.resolve("words-en.txt"),"personal\n");
        Files.writeString(dir.resolve("word-cache.json"),"{\"u:ianpga\":\"Página\"}");
        Files.writeString(dir.resolve("trivia.json"),"{\"Pregunta personal\":\"Personal\"}");
        Files.writeString(dir.resolve("trivia-cache.json"),"{\"Pregunta aprendida\":\"Aprendida\"}");
        String wordCache=Files.readString(dir.resolve("word-cache.json"));
        ConfigManager config=new ConfigManager(temp);
        WordSolver words=new WordSolver(config.dir());TriviaSolver trivia=new TriviaSolver(config.dir());
        assertCorpus(words,trivia);
        assertEquals(wordCache,Files.readString(dir.resolve("word-cache.json")));
        assertEquals("Página",words.unscramble("ianpga").orElseThrow());
        assertEquals("Personal",trivia.answer("Pregunta personal").orElseThrow());
        assertEquals("Aprendida",trivia.answer("Pregunta aprendida").orElseThrow());
        assertFalse(config.get().enabled);assertFalse(config.get().game("trivia"));
        assertTrue(config.get().stats);assertFalse(config.get().hud);
        assertEquals(4200,config.get().delayFor("trivia").sampleMillis());
        assertEquals(1700,config.get().delayFor("math").sampleMillis());
        // Learning after the upgrade must still take precedence over bundled answers.
        assertTrue(words.rememberUnscramble("sMae","MESA"));
        trivia.put("¿Cuál es el océano más grande del mundo?","Respuesta personal");
        assertEquals("MESA",new WordSolver(config.dir()).unscramble("sMae").orElseThrow());
        assertEquals("Respuesta personal",new TriviaSolver(config.dir()).answer("¿Cuál es el océano más grande del mundo?").orElseThrow());
    }

    @Test void newServerVocabularyPreservesAccentsForUnseenAnagramPermutations() throws Exception {
        ConfigManager config=new ConfigManager(temp);
        Files.writeString(config.dir().resolve("words-es.txt"),"Personal\n");
        Files.writeString(config.dir().resolve("words-en.txt"),"personal\n");
        Files.writeString(config.dir().resolve("word-cache.json"),"{}");
        WordSolver words=new WordSolver(config.dir());
        assertEquals("Vórtice",words.unscramble("ecitróV").orElseThrow());
        assertEquals("Atmósfera",words.unscramble("raefsmótA").orElseThrow());
        assertEquals("Jabón",words.unscramble("ónJab").orElseThrow());
    }

    private void assertCorpus(WordSolver words,TriviaSolver trivia) throws Exception {
        List<Round> corpus=rounds();assertEquals(328,corpus.size());
        Map<String,Integer> counts=new HashMap<>();
        for(Round round:corpus) {
            counts.merge(round.type,1,Integer::sum);
            String source=round.sourceFile+":"+round.promptLine+" / "+round.question;
            assertTrue(round.resultLine>round.promptLine,source);
            var detected=GameDetector.detect(round.prompt,true).orElseThrow(()->new AssertionError(source));
            assertEquals(round.type.toUpperCase(Locale.ROOT),detected.type().name(),source);
            assertEquals(round.question,detected.question(),source);
            String answer=switch(round.type) {
                case "trivia" -> trivia.answer(round.question).orElseThrow();
                case "fillout" -> words.fillout(round.question,round.style).orElseThrow();
                case "unscramble" -> words.unscramble(round.question).orElseThrow();
                case "math" -> MathParser.solve(round.question);
                case "reaction" -> detected.question();
                default -> throw new AssertionError(source);
            };
            assertEquals(round.answer,answer,source);
        }
        assertEquals(Map.of("trivia",73,"fillout",61,"unscramble",54,"math",74,"reaction",66),counts);
    }
}
