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
    private Label etiquetaCupos;

    @FXML
    private Label etiquetaMensaje;

    public PantallaCuposController(ConsultarCupos consultarCupos) {
        this.consultarCupos = consultarCupos;
    }

    @FXML
    private void alConsultar() {
        try {
            String eventoId = campoEventoId.getText().trim();
            int cupos = consultarCupos.consultar(eventoId);
            etiquetaCupos.setText(String.valueOf(cupos));
            Mensaje.exito(etiquetaMensaje, "Consulta hecha para el evento " + eventoId + ".");
        } catch (IllegalArgumentException | IllegalStateException e) {
            etiquetaCupos.setText("—");
            Mensaje.error(etiquetaMensaje, e.getMessage());
        }
    }
}
