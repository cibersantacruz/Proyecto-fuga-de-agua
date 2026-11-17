package interfaz;

import java.util.Objects;
import javax.swing.JComboBox;
import modelo.Hogar;
import negocio.GestorHogares;

/**
 * Lista desplegable (JComboBox) de viviendas que comparten todas las pestañas.
 * Está en la parte superior de la ventana: al elegir otra vivienda, todos los paneles
 * trabajan con ese hogar. No es pública porque solo la usa el paquete "interfaz".
 */
class SelectorHogar {

    // gestorHogares: de donde se leen los hogares
    // combo: la lista desplegable que ve el usuario
    // cargando: evita lanzar eventos mientras se vuelve a llenar la lista
    // oyente: acción que se ejecuta cuando el usuario elige otro hogar
    private final GestorHogares gestorHogares;
    private final JComboBox<Hogar> combo;
    private boolean cargando;
    private Runnable oyente = () -> { };

    /** Crea el selector, lo conecta con el gestor de hogares y carga la lista inicial. */
    SelectorHogar(GestorHogares gestorHogares) {
        this.gestorHogares = gestorHogares;
        this.combo = new JComboBox<>();
        combo.addActionListener(e -> {
            if (!cargando) {
                oyente.run();
            }
        });
        recargar();
    }

    /** Devuelve el JComboBox para poder agregarlo a la ventana. */
    JComboBox<Hogar> getComponente() {
        return combo;
    }

    /** Devuelve el hogar seleccionado (null si no hay hogares). */
    Hogar getHogar() {
        return (Hogar) combo.getSelectedItem();
    }

    /** Define qué se hace cuando el usuario elige otra vivienda (la ventana lo usa para refrescar la pestaña abierta). */
    void alCambiar(Runnable oyente) {
        this.oyente = Objects.requireNonNull(oyente);
    }

    /**
     * Vuelve a llenar la lista con los hogares actuales (por ejemplo después de registrar o eliminar uno)
     * y mantiene seleccionado el mismo hogar si todavía existe (se compara por ID).
     */
    void recargar() {
        Hogar seleccionado = getHogar();
        Integer idSeleccionado = seleccionado == null ? null : seleccionado.getId();

        // Mientras se recarga, el combo no avisa al oyente
        cargando = true;
        try {
            combo.removeAllItems();
            Hogar seleccionActualizada = null;
            for (Hogar hogar : gestorHogares.getHogares()) {
                combo.addItem(hogar);
                if (idSeleccionado != null && hogar.getId() == idSeleccionado) {
                    seleccionActualizada = hogar;
                }
            }
            if (seleccionActualizada != null) {
                combo.setSelectedItem(seleccionActualizada);
            }
        } finally {
            cargando = false;
        }
    }
}
