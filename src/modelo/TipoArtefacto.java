package modelo;

/** Tipo de artefacto disponible en el catálogo de consumo. */
public class TipoArtefacto {

    public static final String UNIDAD_MINUTO = "L/min";
    public static final String UNIDAD_DESCARGA = "L/descarga";
    public static final String UNIDAD_CICLO = "L/ciclo";

    private String nombre;
    private double caudalPredeterminado;
    private String unidad;

    public TipoArtefacto(String nombre, double caudalPredeterminado, String unidad) {
        this.nombre = nombre;
        this.caudalPredeterminado = caudalPredeterminado;
        this.unidad = unidad;
    }

    public String getNombre() {
        return nombre;
    }

    public double getCaudalPredeterminado() {
        return caudalPredeterminado;
    }

    public String getUnidad() {
        return unidad;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
