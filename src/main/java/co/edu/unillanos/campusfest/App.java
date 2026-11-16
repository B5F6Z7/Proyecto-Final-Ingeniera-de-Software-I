package co.edu.unillanos.campusfest;

import java.io.IOException;
import java.time.LocalDateTime;

import co.edu.unillanos.campusfest.application.port.Reloj;
import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.application.usecase.ConsultarCupos;
import co.edu.unillanos.campusfest.application.usecase.EditarEvento;
import co.edu.unillanos.campusfest.application.usecase.RegistrarIngreso;
import co.edu.unillanos.campusfest.application.usecase.ReservarEntrada;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.infrastructure.persistence.RepositorioEventosEnMemoria;
import co.edu.unillanos.campusfest.infrastructure.reloj.RelojDelSistema;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaCuposController;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaEventoController;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaIngresoController;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaReservaController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * Ensamblaje de CampusFest: la única clase que crea los adaptadores concretos,
 * los pasa a los casos de uso y abre las pantallas.
 */
public class App extends Application {

    private static final String VISTAS = "vistas/";

    @Override
    public void start(Stage escenario) throws IOException {
        RepositorioEventos repositorio = new RepositorioEventosEnMemoria();
        Reloj reloj = new RelojDelSistema();
        cargarEventosDeEjemplo(repositorio);

        ConsultarCupos consultarCupos = new ConsultarCupos(repositorio, reloj);
        ReservarEntrada reservarEntrada = new ReservarEntrada(repositorio, reloj);
        EditarEvento editarEvento = new EditarEvento(repositorio, reloj);
        RegistrarIngreso registrarIngreso = new RegistrarIngreso(repositorio, reloj);

        TabPane pestanas = new TabPane(
                pestana("CU1 · Consultar cupos", "pantalla-cupos.fxml",
                        new PantallaCuposController(consultarCupos)),
                pestana("CU2 · Reservar entrada", "pantalla-reserva.fxml",
                        new PantallaReservaController(reservarEntrada)),
                pestana("CU3 · Editar evento", "pantalla-evento.fxml",
                        new PantallaEventoController(editarEvento)),
                pestana("CU4 · Registrar ingreso", "pantalla-ingreso.fxml",
                        new PantallaIngresoController(registrarIngreso)));
        pestanas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Scene escena = new Scene(pestanas, 760, 540);
        escena.getStylesheets().add(App.class.getResource(VISTAS + "campusfest.css").toExternalForm());
        escenario.setTitle("CampusFest");
        escenario.setScene(escena);
        escenario.show();
    }

    private Tab pestana(String titulo, String vista, Object controlador) throws IOException {
        FXMLLoader cargador = new FXMLLoader(App.class.getResource(VISTAS + vista));
        cargador.setController(controlador);
        Parent contenido = cargador.load();
        return new Tab(titulo, contenido);
    }

    /** Deja eventos de ejemplo la primera vez que se abre la aplicación. */
    private void cargarEventosDeEjemplo(RepositorioEventos repositorio) {
        if (repositorio.buscar("ev-1") == null) {
            repositorio.guardar(new Evento("ev-1", "Concierto de bienvenida",
                    LocalDateTime.of(2026, 11, 28, 19, 0), "Auditorio Barcelona", 120, 5));
        }
        if (repositorio.buscar("ev-2") == null) {
            repositorio.guardar(new Evento("ev-2", "Feria de emprendimiento",
                    LocalDateTime.of(2026, 12, 3, 9, 0), "Coliseo", 3, 1));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
