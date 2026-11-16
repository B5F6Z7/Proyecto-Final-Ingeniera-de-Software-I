# CampusFest · Proyecto Final Ingeniería de Software I

Aplicación de escritorio en **JavaFX 21** con persistencia en **H2** para reservar y controlar
el ingreso a los eventos de CampusFest. Implementa los cuatro casos de uso diseñados en clase
siguiendo la arquitectura limpia, los principios de diseño y los patrones GRASP.

## Integrantes

| Nombre | Usuario de GitHub | Aporte principal |
| --- | --- | --- |
| _(completar)_ | _(completar)_ | _(completar)_ |
| _(completar)_ | _(completar)_ | _(completar)_ |
| _(completar)_ | _(completar)_ | _(completar)_ |
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

Flujo de prueba sugerido: en CU2 solicite una reserva en `ev-1` (el código queda en el
formulario) y confírmela; copie el código, péguelo en CU4 y registre el ingreso; intente
registrarlo otra vez para ver el rechazo.

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
    └── ui                            ← Controladores JavaFX de las cuatro pantallas

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
  `RepositorioEventosH2`, `RelojDelSistema`), los controladores, las vistas FXML, el CSS,
  `schema.sql`, `App`, `Launcher` y este README.
- Ejecutar `mvn test` y corregir los errores encontrados.

El código generado se revisó contra los aspectos de evaluación (reglas en las entidades,
regla de dependencia, controladores sin reglas del negocio, sin `now()` fuera de `RelojDelSistema`).

**Propuestas de la IA que violaban el diseño y cómo se corrigieron:**

1. *Regla repetida en `Evento` (principio «cada regla en un solo lugar»).* La primera versión de
   `Evento.cambiarAforo` calculaba el aforo mínimo por su cuenta
   (`int minimo = bloqueados + cuposOcupadosPorReservas(ahora)`), repitiendo la fórmula que ya
   estaba en el método privado `aforoMinimo`. Si la regla cambiaba, había que cambiarla en dos
   sitios. Se corrigió para que `cambiarAforo` llame a `aforoMinimo(ahora)` y solo derive de ahí
   los cupos ocupados que necesita para el mensaje de error.
2. *Error en el adaptador H2.* La primera versión de `schema.sql` tenía un `;` dentro de un
   comentario y `RepositorioEventosH2` dividía el script por `;` sin quitar los comentarios, así que
   las 16 pruebas de H2 fallaban al crear el esquema. Se corrigió en el adaptador (método
   `sinComentarios`), sin tocar las pruebas.
