package interfaz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import negocio.GestorFugas;
import negocio.GestorHogares;
import negocio.GestorLecturas;
import negocio.GestorMetas;
import negocio.GestorPuntosConsumo;

/**
 * Ventana principal de la aplicación. Contiene las pestañas (JTabbedPane)
 * y conecta los gestores con sus paneles.
 */
public class VentanaPrincipal extends JFrame {

    // Gestores de la capa de negocio (se comparten entre todos los paneles)
    private GestorHogares gestorHogares;
    private GestorPuntosConsumo gestorPuntos;
    private GestorLecturas gestorLecturas;
    private GestorFugas gestorFugas;
    private GestorMetas gestorMetas;

    // Selector de vivienda (barra superior) y paneles (una pestaña cada uno)
    private SelectorHogar selector;
    private PanelHogares panelHogares;
    private PanelPuntosConsumo panelPuntos;
    private PanelLecturas panelLecturas;
    private PanelIdealReal panelIdealReal;
    private PanelPruebaFugas panelPruebas;
    private PanelMetasAhorro panelMetas;

    /** Crea los paneles y las pestañas, y configura la ventana (900x650, centrada). */
    public VentanaPrincipal(GestorHogares gestorHogares, GestorPuntosConsumo gestorPuntos,
                            GestorLecturas gestorLecturas, GestorFugas gestorFugas,
                            GestorMetas gestorMetas) {
        super("Asistente de Ahorro de Agua y Detección de Fugas");

        this.gestorHogares = gestorHogares;
        this.gestorPuntos = gestorPuntos;
        this.gestorLecturas = gestorLecturas;
        this.gestorFugas = gestorFugas;
        this.gestorMetas = gestorMetas;

        // 1. Selector de vivienda compartido por todas las pestañas
        selector = new SelectorHogar(gestorHogares);

        // 2. Se crean los paneles
        panelHogares = new PanelHogares(gestorHogares, selector);
        panelPuntos = new PanelPuntosConsumo(selector, gestorPuntos);
        panelLecturas = new PanelLecturas(selector, gestorLecturas);
        panelIdealReal = new PanelIdealReal(gestorHogares, selector);
        panelPruebas = new PanelPruebaFugas(selector, gestorFugas);
        panelMetas = new PanelMetasAhorro(selector, gestorHogares, gestorMetas);

        // 3. Se arman las pestañas
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("🏠 Hogares", panelHogares);
        pestanas.addTab("🚰 Puntos de Consumo", panelPuntos);
        pestanas.addTab("📊 Lecturas de Medidor", panelLecturas);
        pestanas.addTab("Ideal vs real", panelIdealReal);
        pestanas.addTab("🔍 Pruebas y Alertas de Fuga", panelPruebas);
        pestanas.addTab("🎯 Metas de Ahorro", panelMetas);

        // Al cambiar de pestaña, se recarga el hogar seleccionado y los datos del panel.
        pestanas.addChangeListener(e -> {
            selector.recargar();
            ((PanelBase) pestanas.getSelectedComponent()).actualizar();
        });

        // Al elegir otra vivienda se actualiza la pestaña que está abierta
        selector.alCambiar(() -> ((PanelBase) pestanas.getSelectedComponent()).actualizar());

        // Barra superior con la lista de viviendas (la misma para todas las pestañas)
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        selectorPanel.setBackground(new Color(227, 242, 253));
        selectorPanel.add(new JLabel("Vivienda seleccionada:"));
        selector.getComponente().setPreferredSize(new Dimension(420, 28));
        selectorPanel.add(selector.getComponente());

        // Selector de vivienda arriba y pestañas en el centro
        setLayout(new BorderLayout());
        add(selectorPanel, BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);

        // 4. Configuración de la ventana
        setSize(900, 650);
        setLocationRelativeTo(null);   // centrada en la pantalla
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}
