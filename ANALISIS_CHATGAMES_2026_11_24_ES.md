# ChatGames: registros del 23 y 24 de noviembre de 2026

## Fuentes y alcance

Se revisaron únicamente los dos archivos proporcionados: `2026-11-23-1.log.gz` (2036 líneas) y `2026-11-24-1.log.gz` (469 líneas). Son **2505 líneas y 14 rondas con resultado confirmado por el servidor**: 7 de Matemáticas y 7 de Trivia.

Las 10 rondas del día 23 coinciden con las que se analizaron como `latest.log` para 1.3.1. No se cuentan como contenido nuevo. El día 24 contiene 4 rondas: una suma y tres preguntas de trivia nuevas. No se observan tipos de minijuego adicionales ni cambios de formato respecto a 1.3.1.

Solo se incorporan respuestas anunciadas por el servidor. Los mensajes y las respuestas de jugadores no se usan como confirmación. El corpus `src/test/resources/server-rounds-132.json` conserva preguntas, anuncios, dificultad, respuesta y referencias a las líneas; los nombres de los ganadores se sustituyen por «Jugador». No se publican logs completos.

## Rondas del 24 de noviembre

| Hora | Dificultad | Reto | Respuesta anunciada | Líneas: reto / resultado |
| --- | --- | --- | --- | --- |
| 08:20:26 | Fácil | 66 + 26 | 92 | 304 / 310 |
| 08:28:26 | Experto | ¿En qué año se firmó el Tratado de Tordesillas? | 1494 | 326 / 335 |
| 08:36:26 | Normal | ¿Qué metal es líquido a temperatura ambiente habitual? | Mercurio | 376 / 384 |
| 08:44:26 | Experto | ¿Qué protocolo de transporte establece conexiones y entrega fiable y ordenada? | TCP | 441 / 455 |

Las referencias corresponden a `2026-11-24-1.log.gz`. La suma `66 + 26 → 92` ya se resuelve con el motor existente. Se añaden al banco las preguntas con las respuestas exactas **1494**, **Mercurio** y **TCP**, conservando mayúsculas y escritura del anuncio.

## Compatibilidad e intervalo

Los tres intervalos consecutivos del día 24 son de **480 segundos exactos** (08:00), entre 08:20:26 y 08:44:26. Esta muestra no requiere cambiar la estimación predeterminada de 08:01 ni el intervalo que haya elegido el usuario. Los niveles siguen siendo Fácil, Normal y Experto; sus tiempos de respuesta se toman del tipo de juego.

Los recursos incluidos llegan a instalaciones con configuración existente sin sustituir `trivia.json`, `trivia-cache.json`, las respuestas aprendidas ni los perfiles. No se cambia la revisión de configuración ni se reinician los tiempos o estados de los juegos.

## Verificación de 1.3.2

103 pruebas JUnit aprobadas. Las tres pruebas nuevas comprueban las 14 rondas, las respuestas exactas y dificultad, su disponibilidad tras actualizar, la conservación de preferencias y archivos personales, la deduplicación entre chat y log y el cierre de los envíos pendientes al observar el resultado.

El logo del README se copia de la imagen proporcionada por el usuario, sin modificar sus píxeles. No se ha probado esta versión conectada al servidor remoto ni se ha repetido la comprobación visual del juego de 1.3.1; los cambios funcionales de 1.3.2 consisten en ampliar el banco de trivia.
