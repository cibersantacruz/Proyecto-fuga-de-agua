package negocio;

import java.time.LocalDate;
import java.util.Locale;
import java.util.OptionalDouble;
import modelo.Hogar;

/**
 * Resultado de comparar el consumo real de un hogar con su consumo ideal en un periodo.
 * Es un objeto de solo lectura (todos sus atributos son final): lo crea
 * GestorHogares.comparar() y la interfaz solo lo muestra.
 */
public final class ComparacionConsumo {

    // Posibles dictámenes del estado del consumo
    public static final String EFICIENTE = "Consumo eficiente";
    public static final String EXCESIVO = "Consumo excesivo";
    public static final String SIN_DATOS = "Datos insuficientes";

    // Datos del hogar y del periodo que se comparó
    // (desde y hasta pueden ser null = sin límite; ideal y real están en litros por día;
    //  real queda vacío si no hay al menos 2 lecturas en el periodo)
    private final String responsable;
    private final int habitantes;
    private final String codigoMedidor;
    private final LocalDate desde;
    private final LocalDate hasta;
    private final int lecturasUsadas;
    private final double dias;
    private final double referenciaPorPersona;
    private final double ideal;
    private final OptionalDouble real;

    /** Constructor de paquete: solo las clases de "negocio" pueden crear una comparación. */
    ComparacionConsumo(Hogar hogar, LocalDate desde, LocalDate hasta,
                       int lecturasUsadas, double dias,
                       double referenciaPorPersona, double ideal, OptionalDouble real) {
        this.responsable = hogar.getNombreResponsable();
        this.habitantes = hogar.getHabitantes();
        this.codigoMedidor = hogar.getCodigoMedidor();
        this.desde = desde;
        this.hasta = hasta;
        this.lecturasUsadas = lecturasUsadas;
        this.dias = dias;
        this.referenciaPorPersona = referenciaPorPersona;
        this.ideal = ideal;
        this.real = real;
    }

    /** Consumo ideal diario en litros según la referencia elegida por persona. */
    public double getIdeal() {
        return ideal;
    }

    /** Consumo real promedio en litros por día (vacío si no hay datos suficientes). */
    public OptionalDouble getReal() {
        return real;
    }

    /** Cantidad de lecturas del periodo que se usaron en el cálculo. */
    public int getLecturasUsadas() {
        return lecturasUsadas;
    }

    /** Diferencia real - ideal en L/día (positiva = se gasta de más). Es 0 si no hay datos. */
    public double getDiferencia() {
        return real.isPresent() ? real.getAsDouble() - ideal : 0;
    }

    /** Dictamen: "Consumo eficiente" si el real es menor o igual al ideal, "Consumo excesivo" si no, o "Datos insuficientes". */
    public String getEstado() {
        if (!real.isPresent()) {
            return SIN_DATOS;
        }
        return real.getAsDouble() <= ideal ? EFICIENTE : EXCESIVO;
    }

    /** Arma un texto de varias líneas con el detalle de la comparación para mostrarlo en pantalla. */
    public String resumen() {
        // Texto que describe el periodo según qué fechas se indicaron
        String periodo;
        if (desde == null && hasta == null) {
            periodo = "todo el historial";
        } else if (desde == null) {
            periodo = "hasta " + hasta;
        } else if (hasta == null) {
            periodo = "desde " + desde;
        } else {
            periodo = desde + " a " + hasta;
        }

        StringBuilder texto = new StringBuilder();
        texto.append("Vivienda: ").append(responsable)
                .append(" (").append(habitantes).append(" habitantes) - Medidor ")
                .append(codigoMedidor).append('\n');
        texto.append("Periodo: ").append(periodo)
                .append(" | lecturas usadas: ").append(lecturasUsadas).append('\n');
        texto.append(String.format(Locale.US,
                "Consumo ideal diario (%.2f L/persona): %.2f L/día%n", referenciaPorPersona, ideal));
        if (real.isPresent()) {
            texto.append(String.format(Locale.US,
                    "Consumo real promedio (%.2f días): %.2f L/día%n", dias, real.getAsDouble()));
            texto.append(String.format(Locale.US, "Diferencia: %+.2f L/día%n", getDiferencia()));
        } else {
            texto.append("Consumo real: no hay al menos 2 lecturas en el periodo.\n");
        }
        texto.append("Dictamen: ").append(getEstado());
        return texto.toString();
    }
}
