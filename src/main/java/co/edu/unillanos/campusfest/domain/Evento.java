package co.edu.unillanos.campusfest.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Evento de CampusFest. Es el experto en sus cupos y en su aforo,
 * y el creador de sus reservas.
 */
public class Evento {

    private final String id;
    private String nombre;
    private LocalDateTime fecha;
    private String lugar;
    private int aforo;
    private final int bloqueados;
    private final List<Reserva> reservas;

    public Evento(String id, String nombre, LocalDateTime fecha, String lugar,
                  int aforo, int bloqueados) {
        this(id, nombre, fecha, lugar, aforo, bloqueados, new ArrayList<>());
    }

    /** Usado por el adaptador H2 para reconstruir el evento con sus reservas. */
    public Evento(String id, String nombre, LocalDateTime fecha, String lugar,
                  int aforo, int bloqueados, List<Reserva> reservas) {
        this.id = textoObligatorio(id, "El identificador del evento");
        validarDatos(nombre, fecha, lugar);
        if (bloqueados < 0 || aforo < bloqueados) {
            throw new IllegalArgumentException(
                    "El aforo no puede ser menor que los cupos bloqueados ni negativo.");
        }
        this.nombre = nombre.trim();
        this.fecha = fecha;
        this.lugar = lugar.trim();
        this.aforo = aforo;
        this.bloqueados = bloqueados;
        this.reservas = new ArrayList<>(reservas);
    }

    /** CU1: cupos disponibles = aforo − bloqueados − cupos ocupados por reservas. */
    public int cuposDisponibles(LocalDateTime ahora) {
        return aforo - aforoMinimo(ahora);
    }

    /** CU2: crea la reserva pendiente si hay cupo (Creador). */
    public Reserva reservar(String estudianteId, LocalDateTime ahora) {
        if (cuposDisponibles(ahora) <= 0) {
            throw new IllegalStateException("No hay cupos disponibles para " + nombre + ".");
        }
        Reserva reserva = Reserva.pendiente(estudianteId, ahora);
        reservas.add(reserva);
        return reserva;
    }

    /** CU2 y CU4: la reserva con ese código, si es de este evento. */
    public Reserva buscarReservaPorCodigo(String codigo) {
        for (Reserva reserva : reservas) {
            if (reserva.getCodigo().equals(codigo)) {
                return reserva;
            }
        }
        throw new IllegalArgumentException("El código " + codigo
                + " no corresponde a ninguna reserva del evento " + nombre + ".");
    }

    /** CU3: cambia nombre, fecha y lugar. */
    public void cambiarDatos(String nombre, LocalDateTime fecha, String lugar) {
        validarDatos(nombre, fecha, lugar);
        this.nombre = nombre.trim();
        this.fecha = fecha;
        this.lugar = lugar.trim();
    }

    /** CU3: el aforo nunca queda por debajo del aforo mínimo. */
    public void cambiarAforo(int nuevoAforo, LocalDateTime ahora) {
        int minimo = aforoMinimo(ahora);
        if (nuevoAforo < minimo) {
            int ocupados = minimo - bloqueados;
            throw new IllegalStateException("Hay " + ocupados + " cupos ocupados por reservas y "
                    + bloqueados + " bloqueados: el aforo no puede ser menor que " + minimo + ".");
        }
        this.aforo = nuevoAforo;
    }

    /** Aforo mínimo = bloqueados + cupos ocupados por reservas. */
    private int aforoMinimo(LocalDateTime ahora) {
        return bloqueados + cuposOcupadosPorReservas(ahora);
    }

    /** Confirmadas + pendientes vigentes; la Reserva decide si ocupa cupo. */
    private int cuposOcupadosPorReservas(LocalDateTime ahora) {
        int ocupados = 0;
        for (Reserva reserva : reservas) {
            if (reserva.ocupaCupo(ahora)) {
                ocupados++;
            }
        }
        return ocupados;
    }

    private static void validarDatos(String nombre, LocalDateTime fecha, String lugar) {
        textoObligatorio(nombre, "El nombre del evento");
        textoObligatorio(lugar, "El lugar del evento");
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha del evento es obligatoria.");
        }
    }

    private static String textoObligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        return valor.trim();
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getLugar() {
        return lugar;
    }

    public int getAforo() {
        return aforo;
    }

    public int getBloqueados() {
        return bloqueados;
    }

    /** Lista de solo lectura: las reservas se crean con reservar(). */
    public List<Reserva> getReservas() {
        return Collections.unmodifiableList(reservas);
    }
}
