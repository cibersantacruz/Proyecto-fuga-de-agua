package interfaz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import modelo.AlertaFuga;
import modelo.Hogar;
import modelo.LecturaMedidor;
import modelo.PruebaFuga;
import negocio.ComparacionHistorica;
import negocio.GestorFugas;

/**
 * Pestaña 4: pruebas de fuga y alertas (RF7).
 * Compara lecturas nocturnas y permite analizar aumentos frente al promedio histórico.
 */
public class PanelPruebaFugas extends PanelBase {

    // selector: vivienda elegida en la parte superior de la ventana
    // gestorFugas: lógica de negocio de las pruebas y alertas
    private SelectorHogar selector;
    private GestorFugas gestorFugas;

    // Campos del formulario y tabla del historial de pruebas
    private JComboBox<LecturaMedidor> cmbInicial;
    private JComboBox<LecturaMedidor> cmbFinal;
    private JTextField txtLimite;
    private JCheckBox chkSinUso;

    private JTable tabla;
    private DefaultTableModel modeloTabla;

    // Zona de resultado: etiqueta de estado (cambia de color), recomendaciones y botones de la alerta
    private JLabel lblEstado;
    private JTextArea txtRecomendaciones;
    private JButton btnIniciarRevision;
    private JButton btnCerrarAlerta;

    private DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Colores del estado
    private static final Color VERDE = new Color(39, 140, 80);
    private static final Color ROJO = new Color(200, 40, 40);
    private static final Color GRIS = new Color(120, 120, 120);

    /** Arma la pantalla y carga las lecturas y pruebas del hogar seleccionado. */
    public PanelPruebaFugas(SelectorHogar selector, GestorFugas gestorFugas) {
        this.selector = selector;
        this.gestorFugas = gestorFugas;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formularios = new JPanel(new BorderLayout(5, 5));
        formularios.add(crearFormulario(), BorderLayout.NORTH);
        formularios.add(crearFormularioAnalisisHistorico(), BorderLayout.SOUTH);
        add(formularios, BorderLayout.NORTH);
        add(crearTabla(), BorderLayout.CENTER);
        add(crearPanelResultado(), BorderLayout.SOUTH);

        actualizar();
    }

    // ---------- Construcción de la pantalla ----------

    /** Crea el formulario: límite nocturno, lecturas inicial y final, confirmación de "sin uso" y el botón de ejecutar. */
    private JPanel crearFormulario() {
        JPanel formulario = new JPanel(new BorderLayout(5, 5));
        formulario.setBorder(BorderFactory.createTitledBorder("Prueba de fuga"));

        cmbInicial = new JComboBox<>();
        cmbFinal = new JComboBox<>();
        txtLimite = new JTextField("5.0");
        chkSinUso = new JCheckBox("Confirmo que nadie usó agua entre ambas lecturas");

        // Campos en una cuadrícula de 2 filas x 4 columnas
        JPanel campos = new JPanel(new GridLayout(2, 4, 8, 8));
        campos.add(new JLabel("Límite nocturno (L, máx. 5):"));
        campos.add(txtLimite);
        campos.add(new JLabel("Lectura inicial (ej. noche):"));
        campos.add(cmbInicial);
        campos.add(new JLabel("Lectura final (ej. madrugada):"));
        campos.add(cmbFinal);
        campos.add(new JLabel(""));
        campos.add(chkSinUso);
        campos.add(new JLabel(""));
        campos.add(new JLabel(""));

        JButton btnEjecutar = new JButton("Ejecutar Prueba de Fuga");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        botones.add(btnEjecutar);

        // Recordatorio del criterio nocturno
        JLabel lblCriterio = new JLabel("Criterio: de 00:00 a 05:00 el consumo debe ser casi cero "
                + "(tolerancia del medidor: 5 L). La lectura final debe ser de la madrugada.");
        lblCriterio.setFont(lblCriterio.getFont().deriveFont(Font.ITALIC));

        formulario.add(lblCriterio, BorderLayout.NORTH);
        formulario.add(campos, BorderLayout.CENTER);
        formulario.add(botones, BorderLayout.SOUTH);

        btnEjecutar.addActionListener(e -> ejecutarPrueba());

        return formulario;
    }

    /** Crea los controles para comparar el consumo reciente con el promedio previo. */
    private JPanel crearFormularioAnalisisHistorico() {
        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        formulario.setBorder(BorderFactory.createTitledBorder("Aumento frente al promedio histórico"));
        JTextField txtDesde = new JTextField(LocalDate.of(2026, 11, 1).toString(), 10);
        JTextField txtHasta = new JTextField(LocalDate.of(2026, 11, 4).toString(), 10);
        JTextField txtFactor = new JTextField("1.5", 5);
        JButton btnAnalizar = new JButton("Analizar consumo");
        formulario.add(new JLabel("Desde (AAAA-MM-DD):"));
        formulario.add(txtDesde);
        formulario.add(new JLabel("Hasta (AAAA-MM-DD):"));
        formulario.add(txtHasta);
        formulario.add(new JLabel("Factor (> 1):"));
        formulario.add(txtFactor);
        formulario.add(btnAnalizar);
        btnAnalizar.addActionListener(e -> analizarConsumoHistorico(txtDesde, txtHasta, txtFactor));
        return formulario;
    }

    /** Muestra el análisis histórico sin crear ni modificar alertas. */
    private void analizarConsumoHistorico(JTextField txtDesde, JTextField txtHasta, JTextField txtFactor) {
        Hogar hogar = selector.getHogar();
        if (hogar == null) {
            mostrarError("Debe seleccionar un hogar.");
            return;
        }
        LocalDate desde;
        LocalDate hasta;
        double factor;
        try {
            desde = LectorEntradas.leerFecha(txtDesde.getText(),
                    "Fecha inválida. Use el formato AAAA-MM-DD (por ejemplo 2026-11-05).");
            hasta = LectorEntradas.leerFecha(txtHasta.getText(),
                    "Fecha inválida. Use el formato AAAA-MM-DD (por ejemplo 2026-11-05).");
            factor = LectorEntradas.leerDecimal(txtFactor.getText(),
                    "El factor de aumento debe ser un número.");
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
            return;
        }
        ComparacionHistorica resultado = gestorFugas.analizarConsumoHistorico(hogar, desde, hasta, factor);
        if (resultado == null) {
            mostrarError(gestorFugas.getMensajeError());
            return;
        }
        JOptionPane.showMessageDialog(this, resultado.resumen(), "Análisis de consumo histórico",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /** Crea la tabla con el historial de pruebas del hogar. */
    private JScrollPane crearTabla() {
        String[] columnas = {"ID", "Lectura inicial", "Lectura final", "Consumo (L)", "L/hora",
            "Límite (L)", "Resultado", "Estado alerta"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Al seleccionar una prueba se muestra su resultado abajo
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarResultado(pruebaSeleccionada());
            }
        });
        return new JScrollPane(tabla);
    }

    /** Crea la zona de resultado: etiqueta de estado (verde o roja), recomendaciones y botones de la alerta. */
    private JPanel crearPanelResultado() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Resultado y alerta"));

        // Etiqueta grande que cambia de color
        lblEstado = new JLabel("Ejecute o seleccione una prueba", SwingConstants.CENTER);
        lblEstado.setOpaque(true);
        lblEstado.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblEstado.setForeground(Color.WHITE);
        lblEstado.setBackground(GRIS);
        lblEstado.setPreferredSize(new Dimension(100, 45));

        // Cuadro con las recomendaciones
        txtRecomendaciones = new JTextArea(6, 40);
        txtRecomendaciones.setEditable(false);
        txtRecomendaciones.setLineWrap(true);
        txtRecomendaciones.setWrapStyleWord(true);

        btnIniciarRevision = new JButton("Iniciar Revisión");
        btnCerrarAlerta = new JButton("Cerrar Alerta");
        btnIniciarRevision.setEnabled(false);
        btnCerrarAlerta.setEnabled(false);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        botones.add(btnIniciarRevision);
        botones.add(btnCerrarAlerta);

        panel.add(lblEstado, BorderLayout.NORTH);
        panel.add(new JScrollPane(txtRecomendaciones), BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);

        btnIniciarRevision.addActionListener(e -> iniciarRevision());
        btnCerrarAlerta.addActionListener(e -> cerrarAlerta());

        return panel;
    }

    // ---------- Acciones ----------

    /** Botón "Ejecutar Prueba de Fuga": el gestor evalúa la prueba; si hay un error de validación se muestra con JOptionPane. */
    private void ejecutarPrueba() {
        Hogar hogar = selector.getHogar();
        if (hogar == null) {
            mostrarError("Debe seleccionar un hogar.");
            return;
        }
        LecturaMedidor inicial = (LecturaMedidor) cmbInicial.getSelectedItem();
        LecturaMedidor fin = (LecturaMedidor) cmbFinal.getSelectedItem();

        double limite;
        try {
            if (txtLimite.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("Debe ingresar el límite de consumo nocturno en litros.");
            }
            limite = LectorEntradas.leerDecimal(txtLimite.getText(),
                    "El límite debe ser un número (por ejemplo 5.0).");
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
            return;
        }
        PruebaFuga prueba = gestorFugas.ejecutarPrueba(hogar, inicial, fin,
                limite, chkSinUso.isSelected());
        if (prueba == null) {
            JOptionPane.showMessageDialog(this, gestorFugas.getMensajeError(),
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
            return;
        }
        actualizarTabla();
        // Se selecciona la prueba recién creada (la última fila)
        int ultima = modeloTabla.getRowCount() - 1;
        if (ultima >= 0) {
            tabla.setRowSelectionInterval(ultima, ultima);
        }
    }

    /** Botón "Iniciar Revisión": pasa la alerta de la prueba seleccionada a "En revisión" y pregunta dónde se sospecha la fuga. */
    private void iniciarRevision() {
        if (selector.getHogar() == null) {
            mostrarError("Debe seleccionar un hogar.");
            return;
        }
        PruebaFuga prueba = pruebaSeleccionada();
        if (prueba == null || prueba.getAlerta() == null) {
            return;
        }
        String ubicacion = JOptionPane.showInputDialog(this,
                "¿Dónde sospecha que está la fuga? (opcional, por ejemplo: baño principal)",
                "Iniciar revisión", JOptionPane.QUESTION_MESSAGE);
        if (ubicacion == null) {
            return;   // el usuario canceló
        }
        if (gestorFugas.iniciarRevision(prueba.getAlerta(), ubicacion)) {
            refrescarManteniendoSeleccion();
        } else {
            JOptionPane.showMessageDialog(this, gestorFugas.getMensajeError(),
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Botón "Cerrar Alerta": pide la acción realizada y cierra la alerta de la prueba seleccionada. */
    private void cerrarAlerta() {
        if (selector.getHogar() == null) {
            mostrarError("Debe seleccionar un hogar.");
            return;
        }
        PruebaFuga prueba = pruebaSeleccionada();
        if (prueba == null || prueba.getAlerta() == null) {
            return;
        }
        String accion = JOptionPane.showInputDialog(this,
                "Describa la acción realizada (por ejemplo: se cambió el sello del inodoro):",
                "Cerrar alerta", JOptionPane.QUESTION_MESSAGE);
        if (accion == null) {
            return;
        }
        if (gestorFugas.cerrarAlerta(prueba.getAlerta(), accion)) {
            refrescarManteniendoSeleccion();
        } else {
            JOptionPane.showMessageDialog(this, gestorFugas.getMensajeError(),
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- Métodos auxiliares ----------

    /** Devuelve la prueba seleccionada en la tabla (null si no hay ninguna). */
    private PruebaFuga pruebaSeleccionada() {
        Hogar hogar = selector.getHogar();
        int fila = tabla.getSelectedRow();
        if (hogar == null || fila < 0 || fila >= hogar.obtenerPruebas().size()) {
            return null;
        }
        return hogar.obtenerPruebas().get(fila);
    }

    /** Muestra el resultado de la prueba: color, texto y recomendaciones. */
    private void mostrarResultado(PruebaFuga prueba) {
        btnIniciarRevision.setEnabled(false);
        btnCerrarAlerta.setEnabled(false);

        if (prueba == null) {
            lblEstado.setText("Ejecute o seleccione una prueba");
            lblEstado.setBackground(GRIS);
            txtRecomendaciones.setText("");
            return;
        }

        String datos = String.format("%.0f", prueba.calcularPerdidaLitros()) + " L en "
                + String.format("%.1f", prueba.calcularHoras()) + " h (límite: "
                + String.format("%.0f", prueba.getLimiteLitros()) + " L)";

        if (prueba.getResultado().equals(PruebaFuga.POSIBLE_FUGA)) {
            lblEstado.setText("POSIBLE FUGA - " + datos);
            lblEstado.setBackground(ROJO);

            AlertaFuga alerta = prueba.getAlerta();
            String texto = "Alerta #" + alerta.getId() + " - Estado: " + alerta.getEstado() + "\n";
            if (!alerta.getUbicacionSospechada().isEmpty()) {
                texto += "Ubicación sospechada: " + alerta.getUbicacionSospechada() + "\n";
            }
            if (!alerta.getAccionRealizada().isEmpty()) {
                texto += "Acción realizada: " + alerta.getAccionRealizada() + "\n";
            }
            texto += "\nRecomendaciones:\n" + alerta.getRecomendaciones();
            txtRecomendaciones.setText(texto);
            txtRecomendaciones.setCaretPosition(0);

            btnIniciarRevision.setEnabled(alerta.getEstado().equals(AlertaFuga.PENDIENTE));
            btnCerrarAlerta.setEnabled(!alerta.getEstado().equals(AlertaFuga.CERRADA));
        } else if (prueba.getResultado().equals(PruebaFuga.NORMAL)) {
            lblEstado.setText("NORMAL - " + datos);
            lblEstado.setBackground(VERDE);
            txtRecomendaciones.setText("No se detectó un consumo anormal durante el periodo analizado.");
        } else {
            lblEstado.setText("DATOS INVÁLIDOS");
            lblEstado.setBackground(GRIS);
            txtRecomendaciones.setText("La prueba no se pudo evaluar con los datos ingresados.");
        }
    }

    /** Actualiza la tabla y vuelve a seleccionar la misma fila. */
    private void refrescarManteniendoSeleccion() {
        int fila = tabla.getSelectedRow();
        actualizarTabla();
        if (fila >= 0 && fila < modeloTabla.getRowCount()) {
            tabla.setRowSelectionInterval(fila, fila);
        }
    }

    /** Llena la tabla con las pruebas del hogar seleccionado. */
    private void actualizarTabla() {
        modeloTabla.setRowCount(0);
        Hogar hogar = selector.getHogar();
        if (hogar == null) {
            mostrarResultado(null);
            return;
        }
        for (PruebaFuga p : hogar.obtenerPruebas()) {
            String estadoAlerta = (p.getAlerta() == null) ? "-" : p.getAlerta().getEstado();
            modeloTabla.addRow(new Object[]{
                p.getId(),
                p.getLecturaInicial().getFechaHora().format(formato),
                p.getLecturaFinal().getFechaHora().format(formato),
                String.format("%.0f", p.calcularPerdidaLitros()),
                String.format("%.1f", p.calcularPerdidaLHora()),
                String.format("%.0f", p.getLimiteLitros()),
                p.getResultado(),
                estadoAlerta
            });
        }
        mostrarResultado(null);
    }

    /** Llena los selectores de lectura con las lecturas del hogar elegido. */
    private void cargarLecturas() {
        LecturaMedidor inicialAnterior = (LecturaMedidor) cmbInicial.getSelectedItem();
        LecturaMedidor finalAnterior = (LecturaMedidor) cmbFinal.getSelectedItem();

        cmbInicial.removeAllItems();
        cmbFinal.removeAllItems();
        Hogar hogar = selector.getHogar();
        if (hogar == null) {
            return;
        }
        for (LecturaMedidor l : hogar.obtenerLecturas()) {
            cmbInicial.addItem(l);
            cmbFinal.addItem(l);
        }
        // Si seguían existiendo, se vuelven a seleccionar
        if (inicialAnterior != null) {
            cmbInicial.setSelectedItem(inicialAnterior);
        }
        if (finalAnterior != null) {
            cmbFinal.setSelectedItem(finalAnterior);
        } else if (cmbFinal.getItemCount() > 0) {
            cmbFinal.setSelectedIndex(cmbFinal.getItemCount() - 1);
        }
    }

    /** Se llama al cambiar de pestaña o de hogar: recarga las lecturas de las listas y la tabla de pruebas. */
    @Override
    public void actualizar() {
        cargarLecturas();
        actualizarTabla();
    }

    /** Muestra un mensaje de error de validación con JOptionPane. */
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de validación", JOptionPane.ERROR_MESSAGE);
    }
}
