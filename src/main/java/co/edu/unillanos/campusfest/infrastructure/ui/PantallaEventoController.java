package co.edu.unillanos.campusfest.infrastructure.ui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import co.edu.unillanos.campusfest.application.usecase.EditarEvento;
import co.edu.unillanos.campusfest.domain.Evento;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/** Pantalla del CU3 · Editar evento. Carga el formulario y envía los cambios al caso de uso. */
public class PantallaEventoController {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final EditarEvento editarEvento;

    @FXML
    private TextField campoEventoId;

    @FXML
    private TextField campoNombre;

    @FXML
    private DatePicker campoFecha;

    @FXML
    private TextField campoHora;

    @FXML
    private TextField campoLugar;

    @FXML
    private TextField campoAforo;

    @FXML
    private Label etiquetaMensaje;

    public PantallaEventoController(EditarEvento editarEvento) {
        this.editarEvento = editarEvento;
    }

    @FXML
    private void alCargar() {
        try {
            Evento evento = editarEvento.obtener(campoEventoId.getText().trim());
            campoNombre.setText(evento.getNombre());
            campoFecha.setValue(evento.getFecha().toLocalDate());
            campoHora.setText(evento.getFecha().toLocalTime().format(FORMATO_HORA));
            campoLugar.setText(evento.getLugar());
            campoAforo.setText(String.valueOf(evento.getAforo()));
            Mensaje.exito(etiquetaMensaje, "Evento cargado: " + evento.getNombre());
        } catch (IllegalArgumentException | IllegalStateException e) {
            Mensaje.error(etiquetaMensaje, e.getMessage());
        }
    }

    @FXML
    private void alGuardar() {
        try {
            editarEvento.editar(campoEventoId.getText().trim(), campoNombre.getText(), leerFecha(),
                    campoLugar.getText(), leerAforo());
            Mensaje.exito(etiquetaMensaje, "Cambios guardados.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            Mensaje.error(etiquetaMensaje, e.getMessage());
        }
    }

    private LocalDateTime leerFecha() {
        LocalDate dia = campoFecha.getValue();
        if (dia == null) {
            throw new IllegalArgumentException("Seleccione la fecha del evento.");
        }
        try {
            return dia.atTime(LocalTime.parse(campoHora.getText().trim(), FORMATO_HORA));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Escriba la hora con el formato HH:mm, por ejemplo 19:00.");
        }
    }

    private int leerAforo() {
        try {
            return Integer.parseInt(campoAforo.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El aforo debe ser un número entero.");
        }
    }
}
