package interfaz;

import java.util.Locale;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import negocio.DatosDePrueba;
import negocio.GestorFugas;
import negocio.GestorHogares;
import negocio.GestorLecturas;
import negocio.GestorMetas;
import negocio.GestorPuntosConsumo;

/** Inicia la aplicación y prepara sus datos en memoria. */
public class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Si falla, se usa el aspecto por defecto de Swing.
            }

            GestorHogares gestorHogares = new GestorHogares();
            GestorPuntosConsumo gestorPuntos = new GestorPuntosConsumo();
            GestorLecturas gestorLecturas = new GestorLecturas();
            GestorFugas gestorFugas = new GestorFugas();
            GestorMetas gestorMetas = new GestorMetas();
            DatosDePrueba.cargar(gestorHogares, gestorPuntos, gestorLecturas, gestorFugas, gestorMetas);

            new VentanaPrincipal(gestorHogares, gestorPuntos, gestorLecturas,
                    gestorFugas, gestorMetas).setVisible(true);
        });
    }
}
