package co.edu.unillanos.campusfest.infrastructure.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Asistencia;
import co.edu.unillanos.campusfest.domain.EstadoReserva;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.domain.Reserva;

/**
 * Adaptador H2 del puerto RepositorioEventos (Fabricación pura).
 * Las entidades no saben que se guardan aquí. Todo SQLException se traduce
 * a ErrorDePersistencia (una IllegalStateException).
 */
public class RepositorioEventosH2 implements RepositorioEventos {

    private static final String SCRIPT_ESQUEMA = "/db/schema.sql";

    private static final String BUSCAR_EVENTO =
            "SELECT ID, NOMBRE, FECHA, LUGAR, AFORO, BLOQUEADOS FROM EVENTO WHERE ID = ?";
    private static final String BUSCAR_RESERVAS =
            "SELECT R.CODIGO, R.ESTUDIANTE_ID, R.VENCE_EN, R.ESTADO, A.HORA "
            + "FROM RESERVA R LEFT JOIN ASISTENCIA A ON A.RESERVA_CODIGO = R.CODIGO "
            + "WHERE R.EVENTO_ID = ? ORDER BY R.VENCE_EN, R.CODIGO";
    private static final String GUARDAR_EVENTO =
            "MERGE INTO EVENTO (ID, NOMBRE, FECHA, LUGAR, AFORO, BLOQUEADOS) KEY (ID) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String GUARDAR_RESERVA =
            "MERGE INTO RESERVA (CODIGO, EVENTO_ID, ESTUDIANTE_ID, VENCE_EN, ESTADO) KEY (CODIGO) "
            + "VALUES (?, ?, ?, ?, ?)";
    private static final String GUARDAR_ASISTENCIA =
            "MERGE INTO ASISTENCIA (RESERVA_CODIGO, HORA) KEY (RESERVA_CODIGO) VALUES (?, ?)";

    private final Connection conexion;

    public RepositorioEventosH2(Connection conexion) {
        this.conexion = conexion;
        crearEsquema();
    }

    @Override
    public Evento buscar(String eventoId) {
        try (PreparedStatement consulta = conexion.prepareStatement(BUSCAR_EVENTO)) {
            consulta.setString(1, eventoId);
            try (ResultSet fila = consulta.executeQuery()) {
                if (!fila.next()) {
                    return null;
                }
                return new Evento(fila.getString("ID"), fila.getString("NOMBRE"),
                        fila.getObject("FECHA", LocalDateTime.class), fila.getString("LUGAR"),
                        fila.getInt("AFORO"), fila.getInt("BLOQUEADOS"), buscarReservas(eventoId));
            }
        } catch (SQLException e) {
            throw new ErrorDePersistencia("No se pudo leer el evento " + eventoId
                    + " de la base de datos.", e);
        }
    }

    @Override
    public void guardar(Evento evento) {
        try {
            conexion.setAutoCommit(false);
            guardarEvento(evento);
            for (Reserva reserva : evento.getReservas()) {
                guardarReserva(evento.getId(), reserva);
            }
            conexion.commit();
        } catch (SQLException e) {
            deshacer();
            throw new ErrorDePersistencia("No se pudo guardar el evento " + evento.getId()
                    + " en la base de datos.", e);
        } finally {
            volverAAutoCommit();
        }
    }

    private List<Reserva> buscarReservas(String eventoId) throws SQLException {
        List<Reserva> reservas = new ArrayList<>();
        try (PreparedStatement consulta = conexion.prepareStatement(BUSCAR_RESERVAS)) {
            consulta.setString(1, eventoId);
            try (ResultSet fila = consulta.executeQuery()) {
                while (fila.next()) {
                    reservas.add(leerReserva(fila));
                }
            }
        }
        return reservas;
    }

    private Reserva leerReserva(ResultSet fila) throws SQLException {
        LocalDateTime horaIngreso = fila.getObject("HORA", LocalDateTime.class);
        Asistencia asistencia = null;
        if (horaIngreso != null) {
            asistencia = new Asistencia(horaIngreso);
        }
        return Reserva.reconstruir(fila.getString("CODIGO"), fila.getString("ESTUDIANTE_ID"),
                fila.getObject("VENCE_EN", LocalDateTime.class),
                EstadoReserva.valueOf(fila.getString("ESTADO")), asistencia);
    }

    private void guardarEvento(Evento evento) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(GUARDAR_EVENTO)) {
            sentencia.setString(1, evento.getId());
            sentencia.setString(2, evento.getNombre());
            sentencia.setObject(3, evento.getFecha());
            sentencia.setString(4, evento.getLugar());
            sentencia.setInt(5, evento.getAforo());
            sentencia.setInt(6, evento.getBloqueados());
            sentencia.executeUpdate();
        }
    }

    private void guardarReserva(String eventoId, Reserva reserva) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(GUARDAR_RESERVA)) {
            sentencia.setString(1, reserva.getCodigo());
            sentencia.setString(2, eventoId);
            sentencia.setString(3, reserva.getEstudianteId());
            sentencia.setObject(4, reserva.getVenceEn());
            sentencia.setString(5, reserva.getEstado().name());
            sentencia.executeUpdate();
        }
        if (reserva.getAsistencia() != null) {
            guardarAsistencia(reserva.getCodigo(), reserva.getAsistencia());
        }
    }

    private void guardarAsistencia(String codigoReserva, Asistencia asistencia) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(GUARDAR_ASISTENCIA)) {
            sentencia.setString(1, codigoReserva);
            sentencia.setObject(2, asistencia.getHora());
            sentencia.executeUpdate();
        }
    }

    private void crearEsquema() {
        String script = leerScript();
        try (Statement sentencia = conexion.createStatement()) {
            for (String instruccion : script.split(";")) {
                if (!instruccion.isBlank()) {
                    sentencia.execute(instruccion);
                }
            }
        } catch (SQLException e) {
            throw new ErrorDePersistencia("No se pudo crear el esquema de la base de datos.", e);
        }
    }

    private String leerScript() {
        try (InputStream entrada = RepositorioEventosH2.class.getResourceAsStream(SCRIPT_ESQUEMA)) {
            if (entrada == null) {
                throw new IllegalStateException("No se encontró el script " + SCRIPT_ESQUEMA + ".");
            }
            return sinComentarios(new String(entrada.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el script " + SCRIPT_ESQUEMA + ".", e);
        }
    }

    /** Quita las líneas de comentario (--) para poder separar las sentencias por ';'. */
    private static String sinComentarios(String script) {
        StringBuilder limpio = new StringBuilder();
        for (String linea : script.split("\\R")) {
            if (!linea.trim().startsWith("--")) {
                limpio.append(linea).append('\n');
            }
        }
        return limpio.toString();
    }

    private void deshacer() {
        try {
            conexion.rollback();
        } catch (SQLException e) {
            // La conexión ya no sirve; el error original es el que se informa.
        }
    }

    private void volverAAutoCommit() {
        try {
            conexion.setAutoCommit(true);
        } catch (SQLException e) {
            // La conexión ya no sirve; el error original es el que se informa.
        }
    }
}
