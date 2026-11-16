# Cambios

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
