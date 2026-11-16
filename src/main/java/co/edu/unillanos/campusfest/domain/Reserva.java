package co.edu.unillanos.campusfest.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Reserva de un cupo en un evento.
 * Es la experta en su plazo de 10 minutos, en sus estados y en el ingreso.
 */
public class Reserva {

    private static final int MINUTOS_PARA_CONFIRMAR = 10;

    private final String codigo;
    private final String estudianteId;
    private final LocalDateTime venceEn;
    private EstadoReserva estado;
    private Asistencia asistencia;

    private Reserva(String codigo, String estudianteId, LocalDateTime venceEn,
                    EstadoReserva estado, Asistencia asistencia) {
        this.codigo = codigo;
        this.estudianteId = estudianteId;
        this.venceEn = venceEn;
        this.estado = estado;
        this.asistencia = asistencia;
    }

    /** Crea una reserva PENDIENTE que vence en ahora + 10 minutos. */
    public static Reserva pendiente(String estudianteId, LocalDateTime ahora) {
        if (estudianteId == null || estudianteId.isBlank()) {
            throw new IllegalArgumentException("El código del estudiante es obligatorio.");
        }
        String codigo = UUID.randomUUID().toString();
        return new Reserva(codigo, estudianteId.trim(), ahora.plusMinutes(MINUTOS_PARA_CONFIRMAR),
                EstadoReserva.PENDIENTE, null);
    }

    /** Vuelve a armar una reserva leída de la base de datos. No aplica reglas. */
    public static Reserva reconstruir(String codigo, String estudianteId, LocalDateTime venceEn,
                                      EstadoReserva estado, Asistencia asistencia) {
        return new Reserva(codigo, estudianteId, venceEn, estado, asistencia);
    }

    /** Ocupa cupo si está confirmada o si está pendiente y no ha vencido. */
    public boolean ocupaCupo(LocalDateTime ahora) {
        if (estado == EstadoReserva.CONFIRMADA) {
            return true;
        }
        return estado == EstadoReserva.PENDIENTE && !vencio(ahora);
    }

    public void confirmar(LocalDateTime ahora) {
        if (estado != EstadoReserva.PENDIENTE) {
            throw new IllegalStateException("La reserva no se puede confirmar porque está "
                    + estado + ".");
        }
        if (vencio(ahora)) {
            estado = EstadoReserva.VENCIDA;
            throw new IllegalStateException("La reserva venció: debía confirmarse antes de "
                    + venceEn.toLocalTime().withNano(0) + ". El cupo quedó libre.");
        }
        estado = EstadoReserva.CONFIRMADA;
    }

    public void cancelar() {
        if (estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("La reserva no se puede cancelar porque está "
                    + estado + ".");
        }
        estado = EstadoReserva.CANCELADA;
    }

    /** Registra el único ingreso permitido de una reserva confirmada (Creador de Asistencia). */
    public Asistencia registrarIngreso(LocalDateTime ahora) {
        if (estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("La reserva no está confirmada (está " + estado
                    + "): no puede ingresar.");
        }
        if (asistencia != null) {
            throw new IllegalStateException("Esta reserva ya ingresó a las "
                    + asistencia.getHora().toLocalTime().withNano(0) + ".");
        }
        asistencia = new Asistencia(ahora);
        return asistencia;
    }

    /** A los 10 minutos exactos la reserva pendiente ya está vencida. */
    private boolean vencio(LocalDateTime ahora) {
        return !ahora.isBefore(venceEn);
    }

    public String getCodigo() {
        return codigo;
    }

    public String getEstudianteId() {
        return estudianteId;
    }

    public LocalDateTime getVenceEn() {
        return venceEn;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public Asistencia getAsistencia() {
        return asistencia;
    }
}
