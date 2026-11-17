package interfaz;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import modelo.CatalogoArtefactos;
import modelo.Hogar;
import modelo.PuntoConsumo;
import modelo.TipoArtefacto;
import negocio.GestorPuntosConsumo;

/**
 * Pestaña 2: puntos de consumo (RF3) con el cálculo del consumo estimado (RF4).
 * El tipo de artefacto se elige en una lista desplegable que rellena el caudal estándar;
 * también permite cargar, actualizar y eliminar puntos existentes.
 */
public class PanelPuntosConsumo extends PanelBase {

    // selector: vivienda elegida en la parte superior de la ventana
    // gestorPuntos: lógica de negocio de los artefactos
    private SelectorHogar selector;
    private GestorPuntosConsumo gestorPuntos;

    // Campos del formulario
    private JTextField txtUbicacion;
    private JComboBox<TipoArtefacto> cmbArtefacto;
    private CatalogoArtefactos catalogo;
    private JTextField txtNombrePersonalizado;   // Solo se habilita con "Otros"
    private JLabel lblCaudal;                    // Cambia según la unidad (L/min, L/descarga, L/ciclo)
    private JTextField txtCaudal;
    private JLabel lblDuracion;
    private JTextField txtDuracion;              // Solo se habilita si el caudal es en L/min
    private JTextField txtFrecuencia;
    private Integer idPuntoEnEdicion;

    // Tabla de artefactos y etiqueta con el total del hogar
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotal;

    /** Arma la pantalla y carga los artefactos del hogar seleccionado. */
    public PanelPuntosConsumo(SelectorHogar selector, GestorPuntosConsumo gestorPuntos) {
        this.selector = selector;
        this.gestorPuntos = gestorPuntos;
        this.catalogo = new CatalogoArtefactos();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(crearFormulario(), BorderLayout.NORTH);
        add(crearTabla(), BorderLayout.CENTER);

        lblTotal = new JLabel(" ");
        lblTotal.setBorder(BorderFactory.createTitledBorder("Consumo total estimado del hogar"));
        add(lblTotal, BorderLayout.SOUTH);

        actualizar();
    }

    // ---------- Construcción de la pantalla ----------

    /** Crea el formulario del artefacto (con la lista desplegable de tipos) y los botones Agregar y Eliminar. */
    private JPanel crearFormulario() {
        JPanel formulario = new JPanel(new BorderLayout(5, 5));
        formulario.setBorder(BorderFactory.createTitledBorder("Nuevo artefacto de consumo"));

        txtUbicacion = new JTextField();
        cmbArtefacto = new JComboBox<>();
        for (TipoArtefacto tipo : catalogo.obtenerTipos()) {
            cmbArtefacto.addItem(tipo);
        }
        txtNombrePersonalizado = new JTextField();
        txtCaudal = new JTextField();
        txtDuracion = new JTextField();
        txtFrecuencia = new JTextField();
        lblCaudal = new JLabel("Caudal (L/min):");
        lblDuracion = new JLabel("Duración media (min):");

        // Campos del formulario en una cuadrícula de 3 filas x 4 columnas
        JPanel campos = new JPanel(new GridLayout(3, 4, 8, 8));
        campos.add(new JLabel("Nombre / Ubicación:"));
        campos.add(txtUbicacion);
        campos.add(new JLabel("Tipo de artefacto:"));
        campos.add(cmbArtefacto);
        campos.add(new JLabel("Nombre (solo si elige Otros):"));
        campos.add(txtNombrePersonalizado);
        campos.add(lblCaudal);
        campos.add(txtCaudal);
        campos.add(lblDuracion);
        campos.add(txtDuracion);
        campos.add(new JLabel("Frecuencia diaria (veces):"));
        campos.add(txtFrecuencia);

        JButton btnAgregar = new JButton("Agregar Artefacto");
        JButton btnCargar = new JButton("Cargar para editar");
        JButton btnActualizar = new JButton("Actualizar Artefacto");
        JButton btnEliminar = new JButton("Eliminar Artefacto");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        botones.add(btnAgregar);
        botones.add(btnCargar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);

        formulario.add(campos, BorderLayout.CENTER);
        formulario.add(botones, BorderLayout.SOUTH);

        // Eventos
        cmbArtefacto.addActionListener(e -> alCambiarArtefacto());
        btnAgregar.addActionListener(e -> guardarArtefacto(false));
        btnCargar.addActionListener(e -> cargarArtefactoSeleccionado());
        btnActualizar.addActionListener(e -> guardarArtefacto(true));
        btnEliminar.addActionListener(e -> eliminarArtefacto());

        // Deja el formulario listo para la primera opción de la lista (Ducha)
        alCambiarArtefacto();

        return formulario;
    }

    /** Crea la tabla de artefactos con sus cálculos (volumen diario, consumo mensual e impacto). */
    private JScrollPane crearTabla() {
        String[] columnas = {"ID", "Nombre / Ubicación", "Artefacto", "Caudal", "Duración (min)",
            "Frecuencia/día", "Volumen Diario (L)", "Consumo Mensual (m³)", "Impacto (%)"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return new JScrollPane(tabla);
    }

    // ---------- Acciones ----------

    /**
     * Se ejecuta cada vez que se elige otro artefacto en la lista:
     *  - Rellena el caudal estándar (el usuario puede editarlo).
     *  - Habilita el nombre personalizado solo con "Otros".
     *  - Deshabilita la duración cuando el caudal es por descarga o por ciclo.
     */
    private void alCambiarArtefacto() {
        TipoArtefacto artefacto = (TipoArtefacto) cmbArtefacto.getSelectedItem();
        if (artefacto == null) {
            return;
        }
        boolean esOtros = artefacto.getNombre().equals(CatalogoArtefactos.OTROS);
        String unidad = artefacto.getUnidad();
        boolean usaDuracion = unidad.equals(TipoArtefacto.UNIDAD_MINUTO);

        // Nombre personalizado
        txtNombrePersonalizado.setEnabled(esOtros);
        txtNombrePersonalizado.setText("");

        // Caudal: valor estándar, o vacío si es "Otros"
        lblCaudal.setText("Caudal (" + unidad + "):");
        if (esOtros) {
            txtCaudal.setText("");
        } else {
            txtCaudal.setText(String.valueOf(artefacto.getCaudalPredeterminado()));
        }
        txtCaudal.setEnabled(true);   // siempre se puede editar

        // Duración: solo tiene sentido cuando el caudal es en L/min
        txtDuracion.setEnabled(usaDuracion);
        if (usaDuracion) {
            txtDuracion.setText("");
        } else {
            txtDuracion.setText("No aplica");
        }
    }

    /** Botones "Agregar" y "Actualizar": el gestor valida los datos; si hay un error se muestra con JOptionPane. */
    private void guardarArtefacto(boolean actualizar) {
        Hogar hogar = selector.getHogar();
        if (hogar == null) {
            mostrarError("Debe seleccionar un hogar.");
            return;
        }
        if (actualizar && idPuntoEnEdicion == null) {
            mostrarError("Seleccione un artefacto y cárguelo para editar.");
            return;
        }
        TipoArtefacto artefacto = (TipoArtefacto) cmbArtefacto.getSelectedItem();
        double caudal;
        double duracion = 0;
        int frecuencia;
        boolean esMinuto = artefacto != null
                && artefacto.getUnidad().equals(TipoArtefacto.UNIDAD_MINUTO);
        try {
            if (txtCaudal.getText().trim().isEmpty() || txtFrecuencia.getText().trim().isEmpty()
                    || (esMinuto && txtDuracion.getText().trim().isEmpty())) {
                throw new IllegalArgumentException("Todos los campos son obligatorios.");
            }
            caudal = LectorEntradas.leerDecimal(txtCaudal.getText(),
                    "El caudal debe ser un número (por ejemplo 9.0).");
            if (esMinuto) {
                duracion = LectorEntradas.leerDecimal(txtDuracion.getText(),
                        "La duración debe ser un número (por ejemplo 5).");
            }
            frecuencia = LectorEntradas.leerEntero(txtFrecuencia.getText(),
                    "La frecuencia diaria debe ser un número entero.");
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
            return;
        }
        boolean ok;
        if (actualizar) {
            ok = gestorPuntos.actualizar(hogar, idPuntoEnEdicion, txtUbicacion.getText(), artefacto,
                    txtNombrePersonalizado.getText(), caudal, duracion, frecuencia);
        } else {
            ok = gestorPuntos.registrar(hogar, txtUbicacion.getText(), artefacto,
                    txtNombrePersonalizado.getText(), caudal, duracion, frecuencia);
        }
        if (ok) {
            txtUbicacion.setText("");
            txtFrecuencia.setText("");
            idPuntoEnEdicion = null;
            alCambiarArtefacto();   // restablece caudal, nombre y duración
            actualizarTabla();
        } else {
            JOptionPane.showMessageDialog(this, gestorPuntos.getMensajeError(),
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Carga en el formulario los datos del artefacto seleccionado para poder actualizarlos. */
    private void cargarArtefactoSeleccionado() {
        Hogar hogar = selector.getHogar();
        int fila = tabla.getSelectedRow();
        if (hogar == null) {
            mostrarError("Debe seleccionar un hogar.");
            return;
        }
        if (fila < 0) {
            mostrarError("Seleccione un artefacto de la tabla para editarlo.");
            return;
        }
        int id = (Integer) modeloTabla.getValueAt(fila, 0);
        PuntoConsumo punto = null;
        for (PuntoConsumo candidato : hogar.obtenerPuntos()) {
            if (candidato.getId() == id) {
                punto = candidato;
                break;
            }
        }
        if (punto == null) {
            mostrarError("No se encontró el artefacto seleccionado.");
            return;
        }
        TipoArtefacto tipo = catalogo.buscarPorNombre(punto.getTipo());
        if (tipo == null) {
            tipo = catalogo.buscarPorNombre(CatalogoArtefactos.OTROS);
            cmbArtefacto.setSelectedItem(tipo);
            txtNombrePersonalizado.setText(punto.getTipo());
        } else {
            cmbArtefacto.setSelectedItem(tipo);
        }
        txtUbicacion.setText(punto.getNombre());
        txtCaudal.setText(String.valueOf(punto.getCaudal()));
        txtDuracion.setText(punto.usaDuracion() ? String.valueOf(punto.getDuracionMinutos()) : "No aplica");
        txtFrecuencia.setText(String.valueOf(punto.getFrecuenciaDiaria()));
        idPuntoEnEdicion = id;
    }

    /** Botón "Eliminar Artefacto": elimina el artefacto seleccionado en la tabla. */
    private void eliminarArtefacto() {
        Hogar hogar = selector.getHogar();
        if (hogar == null) {
            mostrarError("Debe seleccionar un hogar.");
            return;
        }
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un artefacto de la tabla para eliminarlo.",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int id = (Integer) modeloTabla.getValueAt(fila, 0);
        if (gestorPuntos.eliminar(hogar, id)) {
            actualizarTabla();
        } else {
            JOptionPane.showMessageDialog(this, gestorPuntos.getMensajeError(),
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- Métodos auxiliares ----------

    /** Llena la tabla con los artefactos del hogar seleccionado. */
    private void actualizarTabla() {
        modeloTabla.setRowCount(0);
        Hogar hogar = selector.getHogar();
        if (hogar == null) {
            lblTotal.setText("No hay hogares registrados.");
            return;
        }
        for (PuntoConsumo p : hogar.obtenerPuntos()) {
            modeloTabla.addRow(new Object[]{
                p.getId(),
                p.getNombre(),
                p.getTipo(),
                String.format("%.1f %s", p.getCaudal(), p.getUnidad()),
                p.usaDuracion() ? String.valueOf(p.getDuracionMinutos()) : "-",
                p.getFrecuenciaDiaria(),
                String.format("%.1f", p.calcularVolumenDiario()),
                String.format("%.3f", p.calcularConsumoMensualM3()),
                String.format("%.1f", gestorPuntos.calcularPorcentajeImpacto(hogar, p))
            });
        }
        lblTotal.setText("Total estimado: " + String.format("%.1f", gestorPuntos.calcularTotalDiarioLitros(hogar))
                + " L/día  |  " + String.format("%.3f", gestorPuntos.calcularTotalMensualM3(hogar)) + " m³/mes");
    }

    /** Se llama al cambiar de pestaña o de hogar: recarga la tabla de artefactos. */
    @Override
    public void actualizar() {
        actualizarTabla();
    }

    /** Muestra un mensaje de error de validación con JOptionPane. */
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de validación", JOptionPane.ERROR_MESSAGE);
    }
}
