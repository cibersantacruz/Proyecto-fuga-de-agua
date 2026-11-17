package interfaz;

import javax.swing.JPanel;

/**
 * Clase base de todas las pestañas (paneles) de la aplicación.
 * Cada pestaña sobrescribe el método actualizar() con @Override para recargar sus datos
 * cuando la ventana principal cambia de pestaña o de vivienda.
 */
public class PanelBase extends JPanel {

    /** Cada pestaña sobrescribe este método con @Override para recargar sus datos. */
    public void actualizar() {
    }
}
