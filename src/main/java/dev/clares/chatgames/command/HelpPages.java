package dev.clares.chatgames.command;

import java.util.List;

/** Compact pages keep the command and its explanation together in the chat. */
public final class HelpPages {
    public record Entry(String command, String description) { }
    public record Page(String title, List<Entry> entries) { }
    private static final List<Page> PAGES = List.of(
        new Page("Uso diario", List.of(
            new Entry("/cgedit gui · /cgedit menu", "Abre tu interfaz privada: activa juegos, consulta información y guarda sus tiempos."),
            new Entry("/cgedit on · /cgedit off", "Activa o apaga todas las respuestas automáticas. Al apagar se cancelan los envíos pendientes."),
            new Entry("/cgedit status", "Comprueba si el mod y la lectura del chat están activos."),
            new Entry("/cgedit last", "Muestra el último reto, la respuesta y su estado."),
            new Entry("/cgedit game <tipo> on/off", "Activa o pausa un modo. Ejemplo: /cgedit game unscramble on"),
            new Entry("/cgedit stats on/off", "Muestra u oculta los avisos locales. Para apagar las respuestas usa /cgedit off.")
        )),
        new Page("Tiempos por juego", List.of(
            new Entry("/cgedit countdown · /cgedit countdown interval 8 1", "Contador superior en minutos:segundos. Actívalo con countdown on; cambia su vista con countdown view full o last10. Ajustes también en la GUI."),
            new Entry("/cgedit settime 0s", "Sin espera añadida. Acepta decimales: 2.1s o 2,1s, y milisegundos: 500ms."),
            new Entry("/cgedit settime random 0s 1s", "Responde con una espera de 0 a 1 segundo; intervalo inicial de esta versión."),
            new Entry("/cgedit settime math random 2.1s 2.9s", "Configura MATH entre 2.1 y 2.9 segundos. Puedes sustituir math por otro tipo y usar coma decimal."),
            new Entry("/cgedit settime math expert random 20s 30s", "Tiempo propio de Difícil/Experto. Usa easy, normal o expert con cualquier tipo, también trivia. Admite decimales."),
            new Entry("/cgedit settime <tipo> [nivel] default", "Sin nivel: hereda el general. Con nivel: vuelve a la base del juego. La GUI permite editar los tres niveles.")
        )),
        new Page("Pruebas y diagnóstico", List.of(
            new Entry("/cgedit test <tipo> <texto>", "Prueba una solución local sin enviarla al servidor. Ejemplo: test unscramble uMics icsD"),
            new Entry("/cgedit simulate <texto>", "Prueba la detección de un mensaje sin responder en el servidor."),
            new Entry("/cgedit testlog", "Muestra el archivo de registro y la última línea de chat leída."),
            new Entry("/cgedit debug on/off", "Activa o desactiva los detalles de detección y envío."),
            new Entry("/cgedit cache stats", "Consulta cuántas respuestas hay guardadas."),
            new Entry("/cgedit ai status", "Consulta el estado y el modelo de la IA opcional.")
        )),
        new Page("Panel, perfiles e historial",List.of(
            new Entry("/cgedit profile save <nombre>","Guarda tus tiempos, juegos, HUD, avisos y contador. No sobrescribe perfiles ni copia datos de IA."),
            new Entry("/cgedit profile list · /cgedit profile show <nombre>","Lista tus perfiles y consulta sus ajustes antes de cargarlos."),
            new Entry("/cgedit profile load <nombre> · /cgedit profile undo","Aplica un perfil o restaura los ajustes anteriores. Cancela los envíos pendientes."),
            new Entry("/cgedit history <página> · /cgedit report","Últimos 100 retos de esta ejecución y tiempos de envío. Enviar no confirma ganar."),
            new Entry("/cgedit pause · /cgedit resume · /cgedit hud on/off","Pausa y reanuda respuestas o controla el HUD. Teclas: G abre el panel y H pausa."),
            new Entry("/cgedit profile delete <nombre> confirm","Borra solo el perfil elegido. La configuración en uso se conserva.")
        ))
    );
    private HelpPages() { }
    public static int count() { return PAGES.size(); }
    public static Page page(int number) {
        if (number < 1 || number > count()) throw new IllegalArgumentException("Página fuera de rango");
        return PAGES.get(number - 1);
    }
}
