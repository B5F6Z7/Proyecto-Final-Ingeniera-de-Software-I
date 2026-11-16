package co.edu.unillanos.campusfest.infrastructure.ui;

import co.edu.unillanos.campusfest.application.usecase.ConsultarCupos;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/** Pantalla del CU1 · Consultar cupos. Lee el evento, llama al caso de uso y muestra el resultado. */
public class PantallaCuposController {

    private final ConsultarCupos consultarCupos;

    @FXML
    private TextField campoEventoId;

    @FXML
    private Label etiquetaMensaje;

    public PantallaCuposController(ConsultarCupos consultarCupos) {
        this.consultarCupos = consultarCupos;
    }

    @FXML
    private void alConsultar() {
        try {
            int cupos = consultarCupos.consultar(campoEventoId.getText().trim());
            Mensaje.exito(etiquetaMensaje, "Cupos disponibles: " + cupos);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Mensaje.error(etiquetaMensaje, e.getMessage());
        }
    }
}
