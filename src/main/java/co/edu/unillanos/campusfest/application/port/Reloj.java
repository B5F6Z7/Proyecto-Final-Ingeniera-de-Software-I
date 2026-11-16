package co.edu.unillanos.campusfest.application.port;

import java.time.LocalDateTime;

/** Puerto de la hora actual (Variaciones protegidas): permite probar el plazo sin esperar. */
public interface Reloj {

    LocalDateTime ahora();
}
