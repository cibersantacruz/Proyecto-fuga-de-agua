package modelo;

/**
 * Representa un artefacto o punto donde se usa agua en la vivienda
 * (ducha, lavadora, inodoro, grifo de cocina, manguera, etc.).
 *
 * El caudal puede venir en distintas unidades:
 *  - L/min      (ducha, grifo, manguera): el volumen depende de la duración del uso.
 *  - L/descarga (inodoro) y L/ciclo (lavadora): el volumen depende solo de las veces que se usa.
 */
public class PuntoConsumo extends EntidadBase {

    // ---------- Atributos ----------
    // nombre: ubicación del artefacto (por ejemplo "Baño principal")
    // tipo: artefacto elegido (por ejemplo "Ducha") o el nombre escrito si eligió "Otros"
    // caudal: caudal en la unidad indicada (L/min, L/descarga o L/ciclo)
    // duracionMinutos: duración de cada uso (solo se usa con L/min)
    // frecuenciaDiaria: veces que se usa al día
    private String nombre;
    private String tipo;
    private double caudal;
    private String unidad;
    private double duracionMinutos;
    private int frecuenciaDiaria;

    /** Crea un artefacto con todos sus datos. */
    public PuntoConsumo(int id, String nombre, String tipo, double caudal, String unidad,
                        double duracionMinutos, int frecuenciaDiaria) {
        super(id);
        this.nombre = nombre;
        this.tipo = tipo;
        this.caudal = caudal;
        this.unidad = unidad;
        this.duracionMinutos = duracionMinutos;
        this.frecuenciaDiaria = frecuenciaDiaria;
    }

    /** Indica si el volumen depende de la duración del uso (solo cuando el caudal está en L/min). */
    public boolean usaDuracion() {
        return unidad.equals(TipoArtefacto.UNIDAD_MINUTO);
    }

    /**
     * Volumen Diario (L):
     *  - L/min:                Caudal * Duración (min) * Frecuencia (veces/día)
     *  - L/descarga o L/ciclo: Caudal * Frecuencia (veces/día)
     */
    public double calcularVolumenDiario() {
        if (usaDuracion()) {
            return caudal * duracionMinutos * frecuenciaDiaria;
        }
        return caudal * frecuenciaDiaria;
    }

    /** Consumo Mensual (m3) = (Volumen Diario * 30) / 1000. */
    public double calcularConsumoMensualM3() {
        return (calcularVolumenDiario() * 30) / 1000.0;
    }

    /** Porcentaje de Impacto (%) = (Consumo mensual del punto / Consumo mensual total del hogar) * 100. */
    public double calcularPorcentajeImpacto(double consumoMensualTotalM3) {
        if (consumoMensualTotalM3 <= 0) {
            return 0;
        }
        return (calcularConsumoMensualM3() / consumoMensualTotalM3) * 100;
    }

    // ---------- Getters y setters ----------

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getCaudal() {
        return caudal;
    }

    public void setCaudal(double caudal) {
        this.caudal = caudal;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public double getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(double duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public int getFrecuenciaDiaria() {
        return frecuenciaDiaria;
    }

    public void setFrecuenciaDiaria(int frecuenciaDiaria) {
        this.frecuenciaDiaria = frecuenciaDiaria;
    }
}
