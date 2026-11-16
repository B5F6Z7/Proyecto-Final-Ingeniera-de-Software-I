package co.edu.unillanos.campusfest;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;

import co.edu.unillanos.campusfest.application.port.Reloj;
import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.application.usecase.ConsultarCupos;
import co.edu.unillanos.campusfest.application.usecase.EditarEvento;
import co.edu.unillanos.campusfest.application.usecase.RegistrarIngreso;
import co.edu.unillanos.campusfest.application.usecase.ReservarEntrada;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.infrastructure.persistence.RepositorioEventosH2;
import co.edu.unillanos.campusfest.infrastructure.reloj.RelojDelSistema;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaCuposController;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaEventoController;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaIngresoController;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaReservaController;
import co.edu.unillanos.campusfest.infrastructure.ui.VentanaPrincipal;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Ensamblaje de CampusFest: la única clase que crea los adaptadores concretos,
 * los pasa a los casos de uso y abre las pantallas.
 */
public class App extends Application {

    private static final String VISTAS = "vistas/";
    private static final String URL_BASE_DE_DATOS = "jdbc:h2:./datos/campusfest";

    private Connection conexion;

    @Override
    public void start(Stage escenario) throws IOException, SQLException {
        conexion = DriverManager.getConnection(URL_BASE_DE_DATOS, "sa", "");
        RepositorioEventos repositorio = new RepositorioEventosH2(conexion);
        Reloj reloj = new RelojDelSistema();
        cargarEventosDeEjemplo(repositorio);

        ConsultarCupos consultarCupos = new ConsultarCupos(repositorio, reloj);
        ReservarEntrada reservarEntrada = new ReservarEntrada(repositorio, reloj);
        EditarEvento editarEvento = new EditarEvento(repositorio, reloj);
        RegistrarIngreso registrarIngreso = new RegistrarIngreso(repositorio, reloj);

        VentanaPrincipal ventana = new VentanaPrincipal();
        ventana.agregarPantalla("1", "Consultar cupos", "Estudiante",
                cargarVista("pantalla-cupos.fxml", new PantallaCuposController(consultarCupos)));
        ventana.agregarPantalla("2", "Reservar entrada", "Estudiante",
                cargarVista("pantalla-reserva.fxml", new PantallaReservaController(reservarEntrada)));
        ventana.agregarPantalla("3", "Editar evento", "Organizador",
                cargarVista("pantalla-evento.fxml", new PantallaEventoController(editarEvento)));
        ventana.agregarPantalla("4", "Registrar ingreso", "Encargado de la entrada",
                cargarVista("pantalla-ingreso.fxml", new PantallaIngresoController(registrarIngreso)));

        Scene escena = new Scene(ventana.getRaiz(), 1040, 680);
        escena.getStylesheets().add(App.class.getResource(VISTAS + "campusfest.css").toExternalForm());
        escenario.setTitle("CampusFest");
        escenario.setMinWidth(860);
        escenario.setMinHeight(560);
        escenario.setScene(escena);
        escenario.show();
    }

    @Override
    public void stop() throws SQLException {
        if (conexion != null) {
            conexion.close();
        }
    }

    private Parent cargarVista(String vista, Object controlador) throws IOException {
        FXMLLoader cargador = new FXMLLoader(App.class.getResource(VISTAS + vista));
        cargador.setController(controlador);
        return cargador.load();
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
