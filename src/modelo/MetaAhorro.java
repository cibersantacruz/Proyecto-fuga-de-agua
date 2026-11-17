package modelo;

import java.time.LocalDate;

/**
 * Meta de ahorro de agua de un hogar. Se define con un porcentaje de reducción
 * sobre el consumo ideal (referencia de la OMS: 100 L por persona al día).
 */
public class MetaAhorro extends EntidadBase {

    // Posibles estados de la meta
    public static final String CUMPLIDA = "Meta cumplida";
    public static final String NO_CUMPLIDA = "Meta no cumplida";
    public static final String EXCESIVO = "Consumo excesivo";
    public static final String SIN_DATOS = "Sin datos";

    // Atributos privados (encapsulamiento)
    private LocalDate inicio;
    private LocalDate fin;
    private double baseLPersonaDia;        // Consumo de referencia por persona (100 L)
    private double reduccionPorcentaje;    // Ej: 15 significa 15 % de ahorro
    private String descripcion;
    private int habitantesPeriodoBase;     // Habitantes cuando se creó la meta
    private String estado;

    /** Crea una meta. Empieza con el estado "Sin datos" hasta que se evalúe. */
    public MetaAhorro(int id, LocalDate inicio, LocalDate fin, double baseLPersonaDia,
                      double reduccionPorcentaje, int habitantesPeriodoBase) {
        super(id);
        this.inicio = inicio;
        this.fin = fin;
        this.baseLPersonaDia = baseLPersonaDia;
        this.reduccionPorcentaje = reduccionPorcentaje;
        this.habitantesPeriodoBase = habitantesPeriodoBase;
        this.descripcion = "Reducir " + reduccionPorcentaje + "% el consumo diario del hogar";
        this.estado = SIN_DATOS;
    }

    /** Consumo diario ideal del hogar = 100 L * habitantes que tenía cuando se creó la meta. */
    public double calcularIdeal() {
        return baseLPersonaDia * habitantesPeriodoBase;
    }

    /** Consumo diario que se quiere lograr: el ideal menos el porcentaje de reducción. */
    public double calcularObjetivo() {
        return calcularIdeal() * (1 - reduccionPorcentaje / 100.0);
    }

    /**
     * Compara el consumo diario actual con el objetivo y guarda el estado:
     *  - menor o igual al objetivo: "Meta cumplida"
     *  - mayor al objetivo pero menor o igual al ideal: "Meta no cumplida"
     *  - mayor al ideal: "Consumo excesivo"
     *  - consumo negativo (-1 significa que no hay datos): "Sin datos"
     */
    public String evaluar(double consumoActual) {
        if (consumoActual < 0) {
            estado = SIN_DATOS;
        } else if (consumoActual <= calcularObjetivo()) {
            estado = CUMPLIDA;
        } else if (consumoActual <= calcularIdeal()) {
            estado = NO_CUMPLIDA;
        } else {
            estado = EXCESIVO;
        }
        return estado;
    }

    /** Cambia el texto descriptivo de la meta. */
    public void actualizarDescripcion(String texto) {
        this.descripcion = texto;
    }

    // ---------- Getters y setters ----------
    public LocalDate getInicio() {
        return inicio;
    }

    public void setInicio(LocalDate inicio) {
        this.inicio = inicio;
    }

    public LocalDate getFin() {
        return fin;
    }

    public void setFin(LocalDate fin) {
        this.fin = fin;
    }

    public double getBaseLPersonaDia() {
        return baseLPersonaDia;
    }

    public double getReduccionPorcentaje() {
        return reduccionPorcentaje;
    }

    public void setReduccionPorcentaje(double reduccionPorcentaje) {
        this.reduccionPorcentaje = reduccionPorcentaje;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getHabitantesPeriodoBase() {
        return habitantesPeriodoBase;
    }

    public String getEstado() {
        return estado;
    }
}
