package dev.clares.chatgames.gui;

import dev.clares.chatgames.game.RoundHistory;
import net.minecraft.client.gui.screens.Screen;
import java.util.List;

final class RoundDetailScreen extends InfoScreen {
    RoundDetailScreen(Screen parent,RoundHistory.Entry e){super(parent,ChatGamesScreen.title(e.type())+" · ronda",List.of(
        "Estado: "+e.status(),"Dificultad: "+(e.difficulty().isEmpty()?"No indicada":e.difficulty()),"Reto: "+e.question().replaceAll("\\s+"," "),"Respuesta calculada: "+(e.answer().isEmpty()?"—":e.answer()),
        "Fuente: "+(e.source().isEmpty()?"—":e.source()),"Espera elegida: "+ChatGamesScreen.seconds(e.configuredMs()),
        "Primer envío real: "+ChatGamesScreen.seconds(e.sentAfterMs()),"Envíos: "+e.sends(),
        "Respuesta anunciada: "+(e.confirmedAnswer().isEmpty()?"No observada":e.confirmedAnswer()),"",
        "El tiempo real incluye cola, cálculo y espera desde que llega el reto completo. No incluye el viaje al servidor.",
        "Una respuesta enviada o una ronda finalizada no confirma que hayas ganado."));}
}
