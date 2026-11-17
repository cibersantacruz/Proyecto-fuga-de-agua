package modelo;

import java.time.LocalDateTime;

/**
 * Alerta que se genera cuando una prueba de fuga detecta un consumo nocturno
 * mayor al límite permitido. Tiene un estado que va cambiando:
 * Pendiente → En revisión → Cerrada.
 * También guarda las recomendaciones, el lugar sospechoso y la acción realizada.
 */
public class AlertaFuga extends EntidadBase {

    // Posibles estados de la alerta
    public static final String PENDIENTE = "Pendiente";
    public static final String EN_REVISION = "En revisión";
    public static final String CERRADA = "Cerrada";

    // Atributos privados (encapsulamiento)
    // estado: "Pendiente", "En revisión" o "Cerrada"
    // ubicacionSospechada, accionRealizada y recomendaciones: textos que se llenan durante la revisión
    // fechaCreacion y fechaCierre: cuándo se creó y cuándo se cerró la alerta
    private String estado;
    private String observacion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaCierre;
    private String ubicacionSospechada;
    private String accionRealizada;
    private String recomendaciones;

    /** Crea una alerta nueva: empieza en estado "Pendiente" y guarda la fecha y hora actuales. */
    public AlertaFuga(int id) {
        super(id);
        this.estado = PENDIENTE;
        this.observacion = "";
        this.fechaCreacion = LocalDateTime.now();
        this.ubicacionSospechada = "";
        this.accionRealizada = "";
        this.recomendaciones = "";
    }

    /** Pasa la alerta de "Pendiente" a "En revisión". Si está en otro estado, no hace nada. */
    public void iniciarRevision() {
        if (estado.equals(PENDIENTE)) {
            estado = EN_REVISION;
        }
    }

    /** Guarda el lugar donde se sospecha que está la fuga (por ejemplo "baño principal"). */
    public void registrarUbicacion(String lugar) {
        this.ubicacionSospechada = lugar;
    }

    /** Guarda la acción que se realizó para resolver la fuga. */
    public void registrarAccion(String accion) {
        this.accionRealizada = accion;
    }

    /** Cierra la alerta y registra la fecha de cierre. */
    public void cerrar() {
        this.estado = CERRADA;
        this.fechaCierre = LocalDateTime.now();
    }

    // ---------- Getters y setters ----------
    public String getEstado() {
        return estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public String getUbicacionSospechada() {
        return ubicacionSospechada;
    }

    public String getAccionRealizada() {
        return accionRealizada;
    }

    public String getRecomendaciones() {
        return recomendaciones;
    }

    public void setRecomendaciones(String recomendaciones) {
        this.recomendaciones = recomendaciones;
    }
}
