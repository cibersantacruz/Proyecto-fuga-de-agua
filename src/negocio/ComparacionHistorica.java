package negocio;

import java.util.OptionalDouble;

/** Resultado de comparar el consumo de un periodo con el promedio histórico anterior. */
public final class ComparacionHistorica {

    public static final String NORMAL = "Normal";
    public static final String POSIBLE_FUGA = "Posible fuga";
    public static final String DATOS_INSUFICIENTES = "Datos insuficientes";

    private final String estado;
    private final OptionalDouble consumoDetectado;
    private final OptionalDouble promedioHistorico;
    private final double factorAumento;
    private final String motivo;

    ComparacionHistorica(String estado, OptionalDouble consumoDetectado,
                         OptionalDouble promedioHistorico, double factorAumento, String motivo) {
        this.estado = estado;
        this.consumoDetectado = consumoDetectado;
        this.promedioHistorico = promedioHistorico;
        this.factorAumento = factorAumento;
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public OptionalDouble getConsumoDetectado() {
        return consumoDetectado;
    }

    public OptionalDouble getPromedioHistorico() {
        return promedioHistorico;
    }

    public String getMotivo() {
        return motivo;
    }

    /** Devuelve un resumen listo para mostrar en la interfaz. */
    public String resumen() {
        String detectado = consumoDetectado.isPresent()
                ? String.format("%.2f L/día", consumoDetectado.getAsDouble()) : "No disponible";
        String historico = promedioHistorico.isPresent()
                ? String.format("%.2f L/día", promedioHistorico.getAsDouble()) : "No disponible";
        return "Estado: " + estado + "\n"
                + "Consumo del periodo: " + detectado + "\n"
                + "Promedio histórico: " + historico + "\n"
                + "Factor de aumento: " + String.format("%.2f", factorAumento) + "\n\n"
                + motivo;
    }
}
