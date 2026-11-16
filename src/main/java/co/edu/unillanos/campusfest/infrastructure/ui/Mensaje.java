package co.edu.unillanos.campusfest.infrastructure.ui;

import javafx.scene.control.Label;

/** Muestra en la etiqueta de mensajes un resultado o un error con su estilo. */
final class Mensaje {

    private static final String ESTILO_EXITO = "mensaje-exito";
    private static final String ESTILO_ERROR = "mensaje-error";

    private Mensaje() {
    }

    static void exito(Label etiqueta, String texto) {
        mostrar(etiqueta, texto, ESTILO_EXITO);
    }

    static void error(Label etiqueta, String texto) {
        mostrar(etiqueta, texto, ESTILO_ERROR);
    }

    private static void mostrar(Label etiqueta, String texto, String estilo) {
        etiqueta.getStyleClass().removeAll(ESTILO_EXITO, ESTILO_ERROR);
        etiqueta.getStyleClass().add(estilo);
        etiqueta.setText(texto);
    }
}
