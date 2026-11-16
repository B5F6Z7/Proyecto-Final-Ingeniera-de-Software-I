package co.edu.unillanos.campusfest.infrastructure.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Marco de la aplicación: un menú lateral con una opción por pantalla y el área
 * donde se muestra la pantalla elegida. Solo organiza las vistas; no conoce
 * los casos de uso ni las reglas del negocio.
 */
public class VentanaPrincipal {

    private final BorderPane raiz = new BorderPane();
    private final VBox opciones = new VBox(6);
    private final ScrollPane contenido = new ScrollPane();
    private final ToggleGroup grupo = new ToggleGroup();

    public VentanaPrincipal() {
        raiz.getStyleClass().add("ventana");
        raiz.setLeft(crearMenu());
        contenido.getStyleClass().add("contenido");
        contenido.setFitToWidth(true);
        raiz.setCenter(contenido);
        grupo.selectedToggleProperty().addListener((propiedad, anterior, nueva) -> {
            if (nueva == null) {
                grupo.selectToggle(anterior);
            }
        });
    }

    /** Agrega una opción al menú; la primera queda seleccionada. */
    public void agregarPantalla(String numero, String titulo, String actor, Parent pantalla) {
        ToggleButton opcion = new ToggleButton();
        opcion.getStyleClass().add("opcion-menu");
        opcion.setGraphic(crearContenidoDeOpcion(numero, titulo, actor));
        opcion.setMaxWidth(Double.MAX_VALUE);
        opcion.setToggleGroup(grupo);
        opcion.setOnAction(evento -> contenido.setContent(pantalla));
        opciones.getChildren().add(opcion);
        if (grupo.getSelectedToggle() == null) {
            opcion.setSelected(true);
            contenido.setContent(pantalla);
        }
    }

    public Parent getRaiz() {
        return raiz;
    }

    private VBox crearMenu() {
        Label marca = new Label("CampusFest");
        marca.getStyleClass().add("marca");
        Label universidad = new Label("Universidad de los Llanos");
        universidad.getStyleClass().add("marca-detalle");
        VBox encabezado = new VBox(2, marca, universidad);
        encabezado.getStyleClass().add("encabezado-menu");

        Label seccion = new Label("CASOS DE USO");
        seccion.getStyleClass().add("seccion-menu");

        Region espacio = new Region();
        VBox.setVgrow(espacio, Priority.ALWAYS);
        Label pie = new Label("Ingeniería de Software I");
        pie.getStyleClass().add("pie-menu");

        VBox menu = new VBox(10, encabezado, seccion, opciones, espacio, pie);
        menu.getStyleClass().add("barra-lateral");
        return menu;
    }

    private Node crearContenidoDeOpcion(String numero, String titulo, String actor) {
        Label insignia = new Label(numero);
        insignia.getStyleClass().add("insignia");
        insignia.setMinSize(30, 30);
        insignia.setAlignment(Pos.CENTER);
        Label nombre = new Label(titulo);
        nombre.getStyleClass().add("opcion-titulo");
        Label detalle = new Label(actor);
        detalle.getStyleClass().add("opcion-detalle");
        HBox fila = new HBox(12, insignia, new VBox(1, nombre, detalle));
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }
}
