# ChatGames: registros adicionales del 24 de noviembre de 2026

## Fuentes y alcance

Se analizaron únicamente `2026-11-24-2.log.gz` (**2414 líneas**) y `2026-11-24-3.log.gz` (**1223 líneas**): **3637 líneas** en total. Se encontraron **23 rondas completas**, con pregunta y resultado anunciado por el servidor: **12 de Trivia y 11 de Matemáticas**. El primer archivo contiene 17 rondas completas y el segundo 6.

Hay **7 rondas Fácil, 12 Normal y 4 Experto**. Las 12 preguntas de trivia son nuevas frente al banco de 1.4.0; se añaden con la escritura exacta de la respuesta del servidor. El banco pasa de **109 a 121 preguntas**. Los retos matemáticos ya se resuelven con el motor existente. No se observan categorías adicionales ni cambios de formato.

El resultado **«Plata»**, a las **10:28:38**, línea **299** del primer archivo, no tiene una pregunta visible en estos registros. Se excluye del banco y del corpus de rondas completas. No se deduce a qué pregunta corresponde.

Solo se usan anuncios del servidor para confirmar respuestas. El corpus `src/test/resources/server-rounds-141.json` contiene los retos, niveles, respuestas y referencias de línea; los nombres de ganadores se sustituyen por «Jugador». No se publican registros completos ni conversaciones.

## Preguntas incorporadas

| Archivo | Hora | Nivel | Pregunta | Respuesta confirmada | Líneas: pregunta / resultado |
| --- | --- | --- | --- | --- | --- |
| `2026-11-24-2.log.gz` | 10:36:27 | Experto | ¿Qué gran luna de Neptuno tiene una órbita retrógrada? | **Tritón** | 360 / 381 |
| `2026-11-24-2.log.gz` | 10:44:28 | Experto | ¿Qué ordenamiento divide una lista y después fusiona sus partes ordenadas? | **Merge sort** | 496 / 505 |
| `2026-11-24-2.log.gz` | 11:00:28 | Fácil | ¿Cuántos lados tiene un hexágono? | **6** | 686 / 692 |
| `2026-11-24-2.log.gz` | 11:08:28 | Normal | ¿Qué unidad del SI mide la frecuencia? | **Hercio** | 860 / 878 |
| `2026-11-24-2.log.gz` | 11:16:28 | Experto | ¿Qué estructura celular sintetiza proteínas leyendo ARN mensajero? | **Ribosoma** | 1027 / 1038 |
| `2026-11-24-2.log.gz` | 11:24:29 | Experto | ¿Qué algoritmo clásico busca caminos mínimos con pesos no negativos? | **Dijkstra** | 1144 / 1162 |
| `2026-11-24-2.log.gz` | 12:04:30 | Fácil | ¿Qué objeto se utiliza para golpear la pelota en tenis? | **Raqueta** | 1677 / 1685 |
| `2026-11-24-2.log.gz` | 12:20:32 | Fácil | ¿Cuántos días tiene una semana? | **7** | 1904 / 1911 |
| `2026-11-24-2.log.gz` | 12:36:32 | Normal | ¿Quién escribió El principito? | **Antoine de Saint-Exupéry** | 2181 / 2194 |
| `2026-11-24-3.log.gz` | 13:56:36 | Normal | ¿Cuál es la fórmula química del agua? | **H2O** | 308 / 314 |
| `2026-11-24-3.log.gz` | 14:04:36 | Normal | ¿Qué planeta es famoso por su Gran Mancha Roja? | **Júpiter** | 408 / 422 |
| `2026-11-24-3.log.gz` | 14:20:37 | Normal | ¿Qué elemento químico tiene el símbolo Au? | **Oro** | 801 / 808 |

## Retos matemáticos verificados

| Archivo | Hora | Nivel | Expresión | Respuesta confirmada | Líneas: pregunta / resultado |
| --- | --- | --- | --- | --- | --- |
| `2026-11-24-2.log.gz` | 10:52:28 | Fácil | `39 - 29` | **10** | 596 / 602 |
| `2026-11-24-2.log.gz` | 11:32:29 | Fácil | `35 - 33` | **2** | 1242 / 1248 |
| `2026-11-24-2.log.gz` | 11:40:29 | Normal | `29 × 23` | **667** | 1346 / 1355 |
| `2026-11-24-2.log.gz` | 11:48:30 | Normal | `1062 - 383` | **679** | 1482 / 1491 |
| `2026-11-24-2.log.gz` | 11:56:30 | Normal | `24 × 36` | **864** | 1552 / 1560 |
| `2026-11-24-2.log.gz` | 12:12:31 | Normal | `45 × 43` | **1935** | 1782 / 1792 |
| `2026-11-24-2.log.gz` | 12:28:32 | Fácil | `12 + 23` | **35** | 2048 / 2056 |
| `2026-11-24-2.log.gz` | 12:44:32 | Normal | `1106 ÷ 14` | **79** | 2342 / 2350 |
| `2026-11-24-3.log.gz` | 14:12:36 | Normal | `1281 - 375` | **906** | 589 / 606 |
| `2026-11-24-3.log.gz` | 14:28:39 | Normal | `864 ÷ 12` | **72** | 983 / 996 |
| `2026-11-24-3.log.gz` | 14:36:40 | Fácil | `99 - 18` | **81** | 1173 / 1182 |

## Compatibilidad e intervalo

Los 21 pares consecutivos dentro de cada archivo tienen intervalos de **480 a 482 segundos**, con media de **480.43 s**. No se trata como un intervalo consecutivo la separación entre las dos sesiones. Se conserva la estimación inicial de **08:01** y cualquier intervalo elegido por el usuario.

Los tiempos por categoría y dificultad, juegos pausados, perfiles, contador y respuestas personales se conservan. Las respuestas incluidas llegan a instalaciones existentes sin reemplazar `trivia.json`, `trivia-cache.json` ni los perfiles. Los datos personales y aprendidos mantienen su prioridad. No se cambia la revisión de configuración.

## Verificación de 1.4.1

**114 pruebas JUnit aprobadas.** Las tres pruebas nuevas reproducen las 23 rondas con su dificultad y respuesta exacta, comprueban la actualización con configuración previa y tiempos por nivel, la prioridad de respuestas personales y la conservación de archivos, y verifican la deduplicación de chat/log y la cancelación de envíos pendientes al anunciarse el resultado.

Esta actualización amplía el banco de datos; no cambia los solucionadores ni las pantallas, aparte del número de versión. No se conectó al servidor remoto ni se repitió la comprobación visual de la GUI de 1.4.0.
