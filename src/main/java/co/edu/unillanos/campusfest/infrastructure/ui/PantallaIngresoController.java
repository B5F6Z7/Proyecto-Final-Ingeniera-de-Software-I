package co.edu.unillanos.campusfest.infrastructure.ui;

import java.time.format.DateTimeFormatter;

import co.edu.unillanos.campusfest.application.usecase.RegistrarIngreso;
import co.edu.unillanos.campusfest.domain.Asistencia;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Pantalla del CU4 · Registrar el ingreso con QR. El lector de QR escribe el
 * código en el campo como si fuera un teclado; el caso de uso recibe un String.
 */
public class PantallaIngresoController {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final RegistrarIngreso registrarIngreso;

    @FXML
    private TextField campoEventoId;

    @FXML
    private TextField campoCodigo;

    @FXML
    private Label etiquetaMensaje;

    public PantallaIngresoController(RegistrarIngreso registrarIngreso) {
        this.registrarIngreso = registrarIngreso;
    }

    @FXML
    private void alRegistrar() {
        try {
            Asistencia asistencia = registrarIngreso.registrar(campoEventoId.getText().trim(),
                    campoCodigo.getText().trim());
            Mensaje.exito(etiquetaMensaje, "Ingreso registrado a las "
                    + asistencia.getHora().format(FORMATO_HORA) + ". ¡Bienvenido!");
            campoCodigo.clear();
        } catch (IllegalArgumentException | IllegalStateException e) {
            Mensaje.error(etiquetaMensaje, e.getMessage());
        }
    }
}
