package interfaz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import modelo.Hogar;
import modelo.MetaAhorro;
import negocio.GestorHogares;
import negocio.GestorMetas;

/**
 * Pestaña 5: metas de ahorro mensual. Permite establecer una meta (porcentaje de reducción) y compara
 * el consumo real diario del hogar con el objetivo y con el ideal de la OMS (100 litros por persona al día).
 */
public class PanelMetasAhorro extends PanelBase {

    // gestorHogares: para calcular el consumo ideal y el real
    // selector: vivienda elegida en la parte superior de la ventana
    // gestorMetas: lógica de negocio de las metas
    private GestorHogares gestorHogares;
    private SelectorHogar selector;
    private GestorMetas gestorMetas;

    // Campos del formulario
    private JTextField txtInicio;
    private JTextField txtFin;
    private JTextField txtReduccion;

    // Tabla de metas y etiqueta con el resumen del hogar
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JLabel lblResumen;

    /** Arma la pantalla y carga las metas del hogar seleccionado. */
    public PanelMetasAhorro(SelectorHogar selector, GestorHogares gestorHogares, GestorMetas gestorMetas) {
        this.selector = selector;
        this.gestorHogares = gestorHogares;
        this.gestorMetas = gestorMetas;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(crearFormulario(), BorderLayout.NORTH);
        add(crearTabla(), BorderLayout.CENTER);

        lblResumen = new JLabel(" ");
        lblResumen.setBorder(BorderFactory.createTitledBorder("Resumen del hogar"));
        add(lblResumen, BorderLayout.SOUTH);

        actualizar();
        prepararNuevaMeta();
    }

    // ---------- Construcción de la pantalla ----------

    /** Crea el formulario de la meta (reducción %, fecha de inicio y fecha fin) con el botón "Establecer Meta". */
    private JPanel crearFormulario() {
        JPanel formulario = new JPanel(new BorderLayout(5, 5));
        formulario.setBorder(BorderFactory.createTitledBorder("Nueva meta de ahorro"));

        txtInicio = new JTextField();
        txtFin = new JTextField();
        txtReduccion = new JTextField();

        JPanel campos = new JPanel(new GridLayout(1, 6, 8, 8));
        campos.add(new JLabel("Reducción deseada (%):"));
        campos.add(txtReduccion);
        campos.add(new JLabel("Fecha de inicio (AAAA-MM-DD):"));
        campos.add(txtInicio);
        campos.add(new JLabel("Fecha fin (AAAA-MM-DD):"));
        campos.add(txtFin);

        JButton btnEstablecer = new JButton("Establecer Meta");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        botones.add(btnEstablecer);

        formulario.add(campos, BorderLayout.CENTER);
        formulario.add(botones, BorderLayout.SOUTH);

        btnEstablecer.addActionListener(e -> establecerMeta());

        return formulario;
    }

    /** Crea la tabla de metas; la columna "Estado" se pinta de verde (cumplida), naranja (no cumplida) o rojo (excesivo). */
    private JScrollPane crearTabla() {
        String[] columnas = {"ID", "Inicio", "Fin", "Reducción (%)", "Ideal OMS (L/día)",
            "Objetivo meta (L/día)", "Consumo real (L/día)", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);

        // La columna "Estado" se pinta de verde, naranja o rojo
        tabla.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada,
                                                           boolean foco, int fila, int columna) {
                Component c = super.getTableCellRendererComponent(t, valor, seleccionada, foco, fila, columna);
                if (!seleccionada) {
                    String estado = String.valueOf(valor);
                    if (estado.equals(MetaAhorro.CUMPLIDA)) {
                        c.setForeground(new Color(0, 130, 60));
                    } else if (estado.equals(MetaAhorro.EXCESIVO)) {
                        c.setForeground(new Color(200, 30, 30));
                    } else if (estado.equals(MetaAhorro.NO_CUMPLIDA)) {
                        c.setForeground(new Color(210, 120, 0));
                    } else {
                        c.setForeground(Color.DARK_GRAY);
                    }
                }
                return c;
            }
        });
        return new JScrollPane(tabla);
    }

    // ---------- Acciones ----------

    /** Botón "Establecer Meta": crea la meta con los datos del formulario o muestra el error con JOptionPane. */
    private void establecerMeta() {
        Hogar hogar = selector.getHogar();
        if (hogar == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un hogar.",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
            return;
        }
        LocalDate inicio;
        LocalDate fin;
        double reduccion;
        try {
            if (txtInicio.getText().trim().isEmpty() || txtFin.getText().trim().isEmpty()
                    || txtReduccion.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("Todos los campos son obligatorios.");
            }
            inicio = LectorEntradas.leerFecha(txtInicio.getText(),
                    "Fecha de inicio inválida. Use el formato AAAA-MM-DD.");
            fin = LectorEntradas.leerFecha(txtFin.getText(),
                    "Fecha fin inválida. Use el formato AAAA-MM-DD.");
            reduccion = LectorEntradas.leerDecimal(txtReduccion.getText(),
                    "El porcentaje de reducción debe ser un número (por ejemplo 15).");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean ok = gestorMetas.crear(hogar, inicio, fin, reduccion);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Meta de ahorro establecida correctamente.",
                    "Meta creada", JOptionPane.INFORMATION_MESSAGE);
            prepararNuevaMeta();
            actualizarTabla();
        } else {
            JOptionPane.showMessageDialog(this, gestorMetas.getMensajeError(),
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Deja el formulario listo para una meta nueva: inicio hoy, fin en 30 días y reducción vacía. */
    private void prepararNuevaMeta() {
        LocalDate hoy = LocalDate.now();
        txtInicio.setText(hoy.toString());
        txtFin.setText(hoy.plusDays(30).toString());
        txtReduccion.setText("");
    }

    // ---------- Métodos auxiliares ----------
    /** Llena la tabla con las metas del hogar y las evalúa con el consumo actual. */
    private void actualizarTabla() {
        modeloTabla.setRowCount(0);
        Hogar hogar = selector.getHogar();
        if (hogar == null) {
            lblResumen.setText("No hay hogares registrados.");
            return;
        }
        double real = gestorHogares.calcularConsumoRealDiario(hogar);

        for (MetaAhorro m : hogar.obtenerMetas()) {
            String estado = m.evaluar(real);
            modeloTabla.addRow(new Object[]{
                m.getId(),
                m.getInicio().toString(),
                m.getFin().toString(),
                String.format("%.1f", m.getReduccionPorcentaje()),
                String.format("%.1f", m.calcularIdeal()),
                String.format("%.1f", m.calcularObjetivo()),
                real < 0 ? "Sin datos" : String.format("%.1f", real),
                estado
            });
        }

        if (real < 0) {
            lblResumen.setText("Este hogar aún no tiene lecturas suficientes para calcular su consumo real.");
        } else {
            lblResumen.setText("Habitantes: " + hogar.getHabitantes() + "  |  Consumo ideal OMS: "
                    + String.format("%.1f", gestorHogares.calcularConsumoIdeal(hogar))
                    + " L/día  |  Consumo real actual: " + String.format("%.1f", real) + " L/día");
        }
    }

    /** Se llama al cambiar de pestaña o de hogar: recarga la tabla de metas. */
    @Override
    public void actualizar() {
        actualizarTabla();
    }
}
