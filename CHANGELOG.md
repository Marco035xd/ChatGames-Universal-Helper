# 1.3.1 — Nuevo formato de ChatGames

- Reconoce Fácil, Normal y Experto; muestra el nivel en el HUD, historial y comandos de diagnóstico.
- Extrae las operaciones completas del anuncio, con varios grupos de paréntesis. Evita responder con un fragmento de una operación incompleta o inválida.
- Reconoce «Jugador respondió … en … s.» y «Recibió los premios de …»: cierre, cancelación, aprendizaje y contador tras el resultado.
- Añade 4 preguntas de trivia, 5 anagramas exactos, 4 patrones para completar palabras y 1 palabra canónica confirmados en los logs.
- Conserva el formato anterior, tiempos decimales, estados, perfiles y respuestas personales.
- 100 pruebas JUnit aprobadas; reproducción local del reto Experto con respuesta 589, dificultad y cierre de ronda verificados en Minecraft 26.2.

# 1.3.0 — Centro de control

- Panel con cinco pestañas: Inicio, Juegos, Historial, Perfiles y Pruebas.
- Tema uniforme, pestaña seleccionada resaltada, búsqueda sin tildes y filtros por estado.
- Historial temporal de 100 retos: espera elegida, tiempo de primer envío, fuente, resultado observado y resumen. No atribuye victorias.
- Hasta 32 perfiles con vista previa, carga, confirmación de borrado y deshacer persistente; excluyen IA, rutas de logs y datos aprendidos.
- Laboratorio local con ejemplos y resultados asíncronos; no envía chat ni consulta IA.
- H para pausar/reanudar, reasignable; se mantiene G.
- Comandos profile, history, report, hud, pause y resume, autocompletado de perfiles y cuarta página de ayuda con comandos pulsables.
- HUD del reto renovado con estado en español y barra de espera; respeta F1 y las pantallas abiertas.
- Escritura por sustitución de config/perfiles y restauración en memoria ante fallos de carga.
- El lector de logs espera a que termine su hilo al cerrar, evitando bloqueos intermitentes del archivo en Windows.
- 93 pruebas automatizadas y revisión funcional en Minecraft 26.2 con un mundo local de desarrollo.

# Cambios

## 1.2.5

- Panel superior permanente con «Nuevo minijuego en MM:SS», activable desde Contador y con alternativa de mostrar solo los últimos 10 segundos.
- El panel distingue el reto en curso y la espera posterior al resultado. La respuesta no reinicia un intervalo completo: se descuenta lo ya transcurrido desde el inicio.
- Intervalo configurable por minutos y segundos; valor inicial 08:01 basado en 176 intervalos medidos en los registros de noviembre (entre 08:00 y 08:03).
- El antiguo intervalo predeterminado de 25 minutos se migra a 08:01; se conservan los demás intervalos personalizados y los controles de activación, sonido y volumen.
- Comandos `countdown interval 8 1` y `countdown view full/last10`. Se mantienen el aviso musical final, la sincronización con retos reales y el reinicio al desconectarse.
- Informe de horas y referencias en `ANALISIS_INTERVALOS_2026_11_ES.md`, con corpus de regresión sin conversaciones de jugadores.

## 1.2.4

- Eliminada la espera de 350 ms de silencio o hasta 3 segundos para agrupar el chat: los retos completos se procesan al llegar.
- Lectura directa de los retos desde el chat en vivo; el archivo de registro queda como respaldo con deduplicación.
- El intervalo de cada juego se cuenta desde la recepción del reto completo e incluye el tiempo de cola y cálculo local. Si ese tiempo ya transcurrió, se envía en cuanto la respuesta está lista.
- Las preguntas partidas y las ecuaciones conservan sus líneas hasta disponer del texto necesario, sin temporizador de espera después de completarlas.
- Los mensajes de jugadores no se añaden a los retos. Se mantienen los controles de pausa y cancelación y los ajustes guardados.

## 1.2.3

- Botones −0.1 y +0.1 segundos para ajustar el tiempo fijo o los límites mínimo y máximo de cada minijuego.
- Opción rápida de intervalo aleatorio de 2.1 a 2.9 segundos y ejemplos visibles de decimales con punto o coma.
- Los comandos de tiempo aceptan coma decimal además de punto, con sugerencias para intervalos decimales.
- Los tiempos propios se guardan con precisión de milisegundos y se conservan al reiniciar.

## 1.2.2

- Acceso rápido al menú privado con la tecla G, configurable en los controles de Minecraft bajo la categoría ChatGames.
- El acceso utiliza la misma interfaz de `/cgedit gui` y funciona con las respuestas automáticas en pausa.
- El atajo solo abre el menú durante el juego y descarta las pulsaciones recibidas mientras otra pantalla está abierta.
- Traducciones del control al español y al inglés.

## 1.2.1

- Revisión de 70 registros de ChatGames del 2 al 20 de octubre de 2026, con 328 rondas confirmadas por resultados del servidor.
- Incorporación de 15 preguntas de trivia, 57 máscaras para completar palabras, 52 anagramas concretos y 19 palabras del vocabulario del servidor.
- Consulta de los anagramas incluidos y del vocabulario canónico al actualizar instalaciones existentes, conservando la prioridad de los datos personales y aprendidos.
- Corpus de regresión con referencias a los registros originales y análisis en `ANALISIS_LOGS_2026_10_ES.md`.

## 1.2.0

- Migración a Minecraft Java 26.2, Fabric Loader 0.19.5, Fabric API 0.161.0+26.2 y Java 25.
- Actualización de pantallas, widgets, componentes de chat, HUD y envío de respuestas a las APIs de 26.2.
- Aviso de próximo minijuego durante los últimos 10 segundos, con una nota privada por número.
- Intervalo inicial de 25 minutos, configurable; sincronización por retos reales, anuncios reconocidos y ajuste manual.
- Reinicio de la referencia al desconectarse; las estimaciones vencidas no inventan nuevas rondas.
- GUI del contador con activación, sonido, volumen, intervalo, guardado y cancelación.
- Vista previa de diez segundos con un reloj independiente del evento real.
- Lectura de mensajes con saltos de línea escapados y del formato de log de desarrollo de 26.2.
- Conservación de los tiempos de respuesta y preferencias de 1.1.0; las bases de palabras y trivia permanecen iguales.

## 1.1.0

- Interfaz privada con tarjetas, información al pasar el cursor, controles por juego y editor de intervalos.
- Ayuda, autocompletado, controles de HUD y avisos, persistencia y documentación para GitHub.

## 1.0.9

- Intervalo inicial de respuestas de 0 a 1 segundo y soporte de 0 segundos sin espera añadida.
