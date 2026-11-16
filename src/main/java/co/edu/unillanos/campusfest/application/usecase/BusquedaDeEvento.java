package co.edu.unillanos.campusfest.application.usecase;

import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Evento;

/**
 * Busca un evento y rechaza el que no existe. Está en un solo lugar para que
 * los cuatro casos de uso no repitan la misma comprobación.
 */
final class BusquedaDeEvento {

    private BusquedaDeEvento() {
    }

    static Evento existente(RepositorioEventos repositorio, String eventoId) {
        Evento evento = repositorio.buscar(eventoId);
        if (evento == null) {
            throw new IllegalArgumentException("No existe el evento " + eventoId + ".");
        }
        return evento;
    }
}
