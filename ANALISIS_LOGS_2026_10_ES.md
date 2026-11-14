# ChatGames Universal Helper 1.2.1 — registros de octubre de 2026

Fuente: `2026-10-02-1.log.zip`, con 70 archivos `.log.gz` fechados del **2 al 20 de octubre de 2026**. Se recorrieron 30 137 885 líneas; solo se incorporaron preguntas, instrucciones y resultados identificados como rondas de ChatGames. Los registros completos y el chat de jugadores no forman parte de esta actualización.

## Criterio de incorporación

Cada respuesta se asoció con una instrucción anterior del mismo archivo y con un resultado del servidor dentro de los 65 segundos siguientes al inicio de una ronda de 60 segundos. Se utilizaron los mensajes «respondio primero» y «La respuesta correcta era». Los intentos de jugadores y los mensajes del propio helper no se usaron como respuestas. Las letras de los anagramas, las máscaras de completar palabras y los resultados matemáticos se verificaron antes de aceptar cada pareja.

Se conservaron exactamente las respuestas anunciadas, incluidas tildes y mayúsculas. Por ejemplo, el servidor responde «Flauta» a la pregunta «¿Qué instrumento se toca soplando y tiene teclas para cambiar las notas?». La base reproduce el criterio observado del servidor.

## Resultado

| Tipo observado | Rondas confirmadas |
| --- | ---: |
| Trivia | 73 |
| Completar palabras | 61 |
| Ordenar palabras | 54 |
| Escribir la palabra | 66 |
| Matemáticas | 74 |
| **Total** | **328** |

Los cinco tipos observados ya estaban contemplados por el mod. No se encontraron tipos nuevos en estos registros.

Se añadieron **15 preguntas de trivia**, **57 máscaras de completar palabras**, **52 anagramas concretos** y **19 palabras al vocabulario canónico del servidor**. No se encontraron contradicciones con las respuestas existentes. Las máscaras se guardaron en el contexto `es60` para conservar la separación entre formatos del servidor.

El archivo `src/test/resources/server-rounds-121.json` conserva las 328 rondas confirmadas con instrucciones, respuestas y referencias al archivo y líneas de origen, sin nombres de jugadores ni conversaciones.

## Registros incompletos

Se descartaron dos rondas que no incluyen un resultado del servidor en su archivo:

- `R__i_o` — `2026-10-04-5.log.gz:498`.
- `¿Cuál es el opuesto de "frío"?` — `2026-10-20-2.log.gz:975`.

La pregunta sobre el opuesto de «frío» ya estaba en la base y tuvo otras rondas confirmadas. La ronda incompleta no se utilizó como evidencia adicional. También se descartaron cuatro anuncios duplicados de resultados y un resultado sin una instrucción anterior reconocida en su mismo archivo.

## Trivia nueva

| Pregunta | Respuesta del servidor | Archivo:línea de pregunta → resultado |
| --- | --- | --- |
| ¿Qué parte del cuerpo utilizamos para caminar? | Pierna | `2026-10-03-5.log.gz:1120 → 1127` |
| ¿Cómo se llama el lugar donde viven los peces? | Agua | `2026-10-03-6.log.gz:1045 → 1052` |
| ¿Cómo se llama el líquido transparente que cae del cielo cuando llueve? | Agua | `2026-10-03-6.log.gz:2009 → 2018` |
| ¿Cuál es el planeta más grande del sistema solar? | Júpiter | `2026-10-04-2.log.gz:2618 → 2633` |
| ¿Cuál es el animal que dice "miau" y le gusta el pescado? | Gato | `2026-10-04-6.log.gz:2300 → 2305` |
| ¿Cómo se llama el lugar donde se guarda la ropa en la casa? | Armario | `2026-10-05-4.log.gz:1186 → 1191` |
| ¿Qué color tiene el césped cuando está sano? | Verde | `2026-10-08-5.log.gz:1326 → 1332` |
| ¿Cómo se llama el astro rey que ilumina durante el día? | Sol | `2026-10-09-2.log.gz:2514 → 2519` |
| ¿Cuántas horas tiene un día? | Veinticuatro | `2026-10-09-3.log.gz:1743 → 1761` |
| ¿Qué animal tiene una melena y ruge fuerte? | León | `2026-10-13-1.log.gz:420 → 426` |
| ¿Qué elemento químico tiene el símbolo "H"? | Hidrógeno | `2026-10-17-3.log.gz:1449 → 1460` |
| ¿Cuál es el océano más grande del mundo? | Pacífico | `2026-10-17-4.log.gz:394 → 401` |
| ¿Cómo se llama el lugar donde se guarda el dinero? | Banco | `2026-10-17-4.log.gz:650 → 655` |
| ¿Qué instrumento se toca soplando y tiene teclas para cambiar las notas? | Flauta | `2026-10-18-1.log.gz:888 → 894` |
| ¿Cómo se llama el lugar donde vivimos? | Casa | `2026-10-20-2.log.gz:1933 → 1939` |

## Máscaras nuevas

| Reto | Respuesta del servidor | Archivo:línea de instrucción → resultado |
| --- | --- | --- |
| `Co_ín` | Cojín | `2026-10-02-1.log.gz:344 → 350` |
| `_ofá` | Sofá | `2026-10-02-3.log.gz:290 → 296` |
| `Ce_e_l` | Cereal | `2026-10-03-5.log.gz:848 → 853` |
| `_o_tina` | Cortina | `2026-10-03-5.log.gz:951 → 956` |
| `_u__to` | Huerto | `2026-10-03-6.log.gz:486 → 492` |
| `Es_e_o` | Espejo | `2026-10-03-6.log.gz:1803 → 1810` |
| `_g_je_o` | Agujero | `2026-10-03-6.log.gz:2180 → 2187` |
| `_o_i_a` | Cocina | `2026-10-03-6.log.gz:2351 → 2356` |
| `S_fá` | Sofá | `2026-10-04-2.log.gz:2401 → 2407` |
| `_eb_ja` | Rebaja | `2026-10-04-2.log.gz:869649 → 872342` |
| `Re_a_a` | Rebaja | `2026-10-04-2.log.gz:16903464 → 16905790` |
| `_iso` | Piso | `2026-10-04-3.log.gz:512 → 517` |
| `___g_dero` | Fregadero | `2026-10-04-3.log.gz:720 → 727` |
| `F_eg_dero` | Fregadero | `2026-10-04-3.log.gz:1140 → 1145` |
| `Cepi__o` | Cepillo | `2026-10-04-3.log.gz:1367 → 1372` |
| `_er__a` | Percha | `2026-10-04-4.log.gz:1049 → 1061` |
| `_e_t_na` | Ventana | `2026-10-04-6.log.gz:687 → 693` |
| `_c___icion_dor` | Acondicionador | `2026-10-04-6.log.gz:1496 → 1502` |
| `__ta_lo` | Establo | `2026-10-04-6.log.gz:1743 → 1757` |
| `_a_abo` | Lavabo | `2026-10-04-6.log.gz:2003 → 2013` |
| `N_bul__a` | Nebulosa | `2026-10-05-1.log.gz:515 → 520` |
| `Ex____ad_r` | Explorador | `2026-10-05-1.log.gz:769 → 774` |
| `Te__o` | Techo | `2026-10-05-2.log.gz:439 → 445` |
| `__s__ento` | Descuento | `2026-10-05-3.log.gz:442 → 451` |
| `Ja__n` | Jabón | `2026-10-05-3.log.gz:678 → 689` |
| `O_er_a` | Oferta | `2026-10-05-3.log.gz:3451 → 3472` |
| `C__rito` | Carrito | `2026-10-05-3.log.gz:5167 → 5176` |
| `_g_jero` | Agujero | `2026-10-05-3.log.gz:8335 → 8345` |
| `_ult__o` | Cultivo | `2026-10-05-4.log.gz:1783 → 1791` |
| `L_z` | Luz | `2026-10-06-2.log.gz:558 → 564` |
| `_eca_or` | Secador | `2026-10-08-1.log.gz:641 → 646` |
| `_a_lo` | Gallo | `2026-10-08-2.log.gz:562 → 568` |
| `__tre_la` | Estrella | `2026-10-08-3.log.gz:592 → 598` |
| `__deral` | Sideral | `2026-10-08-5.log.gz:508 → 517` |
| `G__axia` | Galaxia | `2026-10-08-5.log.gz:656 → 661` |
| `A__ósfera` | Atmósfera | `2026-10-09-2.log.gz:357 → 362` |
| `_fer_a` | Oferta | `2026-10-09-2.log.gz:881 → 886` |
| `_u_a` | Luna | `2026-10-09-2.log.gz:1297 → 1302` |
| `__j_ro` | Cajero | `2026-10-09-2.log.gz:1476 → 1484` |
| `Sat__no` | Saturno | `2026-10-09-2.log.gz:1767 → 1773` |
| `_até__te` | Satélite | `2026-10-09-2.log.gz:2705 → 2712` |
| `_os__ha` | Cosecha | `2026-10-09-3.log.gz:298 → 304` |
| `C_i__te` | Cliente | `2026-10-13-3.log.gz:882 → 887` |
| `_e_ta_a` | Ventana | `2026-10-17-3.log.gz:1144 → 1152` |
| `A___sf_ra` | Atmósfera | `2026-10-18-1.log.gz:1023 → 1033` |
| `S__er_l` | Sideral | `2026-10-18-1.log.gz:2314 → 2324` |
| `Sec_d_r` | Secador | `2026-10-18-2.log.gz:727 → 732` |
| `Es_u_a` | Estufa | `2026-10-18-2.log.gz:916 → 925` |
| `_uer_o` | Huerto | `2026-10-18-2.log.gz:1261 → 1267` |
| `_a__ín` | Jardín | `2026-10-18-3.log.gz:542 → 550` |
| `_rbi_a` | Órbita | `2026-10-18-3.log.gz:2125 → 2130` |
| `__bu_osa` | Nebulosa | `2026-10-18-3.log.gz:2626 → 2631` |
| `__all_ro` | Toallero | `2026-10-19-3.log.gz:965 → 971` |
| `__an_ro` | Granero | `2026-10-20-1.log.gz:299 → 311` |
| `__r_ina` | Cortina | `2026-10-20-1.log.gz:342 → 347` |
| `V_r_ice` | Vórtice | `2026-10-20-2.log.gz:799 → 810` |
| `La_a_o` | Lavabo | `2026-10-20-2.log.gz:1730 → 1749` |

## Anagramas nuevos

| Reto | Respuesta del servidor | Archivo:línea de instrucción → resultado |
| --- | --- | --- |
| `aocniC` | Cocina | `2026-10-02-1.log.gz:452 → 458` |
| `oetehC` | Cohete | `2026-10-02-2.log.gz:797 → 802` |
| `rCdeo` | Cerdo | `2026-10-03-3.log.gz:365 → 371` |
| `paoCm` | Campo | `2026-10-04-2.log.gz:5461582 → 5478790` |
| `caaV` | Vaca | `2026-10-04-2.log.gz:7085024 → 7086682` |
| `eRoicb` | Recibo | `2026-10-04-2.log.gz:16716060 → 16717264` |
| `xaGaila` | Galaxia | `2026-10-04-2.log.gz:24897003 → 24914706` |
| `aoelTorl` | Toallero | `2026-10-04-6.log.gz:947 → 952` |
| `eElpcis` | Eclipse | `2026-10-05-1.log.gz:574 → 579` |
| `ircotPó` | Pórtico | `2026-10-05-1.log.gz:688 → 693` |
| `sMae` | Mesa | `2026-10-05-3.log.gz:568 → 576` |
| `arieTr` | Tierra | `2026-10-05-3.log.gz:1163 → 1169` |
| `uetoHr` | Huerto | `2026-10-05-3.log.gz:1836 → 1841` |
| `aióiRndac` | Radiación | `2026-10-05-3.log.gz:2393 → 2399` |
| `aBnlcó` | Balcón | `2026-10-05-3.log.gz:2737 → 2743` |
| `Plaa` | Pala | `2026-10-05-3.log.gz:7506 → 7517` |
| `etCeoh` | Cohete | `2026-10-06-1.log.gz:486 → 494` |
| `alGaaix` | Galaxia | `2026-10-06-1.log.gz:570 → 576` |
| `epjosE` | Espejo | `2026-10-06-1.log.gz:595 → 603` |
| `nalooCi` | Colonia | `2026-10-06-1.log.gz:628 → 634` |
| `Eutfas` | Estufa | `2026-10-06-1.log.gz:665 → 670` |
| `reaFogder` | Fregadero | `2026-10-07-1.log.gz:555 → 561` |
| `tasEnte` | Estante | `2026-10-07-4.log.gz:447 → 454` |
| `nbóaJ` | Jabón | `2026-10-08-5.log.gz:1019 → 1026` |
| `osmrCpa` | Compras | `2026-10-09-2.log.gz:2623 → 2628` |
| `oecneDust` | Descuento | `2026-10-09-3.log.gz:602 → 629` |
| `taribÓ` | Órbita | `2026-10-09-3.log.gz:779 → 786` |
| `cbReoi` | Recibo | `2026-10-09-4.log.gz:556 → 564` |
| `lzaarotHi` | Hortaliza | `2026-10-09-4.log.gz:1357 → 1362` |
| `aeriTr` | Tierra | `2026-10-10-1.log.gz:445 → 450` |
| `aidónRica` | Radiación | `2026-10-13-3.log.gz:1047 → 1053` |
| `etlraslE` | Estrella | `2026-10-16-1.log.gz:764 → 771` |
| `seMa` | Mesa | `2026-10-17-3.log.gz:724 → 730` |
| `Tiogr` | Trigo | `2026-10-17-3.log.gz:1741 → 1749` |
| `Hvuoe` | Huevo | `2026-10-17-3.log.gz:1954 → 1959` |
| `rsalcaEe` | Escalera | `2026-10-17-4.log.gz:1508 → 1514` |
| `raeNev` | Nevera | `2026-10-17-4.log.gz:1736 → 1742` |
| `tCeleni` | Cliente | `2026-10-17-4.log.gz:2158 → 2170` |
| `erinsoUv` | Universo | `2026-10-18-1.log.gz:1416 → 1421` |
| `lToala` | Toalla | `2026-10-18-1.log.gz:2750 → 2758` |
| `loPo` | Polo | `2026-10-18-2.log.gz:1073 → 1078` |
| `atetlSié` | Satélite | `2026-10-18-3.log.gz:3072 → 3078` |
| `Cohtee` | Cohete | `2026-10-18-3.log.gz:3243 → 3248` |
| `Eftsau` | Estufa | `2026-10-18-3.log.gz:3727 → 3732` |
| `pamoC` | Campo | `2026-10-18-3.log.gz:4446 → 4452` |
| `iCleo` | Cielo | `2026-10-18-3.log.gz:4801 → 4807` |
| `tOefra` | Oferta | `2026-10-20-1.log.gz:923 → 928` |
| `Hberai` | Hierba | `2026-10-20-2.log.gz:571 → 576` |
| `orinUsev` | Universo | `2026-10-20-2.log.gz:1267 → 1275` |
| `oabLav` | Lavabo | `2026-10-20-2.log.gz:1390 → 1395` |
| `nioarCt` | Cortina | `2026-10-20-2.log.gz:1643 → 1648` |
| `alaixaG` | Galaxia | `2026-10-20-2.log.gz:2252 → 2258` |

## Vocabulario nuevo

Cabra, Oveja, Vaca, Recibo, Piso, Percha, Ducha, Acondicionador, Jabón, Champú, Carrito, Estufa, Atmósfera, Puerta, Pasto, Estación, Trigo, Vórtice, Cielo.

## Disponibilidad al actualizar

Las respuestas de anagramas incluidas en el mod se consultan también cuando ya existe una configuración personal. El vocabulario canónico del servidor se carga como respaldo de los diccionarios personales para resolver otras permutaciones. Las respuestas aprendidas y los diccionarios personales conservan su prioridad.

## Verificación de la actualización

`./gradlew build` completó la compilación y las 65 pruebas JUnit, sin fallos, errores ni pruebas omitidas. El corpus de octubre se comprobó tanto con una configuración nueva como con una configuración existente de revisión 9, incluidos los controles de activación, los tiempos y los datos personales. Se verificó que el JAR instalable de la versión 1.2.1 contiene los cuatro recursos de respuestas actualizados.
