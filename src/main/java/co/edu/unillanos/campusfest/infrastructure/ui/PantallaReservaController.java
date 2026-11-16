package co.edu.unillanos.campusfest.infrastructure.ui;

import java.time.format.DateTimeFormatter;

import co.edu.unillanos.campusfest.application.usecase.ReservarEntrada;
import co.edu.unillanos.campusfest.domain.Reserva;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/** Pantalla del CU2 · Reservar y confirmar una entrada. */
public class PantallaReservaController {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ReservarEntrada reservarEntrada;

    @FXML
    private TextField campoEventoId;

    @FXML
    private TextField campoEstudianteId;

    @FXML
    private TextField campoCodigo;

    @FXML
    private Label etiquetaMensaje;

    public PantallaReservaController(ReservarEntrada reservarEntrada) {
        this.reservarEntrada = reservarEntrada;
    }

    @FXML
    private void alSolicitar() {
        String eventoId = campoEventoId.getText().trim();
        String estudianteId = campoEstudianteId.getText().trim(); // Se agrega el trim() faltante

        // Validación exclusiva de la UI: campos vacíos
        if (eventoId.isEmpty() || estudianteId.isEmpty()) {
            Mensaje.error(etiquetaMensaje, "Los campos Evento y Estudiante no pueden estar vacíos.");
            return;
        }

        try {
            Reserva reserva = reservarEntrada.solicitarReserva(eventoId, estudianteId);
            campoCodigo.setText(reserva.getCodigo());
            Mensaje.exito(etiquetaMensaje, "Reserva pendiente creada. Confírmela antes de las "
                    + reserva.getVenceEn().format(FORMATO_HORA) + ".");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Mensaje.error(etiquetaMensaje, e.getMessage());
        }
    }

    @FXML
    private void alConfirmar() {
        String eventoId = campoEventoId.getText().trim();
        String codigo = campoCodigo.getText().trim();

        if (eventoId.isEmpty() || codigo.isEmpty()) {
            Mensaje.error(etiquetaMensaje, "Los campos Evento y Código no pueden estar vacíos.");
            return;
        }

        try {
            Reserva reserva = reservarEntrada.confirmarReserva(eventoId, codigo);
            Mensaje.exito(etiquetaMensaje, "Reserva confirmada. Su código QR es " + reserva.getCodigo());
        } catch (IllegalArgumentException | IllegalStateException e) {
            Mensaje.error(etiquetaMensaje, e.getMessage());
        }
    }