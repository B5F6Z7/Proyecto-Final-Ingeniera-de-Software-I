# CampusFest · Proyecto Final Ingeniería de Software I

Aplicación de escritorio en **JavaFX 21** con persistencia en **H2** para reservar y controlar
el ingreso a los eventos de CampusFest. Implementa los cuatro casos de uso diseñados en clase
siguiendo la arquitectura limpia, los principios de diseño y los patrones GRASP.

## Integrantes

| Nombre | Usuario de GitHub | Aporte principal |
| --- | --- | --- |
| Daniel Samboni | DanielSamboni | Controladores de interfaz gráfica y revisión de código |
| Julian Baez | P4trick_B4tem4n | Testing y revision del contrato |
| Juan Pablo Torres | jupatorres-pixel | Documentación de una guía con cinco escenarios de prueba manual para los cuatro casos de uso de CampusFest. |
| _(completar)_ | _(completar)_ | _(completar)_ |

## Requisitos

- JDK 21
- Maven 3.9 o superior (descarga JavaFX 21.0.5, H2 2.2.224 y JUnit 5.10.2)

## Cómo ejecutar

```bash
# Compilar
mvn compile

# Correr las 90 pruebas entregadas por los docentes
mvn test

# Abrir la aplicación
mvn javafx:run
```

También se puede ejecutar la clase `co.edu.unillanos.campusfest.Launcher` desde el IDE.

La base de datos H2 se crea en `./datos/campusfest.mv.db` la primera vez que se abre la
aplicación, con dos eventos de ejemplo:

| Id | Evento | Aforo | Bloqueados |
| --- | --- | --- | --- |
| `ev-1` | Concierto de bienvenida | 120 | 5 |
| `ev-2` | Feria de emprendimiento | 3 | 1 |

`ev-2` tiene solo 2 cupos libres, para probar rápido el flujo «sin cupos».

## Casos de uso implementados

| CU | Actor | Caso de uso (clase) | Pantalla | Flujos alternativos cubiertos |
| --- | --- | --- | --- | --- |
| CU1 · Consultar cupos | Estudiante | `ConsultarCupos.consultar(eventoId)` | `pantalla-cupos.fxml` | Evento inexistente |
| CU2 · Reservar y confirmar | Estudiante | `ReservarEntrada.solicitarReserva(...)`, `confirmarReserva(...)` | `pantalla-reserva.fxml` | Sin cupos, reserva vencida (10 min), código de otro evento |
| CU3 · Editar evento | Organizador | `EditarEvento.obtener(...)`, `editar(...)` | `pantalla-evento.fxml` | Aforo menor que los ocupados, datos vacíos |
| CU4 · Registrar ingreso con QR | Encargado de la entrada | `RegistrarIngreso.registrar(eventoId, codigo)` | `pantalla-ingreso.fxml` | Código de otro evento, reserva no confirmada, ingreso repetido |

## Guía de pruebas manuales

Abre CampusFest con `mvn javafx:run`. Para reproducir los valores de esta guía,
parte de los datos de ejemplo. El evento `ev-2` comienza con aforo de 3 personas,
1 cupo bloqueado y 2 cupos libres. Como H2 conserva los cambios entre ejecuciones,
consulta primero el estado del evento si ya se usó en otras pruebas.

| Escenario | Pasos | Resultado esperado |
| --- | --- | --- |
| **Consultar cupos (CU1)** | Abre **Consultar cupos**, escribe `ev-2` y pulsa **Consultar**. Repite con `ev-999`. | Con los datos de ejemplo, `ev-2` muestra 2 cupos libres. Para `ev-999`, la aplicación informa que el evento no existe. |
| **Intentar ingresar con una reserva pendiente (CU2 y CU4)** | En CU2, solicita una reserva para `ev-1` con un código de estudiante de prueba, por ejemplo `160004512`. Copia el código de reserva sin confirmarla. En CU4, ingresa `ev-1` y ese código, y pulsa **Registrar ingreso**. | El ingreso se rechaza porque la reserva todavía está pendiente de confirmación. |
| **Registrar un ingreso y comprobar el rechazo de duplicados (CU2 y CU4)** | Vuelve a CU2 y confirma la reserva anterior antes de la hora límite que muestra la aplicación. Si ya venció, solicita y confirma una nueva. En CU4, registra el ingreso para `ev-1` con el código confirmado y luego repite la operación con el mismo código. | El primer ingreso se registra correctamente y el segundo se rechaza porque el código ya se utilizó. |
| **Agotar los cupos disponibles (CU1 y CU2)** | Consulta los cupos de `ev-2`. Con los datos de ejemplo, solicita y confirma dos reservas, usando un código de estudiante distinto para cada una. Vuelve a consultar los cupos e intenta solicitar una tercera reserva. | La consulta muestra 0 cupos libres y la tercera solicitud se rechaza. |
| **Reducir el aforo por debajo del mínimo (CU3)** | Después de agotar los cupos de `ev-2`, abre **Editar evento**, escribe `ev-2` y pulsa **Cargar evento**. Cambia el aforo de `3` a `2`, pulsa **Guardar cambios** y vuelve a pulsar **Cargar evento**. | La aplicación rechaza el cambio porque hay 2 cupos reservados y 1 bloqueado. Al cargar de nuevo el evento, el aforo sigue siendo 3. |

Al ejecutar cada escenario, registra el resultado observado y compáralo con el
esperado. Si hay una diferencia, anota el paso en que ocurrió y el mensaje que
mostró la aplicación.


## Arquitectura

```
co.edu.unillanos.campusfest
├── App, Launcher                     ← Frameworks y drivers (ensamblaje)
├── domain                            ← Entidades: Evento, Reserva, EstadoReserva, Asistencia
├── application
│   ├── port                          ← Puertos: RepositorioEventos, Reloj
│   └── usecase                       ← ConsultarCupos, ReservarEntrada, EditarEvento, RegistrarIngreso
└── infrastructure
    ├── persistence                   ← RepositorioEventosEnMemoria, RepositorioEventosH2
    ├── reloj                         ← RelojDelSistema (la única clase que llama a now())
    └── ui                            ← Controladores de las cuatro pantallas y VentanaPrincipal (menú lateral)

src/main/resources
├── co/edu/unillanos/campusfest/vistas/   ← 4 FXML + campusfest.css
└── db/schema.sql                         ← Tablas EVENTO, RESERVA, ASISTENCIA con PK y FK
```

Decisiones de diseño:

- **Experto en información**: `Evento.cuposDisponibles` cuenta las reservas con
  `Reserva.ocupaCupo`; solo `Reserva` mira su propio estado y su plazo.
- **Creador**: `Evento.reservar` crea la `Reserva` y `Reserva.registrarIngreso` crea la `Asistencia`.
- **Controlador**: los casos de uso solo buscan el evento, piden la hora al `Reloj`, delegan en la
  entidad y guardan. La búsqueda «evento o error» vive en un solo lugar (`BusquedaDeEvento`).
- **Variaciones protegidas / Indirección**: la hora llega por el puerto `Reloj`; en las pruebas se usa `RelojFijo`.
- **Fabricación pura**: `RepositorioEventosH2` y `RepositorioEventosEnMemoria` guardan y buscan;
  las entidades no saben cómo se persisten. Todo `SQLException` sale como `ErrorDePersistencia`
  (subclase de `IllegalStateException`).
- **Regla de dependencia**: `domain` no importa nada de otros anillos; `application` solo importa
  `domain` y sus puertos; nada de adentro importa `javafx.*`, `java.sql.*` ni H2.
- `App` es la única clase que crea `RepositorioEventosH2` y `RelojDelSistema`. El paso de la
  versión en memoria a H2 solo cambió `App` (ver el commit
  «Cambiar App para usar RepositorioEventosH2…»).

## Declaración de uso de IA

**Herramienta:** Claude Code (modelo Claude Opus 5.5, de Anthropic).

**Para qué se usó:**

- Leer el contrato (`CONTRATO.md`), las pruebas entregadas y el resumen de análisis y diseño, y
  proponer la implementación de las clases que hacen pasar las pruebas.
- Generar el código de las entidades (`Evento`, `Reserva`, `EstadoReserva`, `Asistencia`), los
  puertos, los cuatro casos de uso, los adaptadores (`RepositorioEventosEnMemoria`,
  `RepositorioEventosH2`, `RelojDelSistema`), los controladores, `VentanaPrincipal`, las vistas FXML, el CSS (incluido el rediseño visual),
  `schema.sql`, `App`, `Launcher` y este README.
- Ejecutar `mvn test` y corregir los errores encontrados.

El código generado se revisó contra los aspectos de evaluación (reglas en las entidades,
regla de dependencia, controladores sin reglas del negocio, sin `now()` fuera de `RelojDelSistema`).

**Propuestas de la IA que violaban el diseño y cómo se corrigieron:**

1. *Reglas del negocio escritas en las vistas FXML (señal de alerta «reglas dentro de un FXML»).*
   Al rediseñar la interfaz (PR «Rediseño de la interfaz JavaFX»), la IA escribió en los FXML
   textos que repetían las reglas: el plazo de «10 minutos» en `pantalla-reserva.fxml`, la fórmula
   «aforo menos bloqueados, confirmadas y pendientes vigentes» en `pantalla-cupos.fxml`, el aforo
   mínimo en `pantalla-evento.fxml` y el ingreso «una sola vez» en `pantalla-ingreso.fxml`. Esas
   reglas viven en `Evento` y `Reserva`; si el plazo cambiara, la pantalla diría algo falso. Se
   corrigió en el commit «Quitar de las vistas FXML los textos que repetían reglas del negocio»:
   las vistas ahora solo muestran el resultado o el mensaje que devuelve el caso de uso
   (por ejemplo, la hora límite de la reserva sale de `Reserva.getVenceEn()`).

Las dos correcciones siguientes se hicieron mientras se escribía el código, antes del primer
commit, por eso no tienen un commit propio en el historial:

2. *Regla repetida en `Evento` (principio «cada regla en un solo lugar»).* La primera versión de
   `Evento.cambiarAforo` calculaba el aforo mínimo por su cuenta
   (`int minimo = bloqueados + cuposOcupadosPorReservas(ahora)`), repitiendo la fórmula que ya
   estaba en el método privado `aforoMinimo`. Si la regla cambiaba, había que cambiarla en dos
   sitios. Se corrigió para que `cambiarAforo` llame a `aforoMinimo(ahora)` y solo derive de ahí
   los cupos ocupados que necesita para el mensaje de error.
3. *Error en el adaptador H2.* La primera versión de `schema.sql` tenía un `;` dentro de un
   comentario y `RepositorioEventosH2` dividía el script por `;` sin quitar los comentarios, así que
   las 16 pruebas de H2 fallaban al crear el esquema. Se corrigió en el adaptador (método
   `sinComentarios`), sin tocar las pruebas.
   Revisado por Julian Baez

Revisado por Daniel Samboni.



