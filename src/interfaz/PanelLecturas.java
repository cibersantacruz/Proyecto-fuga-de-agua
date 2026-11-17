package interfaz;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import modelo.Hogar;
import modelo.LecturaMedidor;
import negocio.GestorLecturas;

/**
 * Pestaña 3: registro de lecturas del medidor (RF5) y su consulta, modificación y eliminación (RF6).
 * Los errores (campos vacíos, valores negativos o una lectura menor a la anterior) se muestran con JOptionPane.
 */
public class PanelLecturas extends PanelBase {

    // selector: vivienda elegida en la parte superior de la ventana
    // gestorLecturas: lógica de negocio de las lecturas
    private SelectorHogar selector;
    private GestorLecturas gestorLecturas;

    // Campos del formulario y del filtro por fechas
    private JTextField txtFecha;
    private JTextField txtHora;
    private JTextField txtValor;
    private JTextField txtDesde;
    private JTextField txtHasta;

    // Tabla del historial de lecturas
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    // Estado de la tabla:
    // mostradas: lecturas que se ven en la tabla (misma posición que las filas)
    // filtroDesde y filtroHasta: filtro de fechas activo (null = sin filtro)
    // hogarMostradoId: ID del último hogar mostrado (para limpiar el formulario al cambiar de hogar)
    private List<LecturaMedidor> mostradas = new ArrayList<>();
    private LocalDate filtroDesde = null;
    private LocalDate filtroHasta = null;
    private Integer hogarMostradoId;

    // Formatos para mostrar la fecha y la hora
    private DateTimeFormatter formatoFechaHora = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");

    /** Arma la pantalla y carga el historial del hogar seleccionado. */
    public PanelLecturas(SelectorHogar selector, GestorLecturas gestorLecturas) {
        this.selector = selector;
        this.gestorLecturas = gestorLecturas;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(crearFormulario(), BorderLayout.NORTH);
        add(crearZonaTabla(), BorderLayout.CENTER);

        actualizar();
        limpiarCampos();
    }

    // ---------- Construcción de la pantalla ----------

    /** Crea el formulario (fecha, hora y valor) con los botones Registrar, Modificar y Eliminar. */
    private JPanel crearFormulario() {
        JPanel formulario = new JPanel(new BorderLayout(5, 5));
        formulario.setBorder(BorderFactory.createTitledBorder("Lectura del medidor"));

        txtFecha = new JTextField();
        txtHora = new JTextField();
        txtValor = new JTextField();

        JPanel campos = new JPanel(new GridLayout(2, 3, 8, 8));
        campos.add(new JLabel("Fecha (AAAA-MM-DD):"));
        campos.add(txtFecha);
        campos.add(new JLabel("Hora (HH:MM):"));
        campos.add(txtHora);
        campos.add(new JLabel("Valor acumulado (m³):"));
        campos.add(txtValor);

        JButton btnRegistrar = new JButton("Registrar Lectura");
        JButton btnModificar = new JButton("Modificar");
        JButton btnEliminar = new JButton("Eliminar");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        botones.add(btnRegistrar);
        botones.add(btnModificar);
        botones.add(btnEliminar);

        formulario.add(campos, BorderLayout.CENTER);
        formulario.add(botones, BorderLayout.SOUTH);

        // Eventos
        btnRegistrar.addActionListener(e -> registrarLectura());
        btnModificar.addActionListener(e -> modificarLectura());
        btnEliminar.addActionListener(e -> eliminarLectura());

        return formulario;
    }

    /** Crea la zona central: filtro de fechas para consultar el historial y la tabla de lecturas. */
    private JPanel crearZonaTabla() {
        JPanel zona = new JPanel(new BorderLayout(5, 5));

        // Barra para consultar el historial por rango de fechas (RF6)
        txtDesde = new JTextField(10);
        txtHasta = new JTextField(10);
        JButton btnFiltrar = new JButton("Filtrar");
        JButton btnVerTodo = new JButton("Ver todo");

        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        filtro.setBorder(BorderFactory.createTitledBorder("Consultar historial por periodo"));
        filtro.add(new JLabel("Desde (AAAA-MM-DD):"));
        filtro.add(txtDesde);
        filtro.add(new JLabel("Hasta (AAAA-MM-DD):"));
        filtro.add(txtHasta);
        filtro.add(btnFiltrar);
        filtro.add(btnVerTodo);

        btnFiltrar.addActionListener(e -> filtrar());
        btnVerTodo.addActionListener(e -> {
            txtDesde.setText("");
            txtHasta.setText("");
            filtroDesde = null;
            filtroHasta = null;
            actualizarTabla();
        });

        String[] columnas = {"ID", "Fecha/Hora", "Acumulado (m³)", "Consumo del Periodo (litros)", "Observación"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Al seleccionar una fila, se copian sus datos al formulario para poder modificarla
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                copiarFilaAlFormulario();
            }
        });

        zona.add(filtro, BorderLayout.NORTH);
        zona.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return zona;
    }

    // ---------- Acciones ----------

    /** Botón "Registrar Lectura": el gestor valida los datos; si hay un error se muestra con JOptionPane. */
    private void registrarLectura() {
        Hogar hogar = selector.getHogar();
        if (hogar == null) {
            mostrarError("Debe seleccionar un hogar.");
            return;
        }
        LocalDate fecha;
        LocalTime hora;
        double valor;
        try {
            if (txtFecha.getText().trim().isEmpty() || txtHora.getText().trim().isEmpty()
                    || txtValor.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("Todos los campos son obligatorios (fecha, hora y valor del medidor).");
            }
            fecha = LectorEntradas.leerFecha(txtFecha.getText(),
                    "Fecha inválida. Use el formato AAAA-MM-DD (por ejemplo 2026-11-05).");
            hora = LectorEntradas.leerHora(txtHora.getText(),
                    "Hora inválida. Use el formato HH:MM (por ejemplo 07:30).");
            valor = LectorEntradas.leerDecimal(txtValor.getText(),
                    "El valor del medidor debe ser un número (por ejemplo 120.450).");
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
            return;
        }
        boolean ok = gestorLecturas.registrar(hogar, fecha, hora, valor);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Lectura registrada correctamente.",
                    "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            actualizarTabla();
        } else {
            mostrarError(gestorLecturas.getMensajeError());
        }
    }

    /** Botón "Modificar": cambia la lectura seleccionada en la tabla con los datos del formulario. */
    private void modificarLectura() {
        Hogar hogar = selector.getHogar();
        if (hogar == null) {
            mostrarError("Debe seleccionar un hogar.");
            return;
        }
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mostrarError("Seleccione una lectura de la tabla y cambie sus datos en el formulario.");
            return;
        }
        LecturaMedidor lectura = mostradas.get(fila);
        LocalDate fecha;
        LocalTime hora;
        double valor;
        try {
            if (txtFecha.getText().trim().isEmpty() || txtHora.getText().trim().isEmpty()
                    || txtValor.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("Todos los campos son obligatorios (fecha, hora y valor del medidor).");
            }
            fecha = LectorEntradas.leerFecha(txtFecha.getText(),
                    "Fecha inválida. Use el formato AAAA-MM-DD (por ejemplo 2026-11-05).");
            hora = LectorEntradas.leerHora(txtHora.getText(),
                    "Hora inválida. Use el formato HH:MM (por ejemplo 07:30).");
            valor = LectorEntradas.leerDecimal(txtValor.getText(),
                    "El valor del medidor debe ser un número (por ejemplo 120.450).");
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
            return;
        }
        boolean ok = gestorLecturas.actualizar(hogar, lectura.getId(), fecha, hora, valor);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Lectura modificada. Los consumos fueron recalculados.",
                    "Modificación exitosa", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            actualizarTabla();
        } else {
            mostrarError(gestorLecturas.getMensajeError());
        }
    }

    /** Botón "Eliminar": elimina la lectura seleccionada, después de pedir confirmación. */
    private void eliminarLectura() {
        Hogar hogar = selector.getHogar();
        if (hogar == null) {
            mostrarError("Debe seleccionar un hogar.");
            return;
        }
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mostrarError("Seleccione una lectura de la tabla para eliminarla.");
            return;
        }
        LecturaMedidor lectura = mostradas.get(fila);
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Seguro que desea eliminar la lectura " + lectura.getId() + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            if (gestorLecturas.eliminar(hogar, lectura.getId())) {
                limpiarCampos();
                actualizarTabla();
            } else {
                mostrarError(gestorLecturas.getMensajeError());
            }
        }
    }

    /** Botón "Filtrar": valida las fechas "Desde" y "Hasta" y muestra solo las lecturas de ese periodo. */
    private void filtrar() {
        LocalDate desde = null;
        LocalDate hasta = null;
        try {
            if (!txtDesde.getText().trim().isEmpty()) {
                desde = LectorEntradas.leerFecha(txtDesde.getText(),
                        "Fecha inválida en el filtro. Use el formato AAAA-MM-DD.");
            }
            if (!txtHasta.getText().trim().isEmpty()) {
                hasta = LectorEntradas.leerFecha(txtHasta.getText(),
                        "Fecha inválida en el filtro. Use el formato AAAA-MM-DD.");
            }
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
            return;
        }
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            mostrarError("La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'.");
            return;
        }
        filtroDesde = desde;
        filtroHasta = hasta;
        actualizarTabla();
    }

    // ---------- Métodos auxiliares ----------

    /** Copia la fecha, la hora y el valor de la fila seleccionada al formulario. */
    private void copiarFilaAlFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= mostradas.size()) {
            return;
        }
        LecturaMedidor l = mostradas.get(fila);
        txtFecha.setText(l.getFecha().toString());
        txtHora.setText(l.getFechaHora().format(formatoHora));
        txtValor.setText(String.valueOf(l.getAcumuladoM3()));
    }

    /** Prepara el formulario para una lectura nueva: fecha y hora actuales y valor vacío. */
    private void limpiarCampos() {
        txtFecha.setText(LocalDate.now().toString());
        txtHora.setText(LocalTime.now().format(formatoHora));
        txtValor.setText("");
        tabla.clearSelection();
    }

    /** Llena la tabla con el historial del hogar seleccionado (aplicando el filtro de fechas si hay uno). */
    private void actualizarTabla() {
        modeloTabla.setRowCount(0);
        mostradas.clear();
        Hogar hogar = selector.getHogar();
        if (hogar == null) {
            return;
        }
        mostradas.addAll(gestorLecturas.consultar(hogar, filtroDesde, filtroHasta));
        for (LecturaMedidor l : mostradas) {
            double consumo = gestorLecturas.consumoPeriodoLitros(hogar, l);
            modeloTabla.addRow(new Object[]{
                l.getId(),
                l.getFechaHora().format(formatoFechaHora),
                String.format("%.3f", l.getAcumuladoM3()),
                consumo < 0 ? "-" : String.format("%.0f", consumo),
                l.getObservacion()
            });
        }
    }

    /** Se llama al cambiar de pestaña o de hogar: si cambió el hogar limpia el formulario y luego recarga la tabla. */
    @Override
    public void actualizar() {
        Hogar hogar = selector.getHogar();
        Integer idHogar = hogar == null ? null : hogar.getId();
        if (!Objects.equals(hogarMostradoId, idHogar)) {
            hogarMostradoId = idHogar;
            limpiarCampos();
        }
        actualizarTabla();
    }

    /** Muestra un mensaje de error de validación con JOptionPane. */
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de validación", JOptionPane.ERROR_MESSAGE);
    }
}
