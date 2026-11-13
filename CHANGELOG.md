# Registro de cambios

## 1.10.3

- Animación de inicio: al abrir la app, cuatro orbes con los colores del icono (rojo, amarillo, verde y azul) brotan del centro, esperan respirando, giran un cuarto de vuelta y se encogen hasta desaparecer, y entonces aparece la galería. Es la misma idea que la del vídeo de referencia, con las formas en las posiciones del icono (rojo arriba, amarillo a la derecha, verde abajo y azul a la izquierda). Un toque la salta. Se activa y desactiva en Ajustes → Apariencia ("Animación de inicio") y no se muestra si las animaciones están apagadas.
- Cristal acrílico: el estilo Acrílico se ve ahora igual que en la captura de referencia, con Liquid Glass o sin él (ese estilo trae su propio cristal líquido, así que el interruptor queda fijo cuando está elegido) y también en Android 12, con el mismo radio fino de desenfoque. Medido sobre la captura, el tinte blanco, el brillo superior y el borde coinciden con los valores del estilo.
- Acrílico por defecto: es el estilo de serie en instalaciones nuevas y se aplica una sola vez a quien ya tenía la app; después se puede cambiar en Ajustes → Apariencia → "Desenfoque de la interfaz".

## 1.9.4

- Editor de fotos: nuevo botón con un lápiz en el visor (solo para fotos que no sean animadas ni estén en la papelera). Permite recortar con la mano o con proporciones (libre, original, 1:1, 4:3, 3:4, 16:9, 9:16), girar, voltear en espejo, ajustar brillo, contraste, saturación y calidez, y aplicar filtros (Vívido, Frío, Cálido, B/N, Sepia, Desvaído, Drama). El original no se toca: se guarda una copia junto a él (o en Pictures/Pixel Gallery si esa carpeta no admite fotos nuevas). Las fotos PNG se guardan como PNG y el resto como JPEG; el tamaño máximo guardado es de 4096 px por lado.
- Fotos animadas: las miniaturas ya no muestran la palabra GIF, solo un icono de foto animada.
- Fondo de la cuadrícula de fotos, videos y álbumes: ahora son tres luces de aurora con los colores de las fotos que se ven (arriba, en medio y abajo), que cambian con suavidad al desplazar, más un velo del color del tema para leer bien. Funciona en cualquier versión de Android; desde Android 12 se suma la primera foto difuminada. Se sigue activando y desactivando en Ajustes, en Apariencia.
- Visor: los botones de la barra superior miden 44 dp para que quepa el del editor.
- Android 9 o menor: se declara el permiso de escritura (solo hasta la API 28) para poder guardar la copia editada.

## 1.9.1

- Barra de filtros (Todo, Fotos, Videos): ahora va centrada por defecto y se ajusta desde Ajustes, en el nuevo grupo "Barra de filtros": tamaño, ancho (al mínimo es Auto), distancia superior y posición (izquierda, centro o derecha). Incluye vista previa en vivo que se queda fija arriba mientras mueves los controles.
- Fondo difuminado: detrás de la cuadrícula de fotos y de álbumes aparece la foto de arriba convertida en un fondo borroso que cambia al desplazar. Se activa y desactiva desde Ajustes, en Apariencia ("Fondo difuminado"). Requiere Android 12 o superior.
- Reproductor: los dos paneles (progreso y botones) se ajustan desde Ajustes, en el grupo Reproductor: grosor, tamaño de los controles, ancho, distancia al borde inferior y posición (izquierda, centro o derecha). El ancho, la distancia y la posición se aplican con el panel flotante; grosor y tamaño valen para los dos modos. Incluye vista previa con los mismos paneles que el reproductor real.
- Código: los paneles del reproductor viven ahora en un solo composable compartido (PlayerPanels), así que la vista previa y el reproductor real no pueden diferir.

## 1.8.9

- Fotos: el encabezado ya no dice "Fotos". Ahora es una barra fina y compacta con solo los filtros (Todo, Fotos, Videos) y se queda fija mientras desplazas la galería, sin esconderse. En Álbumes la barra lleva solo el nombre de la sección.
- Ajustes: la sección "Barra de navegación" muestra una vista previa en vivo con el estilo, el alto, el ancho, la distancia al borde, el tamaño de los iconos, el redondeo y el desenfoque que tengas aplicados. La vista previa se queda fija arriba mientras mueves los controles.
- Reproductor: la barra de progreso y los botones (velocidad, pausa, encuadre, volumen) ahora van en dos paneles de cristal separados, más finos y compactos. Adelantar y atrasar salta 5 segundos en lugar de 10, también con doble toque en los lados del video.
- Rendimiento: la cuadrícula de fotos se desplaza y carga con menos lag. Las miniaturas ya no registran su posición en cada fotograma de scroll, solo las nuevas crean animación de entrada, la agrupación por días y las etiquetas de fecha se calculan una sola vez, la precarga espera a que el scroll se calme y no repite lo que ya se ve, y la carga de miniaturas se limita y se cancela al salir de pantalla. Una recarga que no trae cambios ya no vuelve a pintar toda la galería. Todos los desenfoques y opciones se mantienen igual.

## 1.7.4

- Iconos: se suman "Pixel Art" (la forma del icono por defecto en pixeles) y "Pixel Style" (la forma del icono por defecto con colores rojo, amarillo, verde y azul, más brillantes).

## 1.7.3

- Fotos animadas: los GIF y los WebP animados (y AVIF animado desde Android 12) se reproducen en el visor. Los GIF llevan una insignia en la cuadrícula.
- Ajustes: nuevo selector de iconos de la app con vista previa real. Se suman 8 variantes del icono original (Contorno, Pastel, Cristal, Mono, Atardecer, Sakura, Neón y Plano) a los dos que ya existían.
- Animaciones: las miniaturas entran con un efecto escalonado, se hunden al tocarlas y se reacomodan al cambiar de filtro. Los álbumes y los grupos de Ajustes entran en cascada.
- Tirar de la lista hacia abajo recarga la galería con un indicador animado. El encabezado flotante se esconde al bajar y vuelve al subir.
- Visor: la foto o el video se abre con un velo y un movimiento más vivo, y al deslizar entre archivos el que sale se aleja y se atenúa.

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
