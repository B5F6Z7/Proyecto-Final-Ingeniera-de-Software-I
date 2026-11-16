package co.edu.unillanos.campusfest.infrastructure.persistence;

/**
 * Error de la base de datos traducido por el adaptador. Es una IllegalStateException,
 * así ninguna excepción de java.sql llega a los casos de uso ni a la pantalla.
 */
public class ErrorDePersistencia extends IllegalStateException {

    public ErrorDePersistencia(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
