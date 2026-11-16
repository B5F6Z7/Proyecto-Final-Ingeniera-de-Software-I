package co.edu.unillanos.campusfest.application.port;

import co.edu.unillanos.campusfest.domain.Evento;

/** Puerto de persistencia: los casos de uso no saben cómo se guardan los eventos. */
public interface RepositorioEventos {

    /** Devuelve el evento con sus reservas y asistencias, o null si no existe. */
    Evento buscar(String eventoId);

    /** Inserta o actualiza el evento con sus reservas y asistencias. */
    void guardar(Evento evento);
}
