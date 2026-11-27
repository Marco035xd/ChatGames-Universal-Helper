# ChatGames: segundo registro del 26 de noviembre de 2026

## Fuente y alcance

Se revisó únicamente `2026-11-26-2.log.gz`, de **3723 líneas**. Se encontraron **16 rondas completas** con pregunta y resultado anunciado por el servidor: **11 de Trivia y 5 de Matemáticas**. Los niveles son **6 Fácil, 6 Normal y 4 Experto**. No hay resultados huérfanos en esta muestra.

Ocho preguntas de Trivia son nuevas frente a 1.4.2 y tres ya estaban incorporadas con la misma respuesta: TCP, Júpiter y Ribosoma. El banco pasa de **125 a 133 preguntas**. No se observan tipos de minijuego adicionales ni cambios de formato.

Solo se utilizan resultados del servidor para confirmar respuestas. El corpus `src/test/resources/server-rounds-143.json` conserva pregunta, nivel, respuesta y referencias de línea; los nombres de ganadores se sustituyen por «Jugador». No se publican registros completos ni conversaciones.

## Preguntas incorporadas

| Hora | Nivel | Pregunta | Respuesta confirmada | Líneas: pregunta / resultado |
| --- | --- | --- | --- | --- |
| 11:08:00 | Fácil | ¿Qué color sale al mezclar pintura azul y amarilla? | **Verde** | 476 / 484 |
| 11:16:01 | Experto | ¿Cuántos grados suman los ángulos interiores de un dodecágono plano? | **1800** | 666 / 692 |
| 11:24:01 | Normal | ¿Cuál es la capital de Brasil? | **Brasilia** | 954 / 962 |
| 11:40:01 | Fácil | ¿Cuál es la capital de Ecuador? | **Quito** | 1337 / 1343 |
| 11:56:01 | Normal | ¿Cuántos grados suman los ángulos interiores de un cuadrilátero plano? | **360** | 1741 / 1747 |
| 12:36:02 | Fácil | ¿Qué satélite natural orbita la Tierra? | **Luna** | 2842 / 2849 |
| 12:52:02 | Normal | ¿Cómo se llama el lado opuesto al ángulo recto de un triángulo rectángulo? | **Hipotenusa** | 3310 / 3322 |
| 13:08:02 | Fácil | ¿Qué figura redonda tiene todos sus puntos a igual distancia del centro? | **Circunferencia** | 3704 / 3715 |

Las preguntas sobre ángulos, hipotenusa y circunferencia se clasifican como **Trivia**. Se guardan las respuestas exactas del anuncio, incluidas **1800**, **360**, **Brasilia** y **Circunferencia**.

## Preguntas ya incorporadas

| Hora | Pregunta | Respuesta confirmada | Líneas: pregunta / resultado |
| --- | --- | --- | --- |
| 12:20:01 | ¿Qué protocolo de transporte establece conexiones y entrega fiable y ordenada? | **TCP** | 2411 / 2425 |
| 12:28:02 | ¿Qué planeta es famoso por su Gran Mancha Roja? | **Júpiter** | 2582 / 2595 |
| 12:44:02 | ¿Qué estructura celular sintetiza proteínas leyendo ARN mensajero? | **Ribosoma** | 3124 / 3136 |

Estas tres preguntas se conservan sin duplicarlas.

## Retos matemáticos verificados

| Hora | Nivel | Expresión | Respuesta confirmada | Líneas: pregunta / resultado |
| --- | --- | --- | --- | --- |
| 11:32:01 | Normal | `22 × 33` | **726** | 1180 / 1190 |
| 11:48:01 | Fácil | `21 + 32` | **53** | 1531 / 1539 |
| 12:04:01 | Fácil | `92 - 34` | **58** | 1927 / 1935 |
| 12:12:01 | Experto | `(1104 ÷ 12 - 17) × 4 + 512 ÷ 8` | **364** | 2222 / 2238 |
| 13:00:02 | Normal | `472 + 967` | **1439** | 3520 / 3528 |

## Compatibilidad e intervalo

Los **15 pares consecutivos** tienen intervalos de **480 a 481 segundos**, con media de **480.13 s**. Se conserva la estimación predeterminada de **08:01** y cualquier intervalo elegido por el usuario.

Se mantienen tiempos por categoría y dificultad, perfiles, juegos pausados, contador y respuestas personales. Las nuevas respuestas llegan a instalaciones existentes sin reemplazar `trivia.json`, `trivia-cache.json` ni los perfiles. Los datos personales y aprendidos conservan su prioridad. No se cambia la revisión de configuración ni los solucionadores.

## Verificación de 1.4.3

**120 pruebas JUnit aprobadas.** Las mismas tres pruebas de integración de registros recientes se ejecutan para las tres muestras: 23 rondas de 1.4.1, 7 de 1.4.2 y 16 de 1.4.3. Comprueban pregunta, tipo, dificultad y respuesta, actualización con archivos personales y tiempos por nivel, deduplicación entre chat y log y cancelación de respuestas pendientes al anunciarse el resultado. Los cinco problemas matemáticos coinciden con el motor existente.

Esta versión amplía el banco y cambia el número de versión. No se conectó al servidor remoto ni se repitió la comprobación visual de la GUI de 1.4.0.
