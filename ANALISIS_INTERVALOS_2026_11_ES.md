# Intervalo de ChatGames — noviembre de 2026

Se revisaron 21 registros disponibles en la instancia de Minecraft (archivos de noviembre y el registro activo al hacer la revisión). Se identificaron **191 inicios de minijuegos** y **176 pares consecutivos dentro de un mismo archivo**. Los mensajes del helper y las conversaciones de jugadores se excluyeron.

Los registros originales permanecen en el equipo. Este informe y el corpus de pruebas solo conservan horas, diferencias y referencias de líneas; no incluyen conversaciones ni nombres de jugadores.

## Tiempo entre inicios

| Intervalo observado | Pares |
| --- | ---: |
| 8:00 | 90 |
| 8:01 | 67 |
| 8:02 | 18 |
| 8:03 | 1 |

La media fue de **480.60 segundos**. Se utiliza **8 minutos y 1 segundo (481 s)** como estimación inicial. Las marcas de los registros tienen resolución de un segundo y la carga del servidor puede introducir variaciones.

## Desde el ganador hasta el siguiente inicio

El resultado no reinicia otros ocho minutos completos: se descuenta el tiempo que ya consumió el reto. En los pares observados, la espera posterior al resultado fue de **7:53 a 8:00**.

| Registro | Inicio (línea) | Resultado (línea) | Siguiente inicio (línea) | Entre inicios | Tras el resultado |
| --- | --- | --- | --- | --- | --- |
| `2026-11-15-1.log.gz` | 14:04:42 (2865) | 14:04:45 (2874) | 14:12:43 (3086) | 8:01 | 7:58 |
| `2026-11-15-1.log.gz` | 14:12:43 (3086) | 14:12:48 (3094) | 14:20:45 (3246) | 8:02 | 7:57 |
| `2026-11-15-1.log.gz` | 15:57:02 (5297) | 15:57:07 (5305) | 16:05:03 (5461) | 8:01 | 7:56 |
| `2026-11-16-1.log.gz` | 10:20:07 (393) | 10:20:09 (398) | 10:28:07 (475) | 8:00 | 7:58 |

## Comportamiento del contador

Cada inicio real vuelve a sincronizar la estimación. Mientras el reto está activo, el panel indica «Minijuego en curso». Cuando llega el resultado o el cierre de la ronda, muestra «Nuevo minijuego en MM:SS» con el tiempo restante desde el inicio original. Por ejemplo, con intervalo 08:01 y respuesta a los 3 segundos, muestra 07:58.

El panel puede activarse, desactivarse o limitarse a los últimos diez segundos. Su visibilidad no activa ni pausa las respuestas automáticas. Al desconectarse se borra la referencia; al entrar espera un reto real para sincronizar. Una estimación vencida espera el siguiente anuncio, sin inventar una nueva ronda.

El intervalo puede editarse por minutos y segundos, tanto en la interfaz como con `/cgedit countdown interval 8 1`. El corpus `src/test/resources/countdown-intervals-125.json` conserva las 176 mediciones para las pruebas.
