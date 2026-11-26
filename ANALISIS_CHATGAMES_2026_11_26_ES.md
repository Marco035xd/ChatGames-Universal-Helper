# ChatGames: registros del 25 y 26 de noviembre de 2026

## Fuentes y alcance

Se revisaron únicamente `2026-11-25-2.log.gz` (**407 líneas**) y `2026-11-26-1.log.gz` (**1160 líneas**): **1567 líneas** en total. Hay **7 rondas completas**, todas con pregunta y resultado anunciado por el servidor: **4 de Trivia y 3 de Matemáticas**. El primer archivo contiene una ronda y el segundo seis.

Los niveles observados son **3 Fácil, 3 Normal y 1 Experto**. Las cuatro preguntas de trivia son nuevas respecto a 1.4.1. Se incorporan con las respuestas exactas del anuncio: el banco pasa de **121 a 125 preguntas**. No se observan tipos de minijuego adicionales ni cambios de formato.

Solo se usan anuncios del servidor para confirmar respuestas. El corpus `src/test/resources/server-rounds-142.json` conserva pregunta, nivel, respuesta y referencias a las líneas; los nombres de ganadores se sustituyen por «Jugador». No se publican registros completos ni conversaciones. No hay resultados huérfanos en esta muestra.

## Preguntas incorporadas

| Archivo | Hora | Nivel | Pregunta | Respuesta confirmada | Líneas: pregunta / resultado |
| --- | --- | --- | --- | --- | --- |
| `2026-11-25-2.log.gz` | 16:12:31 | Fácil | ¿Cuántas ruedas tiene una bicicleta común? | **2** | 390 / 397 |
| `2026-11-26-1.log.gz` | 09:32:00 | Normal | ¿En qué continente discurre el río Danubio? | **Europa** | 422 / 431 |
| `2026-11-26-1.log.gz` | 09:56:00 | Fácil | ¿Qué animal parecido a un caballo tiene rayas blancas y negras? | **Cebra** | 759 / 765 |
| `2026-11-26-1.log.gz` | 10:12:00 | Experto | ¿Qué piedra con tres escrituras ayudó a descifrar los jeroglíficos egipcios? | **Piedra de Rosetta** | 1053 / 1061 |

## Retos matemáticos verificados

| Archivo | Hora | Nivel | Expresión | Respuesta confirmada | Líneas: pregunta / resultado |
| --- | --- | --- | --- | --- | --- |
| `2026-11-26-1.log.gz` | 09:40:00 | Normal | `303 + 637` | **940** | 515 / 522 |
| `2026-11-26-1.log.gz` | 09:48:00 | Fácil | `95 + 13` | **108** | 656 / 665 |
| `2026-11-26-1.log.gz` | 10:04:00 | Normal | `1559 - 428` | **1131** | 903 / 913 |

## Compatibilidad e intervalo

Los cinco pares consecutivos del día 26 tienen **480 segundos exactos (08:00)** entre inicios. El día 25 solo aporta un inicio; no se calcula un intervalo entre sesiones de días distintos. Se conserva la estimación predeterminada de **08:01** y cualquier intervalo elegido por el usuario.

La actualización mantiene tiempos por categoría y dificultad, perfiles, juegos pausados, contador y respuestas personales. Las nuevas respuestas están disponibles en instalaciones existentes sin reemplazar `trivia.json`, `trivia-cache.json` ni los perfiles. Los datos personales y aprendidos conservan su prioridad. No se cambia la revisión de configuración ni los solucionadores.

## Verificación de 1.4.2

**117 pruebas JUnit aprobadas.** Las tres pruebas de integración de los registros recientes se ejecutan para las dos muestras: 23 rondas de 1.4.1 y 7 de 1.4.2. Comprueban pregunta, respuesta y dificultad, actualización con archivos personales y tiempos por nivel, deduplicación entre chat y log y cancelación de respuestas pendientes al anunciarse el resultado. Los tres problemas matemáticos coinciden con el motor existente.

Esta versión amplía el banco y cambia el número de versión. No se conectó al servidor remoto ni se repitió la comprobación visual de la GUI de 1.4.0.
