# Registro de cambios

## 1.6.2

- Los comentarios del código ahora son frases de anime y mensajes ocultos (base64 y rot13). ¡A buscarlos!
- Las descripciones de Ajustes y de la bienvenida ahora usan frases y nombres de anime (Gojo, Tanjiro, Sailor Moon, Totoro...).
- Visor: el texto y los iconos de la barra superior y del panel del reproductor cambian entre oscuro y claro según el contenido que hay detrás, para que siempre se lean.
- Visor: el video se centra entre la barra superior y el panel de controles, con el mismo hueco arriba y abajo, y ya no se mete por debajo del panel.
- Fotos y Álbumes: el título y los filtros ahora van en un encabezado flotante de cristal, con filtros en píldoras totalmente redondeadas e iconos circulares. El contenido pasa por detrás con desenfoque.
- Reproductor: la barra superior y el panel de controles flotan encima en cristal. Nuevo botón para ajustar el video entero o rellenar toda la pantalla.
- Desenfoque Acrílico: refracción moderada y sin arcoíris (casi sin dispersión cromática), con menos desenfoque y velo para que se vea el fondo. Sigue refractando aunque Liquid Glass esté apagado, y ahora también se nota en el panel del reproductor.
- Los videos se reproducen con las esquinas redondeadas.
- Barra de navegación: sin fondo, solo desenfoque y refracción. Ajustes y Papelera pasan por detrás de la barra en lugar de cortarse encima.

## 1.4.3

- Desenfoque Acrílico: ahora es un cristal esmerilado claro que se ve también sobre fondos oscuros (Ajustes, reproductor), no solo sobre fotos. Escarcha recibe un toque más suave del mismo efecto.
- El panel del reproductor usa el mismo cristal acrílico que la barra de navegación y tiene un poco más de aire entre filas.

## 1.4.2

- Barras de navegación más compactas, y todas se pueden arrastrar con el dedo para cambiar de pestaña.
- Nueva sección "Barra de navegación" en Ajustes: estilo y calibración del alto, ancho, distancia al borde inferior, tamaño de los iconos y redondeo, con botón para restablecer.
- El globito con el número de la papelera ya no se recorta ni descentra el icono.
- Reproductor más compacto: tiempo, barra y duración en una sola fila, botones más pequeños y más espacio para el video.
- El panel flotante del reproductor usa el mismo cristal que la barra de navegación y ya no muestra un rectángulo oscuro por dentro.

## 1.4.1

- Barra de navegación: 5 estilos nuevos (TikTok, Completa, Isla, Indicador y Burbuja) además de Clásica y Apilada. La barra TikTok es una píldora oscura de cristal con globito rojo en la papelera; si el desenfoque estaba apagado, al elegirla se activa el tipo Oscuro.
- Desenfoque: 7 tipos a elegir (Suave, Fuerte, Oscuro, Escarcha, Vibrante, Humo y Acrílico), que se combinan con el interruptor de Liquid Glass.
- Reproductor: nueva opción "Controles del reproductor" para elegir entre el panel pegado abajo (por defecto) y uno flotante.
- Actualizaciones: la ventana ya no muestra "null" cuando la versión publicada no trae notas; las notas de cada release se toman de este registro de cambios.

## 1.3.0

- Barra de navegación: dos estilos que se eligen en Ajustes > Apariencia, la clásica (icono y nombre en la píldora activa) y la apilada (icono sobre el nombre). Las dos son más compactas, muestran una insignia con los elementos de la papelera y se pueden recorrer arrastrando el dedo.
- Liquid Glass: interruptor en Ajustes para activar o desactivar el cristal transparente, con el fondo más vivo y reflejos de luz en los bordes. En Android 13 o superior las barras refractan la luz de verdad (shader AGSL con dispersión cromática en el borde). Apagado vuelve al desenfoque clásico.
- Interfaz más compacta: barras de navegación, filtros, encabezados de día y filas de Ajustes ocupan menos espacio.

## 1.2.0

- Idiomas: la app está disponible en español, inglés, portugués, francés, alemán, italiano, ruso, japonés y chino simplificado.
- Se puede cambiar el idioma en Ajustes > Idioma (y en la pantalla de bienvenida). Por defecto sigue el idioma del sistema.
- Las fechas, los plurales y los mensajes de error de las actualizaciones también se adaptan al idioma elegido.

## 1.1.1

- Álbumes: ya se abren todos (algunos tenían un identificador negativo y la app los ignoraba).
- Animaciones: el visor vuelve a crecer desde la miniatura y a regresar a ella al cerrarse.
- Barra de navegación: ya no deja pasar toques a las fotos o álbumes que hay detrás.
- Actualizaciones: usa la red activa y explica la causa real cuando falla (firewall, VPN, tiempo agotado...).

## 1.1.0 — Estable

- Actualizaciones OTA: la app busca versiones nuevas en GitHub Releases, las descarga y lanza el instalador (Ajustes > Actualizaciones).
- "Acerca de": foto de perfil de GitHub sincronizada y logo del canal de Telegram.
- Animaciones: el visor crece desde la miniatura al abrirse y vuelve a ella al cerrarse; los álbumes, "Acerca de" y la barra de selección entran y salen con transiciones suaves; las miniaturas aparecen con un fundido.
- El proyecto pasa a llamarse Pixel-Gallery-You.
- Ahora la app usa internet (solo para las actualizaciones, la foto de GitHub y el logo del canal).

## 1.0.0 — Primera versión estable

- Galería de fotos y videos con cuadrícula fluida, pellizcar para cambiar columnas y precarga de miniaturas.
- Álbumes, papelera con restauración y borrado definitivo, mover fotos entre álbumes.
- Visor de fotos con zoom y de videos con reproductor propio (velocidad, saltos de 10 s, volumen).
- Deslizar hacia abajo en una foto o video para cerrarlo.
- Barra de navegación flotante de cristal.
- Desenfoque estilo cristal líquido (Android 12+) en barras y visor.
- Tema claro, oscuro o del sistema, colores dinámicos y icono Material You.
- Pantalla "Acerca de" con el mantenedor, GitHub y los canales de Telegram.
- Bienvenida de primer uso, set de iconos propio y retroceso predictivo.
