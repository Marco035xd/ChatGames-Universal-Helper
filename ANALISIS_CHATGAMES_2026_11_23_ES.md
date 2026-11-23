# ChatGames de Infection: cambio de formato observado el 23 de noviembre de 2026

## Fuentes

Se revisaron `2026-11-21-1.log.gz`, `2026-11-21-2.log.gz`, el archivo proporcionado `2026-11-21-3.log.gz` y una lectura de `latest.log` del 23 de noviembre (hasta la línea 2036). En total: 5952 líneas originales y 26 rondas con respuesta anunciada por el servidor.

Los tres archivos del 21 contienen 16 rondas del formato anterior. El sistema con niveles se observa en las 10 rondas de `latest.log`: 5 Fácil, 1 Normal y 4 Experto. No se puede determinar la hora exacta de despliegue del cambio a partir de estas sesiones discontinuas.

No se distribuyen los registros completos ni mensajes de jugadores. El corpus de pruebas `src/test/resources/server-rounds-131.json` conserva solo anuncios, preguntas, respuestas confirmadas, tiempos y referencias a líneas; los nombres de ganadores se sustituyen por «Jugador».

## Cambios observados

- Cabecera MINIJUEGO seguida por `(Fácil)`, `(Normal)` o `(Experto)`.
- Matemáticas: `Resuelve este problema: <expresión>`; el límite de 60 segundos pasa a otra línea.
- Trivia: pregunta antes de `Tienes 60 segundos para responder.`.
- Resultado sin comillas: `Jugador respondió 589 en 23.3 s.`.
- Cierre: `Recibió los premios de Experto.`.

La extracción anterior por una expresión regular podía quedarse con un fragmento de operaciones con varios grupos de paréntesis. El nuevo texto de resultado tampoco coincidía con «respondio primero», usado para cancelar y aprender. Ahora se valida y evalúa la expresión completa y se reconoce el nuevo resultado con su respuesta exacta.

## Matemáticas verificadas

| Hora | Expresión | Respuesta anunciada | Líneas de latest.log: reto / resultado |
| --- | --- | --- | --- |
| 12:36:12 | 7 + 10 | 17 | 410 / 417 |
| 13:00:13 | (960 ÷ 12 × 8 + 148) ÷ 4 | 197 | 866 / 878 |
| 13:08:14 | (744 ÷ 8 - 14) × 15 + 200 ÷ 10 | 1205 | 1024 / 1040 |
| 13:16:14 | 477 ÷ 9 × 10 - (232 ÷ 8 + 12) | 489 | 1174 / 1190 |
| 13:40:15 | 1781 - 186 | 1595 | 1808 / 1818 |
| 13:48:16 | 765 ÷ 9 × 6 + (104 - 25) | 589 | 1959 / 1971 |

Se mantienen los paréntesis. Multiplicaciones y divisiones se aplican de izquierda a derecha dentro de cada grupo, antes de sumas y restas. El tiempo configurado para Matemáticas se usa también con Experto.

## Base de respuestas

Se incorporaron cuatro preguntas nuevas confirmadas en `latest.log`:

| Pregunta | Respuesta | Líneas: pregunta / resultado |
| --- | --- | --- |
| ¿En qué estado físico se encuentra el hielo? | Sólido | 536 / 543 |
| ¿Qué parte del cuerpo utilizamos para oír? | Oídos | 658 / 666 |
| ¿Cuántos jugadores tiene un equipo de fútbol en el campo al comenzar? | 11 | 1393 / 1400 |
| ¿Qué animal doméstico ladra? | Perro | 1611 / 1619 |

Los archivos del 21 aportan cinco anagramas exactos (`aCmhúp`, `soMartord`, `lGola`, `cEo`, `aélAcnm`) y cuatro máscaras (`E_t__te`, `_gu_ero`, `_omp__r`, `_x_l_r__or`). La palabra canónica adicional es «Almacén». «Fusión» y «Morado» ya estaban en el banco de trivia. Los datos nuevos están incluidos como recursos para llegar también a instalaciones con configuración existente.

## Intervalos

Las 10 rondas nuevas empiezan entre 12:36:12 y 13:48:16. Los 9 intervalos consecutivos son 5 de 480 segundos y 4 de 481 segundos. Esto sigue siendo compatible con la estimación predeterminada 08:01; no se modifica el intervalo del usuario. El nuevo resultado termina la fase «en curso», sin reiniciar ocho minutos desde la respuesta.

## Validación

100 pruebas JUnit aprobadas. Incluyen todas las rondas de este corpus, las 328 de octubre y pruebas de cancelación, deduplicación, aprendizaje con respuestas sin comillas, resultados con coma decimal, conservación de preferencias y perfiles, expresiones incompletas y contador.

Se reprodujo el ejemplo de Experto en un mundo local de desarrollo de Minecraft 26.2: cálculo 589, dificultad Experto, espera elegida 631 ms, primer envío real 636 ms, un envío y respuesta anunciada 589. Se verificaron el cierre y la cuenta restante. Esta prueba no mide la latencia ni el comportamiento de una conexión real al servidor Infection.
