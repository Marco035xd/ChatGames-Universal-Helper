package dev.clares.chatgames.game;
import dev.clares.chatgames.solver.VariableSolver;
import dev.clares.chatgames.solver.SymbolVariableSolver;
import dev.clares.chatgames.solver.MathParser;
import java.text.Normalizer;
import java.util.Optional;
import java.util.Locale;
import java.util.regex.*;

public final class GameDetector {
    public record Detection(GameType type, String question, DetectionConfidence confidence) { }
    private static final Pattern MATH = Pattern.compile("(?<![\\p{L}\\d])([(+\\-]?\\s*\\d+(?:\\.\\d+)?(?:\\s*[+*/%^×÷−–—xX·✕-]\\s*\\(?\\s*-?\\d+(?:\\.\\d+)?\\s*\\)?)+)(?![\\p{L}\\d])");
    private static final Pattern MATH_PROMPT=Pattern.compile("(?iu)^resuelve este problema\\s*[:：]?\\s*(.*)$");
    private static final Pattern MATH_TEXT=Pattern.compile("[\\d.\\s()+*/%^×÷−–—xX·✕-]+");
    private static final Pattern NEW_RESULT=Pattern.compile("(?iu)^¡?[a-z0-9_]{1,16}\\s+respondi[oó]\\s+(.{1,80}?)\\s+en\\s+\\d+(?:[.,]\\d+)?\\s*s\\.[! ]*$");
    private static final Pattern LEVEL=Pattern.compile("(?iu)^\\((f[aá]cil|normal|experto)\\)$");
    private static final Pattern VARIABLE = Pattern.compile("(?i)(?<![\\p{L}\\d])([\\d.xX+*×·✕−–—\\-\\s]+\\s*=\\s*[\\d.xX+*×·✕−–—\\-\\s]+)(?![\\p{L}\\d])");
    private static final Pattern RANGE = Pattern.compile("(?i)(?:entre|between|from)\\s+(\\d+)\\s+(?:y|and|to|a)\\s+(\\d+)");
    private static final Pattern TICK = Pattern.compile("[`'\"]([^`'\"]{1,80})[`'\"]");
    private static final Pattern MASK = Pattern.compile("(?U)[\\p{L}]*_+[\\p{L}_]*|[\\p{L}]+\\?+[\\p{L}]+");
    private static final Pattern WORD = Pattern.compile("(?iu)\\b(?:escribe|write|type|copiar|copy|descifra|unscramble|unreverse|invierte|reverse|reversa|completa|complete|fill\\s*out)\\b\\s*(?:la\\s+palabra|the\\s+word|esto|it|this|el\\s+texto)?\\s*[:：]?\\s*([\\p{L}\\d_?-]{2,80})");
    private static final Pattern QUESTION = Pattern.compile("¿[^\\r\\n?]+\\?(?:\\s*\\([^\\r\\n)]{1,40}\\))?");
    private static final Pattern QUOTED_ANSWER = Pattern.compile("[`‘]([^`’]{1,80})[`’]");
    private static final Pattern ANNOUNCED_ANSWER = Pattern.compile("(?iu)(?:la respuesta correcta (?:era|es)|respuesta correcta:|the correct answer was|respondi[oó] correctamente:)[ \\t]+(.{1,80}?)[!.]?[ \\t]*$");
    private static final Pattern SOLVED_ANSWER = Pattern.compile("(?iu)has (?:correctly |successfully )?(?:unscrambled|unreversed|completed|solved)[ \\t]+(.{1,80}?)[ \\t]+\\(\\d+(?:[.,]\\d+)?s\\)");
    private static final Pattern ACTION = Pattern.compile("(?iu)\\b(mata|caza|hunt|kill|mina|mine|rompe|break|coloca|place|pesca|fish|come|eat|craft|fabrica|hornea|smelt|furnace|cocina)\\b\\s+(.+)");
    private static String normalized(String s) { return Normalizer.normalize(s,Normalizer.Form.NFD).replaceAll("\\p{M}","").toLowerCase(Locale.ROOT); }
    public static boolean banner(String s) { return normalized(s).matches("(?s).*(?:minijuego|mini.?game|chat.?game|desafio|challenge).*" ) || normalized(s).matches("(?s).*\\b(?:tienes|you have)\\s+\\d+\\s+(?:segundos|seconds).*" ); }
    public static boolean winner(String s) {
        if(playerChat(s)||s.stripLeading().startsWith("[CGH]"))return false;
        for(String line:s.split("\\R"))if(NEW_RESULT.matcher(line.strip()).matches()||normalized(line).strip().matches("recibio los premios de (?:facil|normal|experto)\\."))return true;
        return normalized(s).matches("(?s).*(?:respondio primero|fue recompensado|answered first|winner|tiempo agotado|time is up|time['’]s up|nadie respondio|no one answered|has won|ganador|(?:was|is) (?:the )?(?:first|fastest) (?:player )?to (?:answer|type|write|get|fill|complete|unscramble|unreverse|solve)|answered .{0,100}correctly|successfully (?:unscrambled|unreversed|solved)|has (?:correctly |successfully )?(?:unscrambled|unreversed|completed|solved)|fue (?:el )?(?:primero|mas rapido) en (?:responder|escribir|poner en orden|ordenar|completar|obtener)|respondio correctamente:|(?:el )?(?:juego|evento)(?: de [^!\\n]{1,50})? (?:ha|se ha) terminado|la respuesta correcta (?:era|es)|respuesta correcta:|the correct answer was|(?:game|event) (?:is |has |was )?(?:now )?(?:over|ended|expired|cancelled|canceled)).*");
    }
    /** Command confirmations are not playable prompts, even when they name a game. */
    public static boolean administrative(String s) {
        return normalized(s).matches("(?s).*(?:you have (?:started|stopped|cancelled|canceled) (?:an? |the )?.{0,60}chat (?:event|game)|has iniciado (?:el )?(?:evento|minijuego)).*");
    }
    /** Only result messages may teach an answer; ordinary player chat cannot. */
    public static Optional<String> announcedAnswer(String line) {
        if (!winner(line)) return Optional.empty();
        for(String part:line.split("\\R")) {
            Matcher result=NEW_RESULT.matcher(part.strip());
            if(result.matches())return Optional.of(result.group(1).strip());
        }
        for (Pattern pattern:new Pattern[]{QUOTED_ANSWER,ANNOUNCED_ANSWER,SOLVED_ANSWER}) {
            Matcher matcher=pattern.matcher(line);
            if (matcher.find()) return Optional.of(matcher.group(1).strip());
        }
        return Optional.empty();
    }
    public static String difficulty(String block) {
        for(String line:block.split("\\R")) {
            Matcher level=LEVEL.matcher(line.strip());
            if(level.matches())return switch(normalized(level.group(1))){case "facil"->"Fácil";case "normal"->"Normal";default->"Experto";};
        }
        return "";
    }
    private static Optional<Detection> completeMath(String expression) {
        String candidate=expression.replaceAll("(?iu)[,;]?\\s*(?:tienes|you have)\\s+\\d+\\s+(?:segundos|seconds).*$", "").strip()
            .replaceAll("^[`\"‘]|[`\"’]$", "").strip();
        if(!MATH_TEXT.matcher(candidate).matches())return Optional.empty();
        try{MathParser.solve(candidate);return Optional.of(new Detection(GameType.MATH,candidate,DetectionConfidence.HIGH));}
        catch(IllegalArgumentException|ArithmeticException e){return Optional.empty();}
    }
    private static boolean unscrambleInstruction(String normalized) {
        return normalized.matches("(?s).*(?:descifra|unscramble|anagrama|poner en orden|ordena(?:r)? (?:correctamente )?(?:esta |la )?palabra).*" );
    }
    private static boolean unreverseInstruction(String normalized) {
        return normalized.matches("(?s).*(?:invierte|unreverse|reverse|reversa|al reves|palabra invertida).*" );
    }
    public static boolean capitalizationHint(String s) {
        return normalized(s).contains("[chat games] almost... check your capitalization!");
    }
    public static boolean hintHigher(String s) { return normalized(s).matches("(?s).*(?:mas alto|higher|mayor|too low).*" ); }
    public static boolean hintLower(String s) { return normalized(s).matches("(?s).*(?:mas bajo|lower|menor|too high).*" ); }
    /** These server formats use different word lists, including ambiguous masks. */
    public static String wordContext(String block) {
        String text=normalized(block);
        if (text.matches("(?s).*you have\\s+\\d+\\s+seconds.*")) return "en20";
        if (text.contains("60 segundos")) return "es60";
        if (text.contains("20 segundos")) return "es20";
        return "";
    }
    private static Optional<String> explicitTrivia(String[] lines) {
        for(int i=0;i<lines.length;i++) {
            String instruction=normalized(lines[i]);
            if (!instruction.matches("(?s).*(?:seconds to answer|segundos para responder|responde correctamente:).*")) continue;
            StringBuilder question=new StringBuilder();
            boolean quoted=false;
            for(int j=i+1;j<lines.length;j++) {
                String part=lines[j].strip();
                if (part.isEmpty()) {if(!question.isEmpty())break;else continue;}
                if(question.isEmpty()) {
                    if (!part.matches("(?iu)^[`\"‘]?(?:¿|what\\b|which\\b|how\\b|who\\b|where\\b|when\\b).*")) break;
                    quoted=part.startsWith("`");
                } else if (banner(part) || administrative(part) || winner(part)) break;
                if (!question.isEmpty())question.append(' ');
                question.append(part);
                if (question.length()>300)break;
                if (part.contains("?") || quoted && question.chars().filter(c->c=='`').count()>=2)break;
            }
            if(!question.isEmpty() && question.length()<=300) return Optional.of(question.toString().replaceAll("^[`\"‘]|[`\"’]$", "").strip());
        }
        return Optional.empty();
    }
    /** Wait for missing text, never for a quiet period in the chat. */
    public static Optional<Detection> detectComplete(String block,boolean contextual) {
        String text=block.strip();
        if(text.isEmpty() || text.endsWith(":") || text.chars().filter(c->c=='`').count()%2!=0)return Optional.empty();
        if(text.matches("(?s).*[+*/%^×÷−–—·✕=-]$"))return Optional.empty();
        return detect(block,contextual).filter(d -> {
            if(d.type()!=GameType.TRIVIA)return true;
            if(d.question().contains("?"))return true;
            // Some observed questions have no '?'; their closing quotes mark completion.
            String joined=block.replaceAll("\\s+"," ");
            return joined.contains("`"+d.question()+"`") || joined.contains("\""+d.question()+"\"");
        });
    }
    public static boolean playerChat(String line) {
        return line.contains("»") || line.stripLeading().matches("^<[^>]{1,40}>.*");
    }
    public static Optional<Detection> detect(String block, boolean contextual) {
        if (winner(block)) return Optional.empty();
        String[] lines=block.split("\\R"); String text=block.replace('`',' ').trim();
        String lower=normalized(text);
        // The new server sends a full expression, including several parenthesized groups.
        // Never fall back to a matching substring when that full expression is incomplete.
        for(int i=0;i<lines.length;i++) {
            Matcher prompt=MATH_PROMPT.matcher(lines[i].strip());
            if(prompt.matches()) {
                StringBuilder expression=new StringBuilder(prompt.group(1));
                for(int j=i+1;j<lines.length&&expression.length()<256;j++) {
                    String part=lines[j].strip();
                    if(part.isEmpty())continue;
                    if(!MATH_TEXT.matcher(part).matches())break;
                    expression.append(' ').append(part);
                }
                String candidate=expression.toString().strip();
                // Keep the existing equation mode when an old server uses this label for x.
                if(candidate.contains("=")&&VariableSolver.solve(candidate).isPresent())
                    return Optional.of(new Detection(GameType.VARIABLE,candidate,DetectionConfidence.HIGH));
                return completeMath(candidate);
            }
        }
        Optional<String> trivia=explicitTrivia(lines);
        if(trivia.isPresent())return Optional.of(new Detection(GameType.TRIVIA,trivia.get(),DetectionConfidence.HIGH));
        // A quoted glyph after "solve for" is the unknown, not a word to copy.
        if (SymbolVariableSolver.hasTarget(block))
            return SymbolVariableSolver.solve(block).map(answer -> new Detection(GameType.VARIABLE,block.trim(),DetectionConfidence.HIGH));
        for (int i=lines.length-1;i>=0;i--) {
            String raw=lines[i].trim(), l=normalized(raw);
            if (raw.isEmpty() || administrative(raw) || winner(raw) || (banner(raw) && !raw.contains("`") && !raw.contains("?") && !raw.contains(":") && !l.contains("escribir") && !l.contains("descifr") && !l.contains("completa") && !l.matches("(?s).*\\d+\\s*[+*/×÷xX·✕-]\\s*\\d+.*"))) continue;
            Matcher range=RANGE.matcher(raw);
            if (range.find() && (lower.contains("numero") || lower.contains("number") || lower.contains("adivina") || lower.contains("guess"))) return Optional.of(new Detection(GameType.GUESS_THE_NUMBER,range.group(1)+"-"+range.group(2),DetectionConfidence.HIGH));
            Matcher act=ACTION.matcher(raw);
            if (act.find() && (contextual || l.matches("(?s).*(?:desafio|challenge).*"))) {
                String a=normalized(act.group(1));
                GameType t = switch(a) { case "mata","caza","hunt","kill" -> GameType.HUNT; case "mina","mine","rompe","break" -> GameType.MINE; case "coloca","place" -> GameType.PLACE; case "pesca","fish" -> GameType.FISH; case "come","eat" -> GameType.EAT; case "craft","fabrica" -> GameType.CRAFT; default -> GameType.FURNACE; };
                return Optional.of(new Detection(t,act.group(2).trim(),DetectionConfidence.HIGH));
            }
            Matcher variable=VARIABLE.matcher(raw);
            while (variable.find()) {
                String equation=variable.group(1).trim();
                if (VariableSolver.solve(equation).isPresent() && (contextual || l.matches("(?s).*(?:resuelve|solve|calcula).*")))
                    return Optional.of(new Detection(GameType.VARIABLE,equation,DetectionConfidence.HIGH));
            }
            if(contextual&&MATH_TEXT.matcher(raw.replace("`","")).matches()&&raw.matches(".*[+*/%^×÷−–—xX·✕-].*"))return completeMath(raw);
            Matcher math=MATH.matcher(raw);
            if (math.find() && (contextual || l.matches("(?s).*(?:resuelve|solve|calcula|cuanto es|what is).*"))) return Optional.of(new Detection(GameType.MATH,math.group(1).trim(),DetectionConfidence.HIGH));
            Matcher mask=MASK.matcher(raw);
            // A quoted fillout may contain several words; do not truncate it at the first underscore.
            if(lower.matches("(?s).*(?:fill in the word|completa(?:r)? (?:correctamente )?(?:esta |la )?palabra).*")) {
                Matcher quotedMask=TICK.matcher(raw);
                if(quotedMask.find() && quotedMask.group(1).contains("_"))
                    return Optional.of(new Detection(GameType.FILLOUT,quotedMask.group(1).strip(),DetectionConfidence.HIGH));
            }
            if (mask.find() && (contextual || l.contains("completa") || l.contains("fill"))) return Optional.of(new Detection(GameType.FILLOUT,mask.group(),DetectionConfidence.HIGH));
            Matcher spanishQuestion=QUESTION.matcher(raw);
            if (spanishQuestion.find()) return Optional.of(new Detection(GameType.TRIVIA,spanishQuestion.group(),contextual?DetectionConfidence.HIGH:DetectionConfidence.MEDIUM));
            if (raw.contains("?") && (raw.startsWith("¿") || raw.endsWith("?") || l.matches("(?s).*(?:what|which|who|where|when|how|cual|quien|que).*"))) {
                if (contextual || l.matches("(?s).*(?:cual|que|quien|cuando|where|what|who|which|how).*"))
                    return Optional.of(new Detection(GameType.TRIVIA,raw,contextual?DetectionConfidence.HIGH:DetectionConfidence.MEDIUM));
            }
            Matcher tick=TICK.matcher(raw);
            String token=tick.find()?tick.group(1):null;
            Matcher word=WORD.matcher(raw);
            if (token==null && word.find()) token=word.group(1);
            if (token != null && !token.isBlank()) {
                GameType t = unscrambleInstruction(l) ? GameType.UNSCRAMBLE : unreverseInstruction(l) ? GameType.UNREVERSE : GameType.REACTION;
                if (l.matches("(?s).*(?:random|aleatori).*")) t=GameType.RANDOM;
                if (t!=GameType.REACTION || l.matches("(?s).*(?:escribe|escribir|write|type|copiar|copy|repite|repeat).*")) return Optional.of(new Detection(t,token,DetectionConfidence.HIGH));
            }
            // Plain token only after an explicit instruction in a previous line.
            if (contextual && raw.matches("[\\p{L}\\d]{3,30}") && !l.matches("(?s)(?:game|games|chat|seconds|segundos|minijuego|challenge)")) {
                int prior=i-1; while(prior>=0 && lines[prior].isBlank()) prior--;
                String instruction=prior>=0?normalized(lines[prior]):"";
                if (unscrambleInstruction(instruction)) return Optional.of(new Detection(GameType.UNSCRAMBLE,raw,DetectionConfidence.HIGH));
                if (unreverseInstruction(instruction)) return Optional.of(new Detection(GameType.UNREVERSE,raw,DetectionConfidence.HIGH));
                if (instruction.matches("(?s).*(?:escribe|escribir|write|type|aleatori|random).*")) return Optional.of(new Detection(instruction.matches("(?s).*(?:aleatori|random).*")?GameType.RANDOM:GameType.REACTION,raw,DetectionConfidence.HIGH));
            }
        }
        return Optional.empty();
    }
}
