package modelo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Prueba para detectar una posible fuga: se comparan dos lecturas del
 * medidor (la de la noche y la de la madrugada) cuando nadie usa agua.
 *
 * Criterio nocturno: entre las 00:00 y las 05:00 el consumo de un hogar sin
 * problemas debería ser prácticamente cero. Se permite una tolerancia pequeña
 * (5 litros) por la precisión del medidor. Si el consumo supera el límite sin
 * uso justificado, el resultado es "Posible fuga".
 */
public class PruebaFuga extends EntidadBase {

    // Posibles resultados de la prueba
    public static final String NORMAL = "Normal";
    public static final String POSIBLE_FUGA = "Posible fuga";
    public static final String DATOS_INVALIDOS = "Datos inválidos";

    // Criterio de consumo nocturno
    // TOLERANCIA_LITROS: máximo permitido por la precisión del medidor
    // HORA_FIN_MADRUGADA: la madrugada llega hasta las 05:00
    // DURACION_MAXIMA_HORAS: una prueba nocturna no puede ser más larga
    public static final double TOLERANCIA_LITROS = 5.0;
    public static final int HORA_FIN_MADRUGADA = 5;
    public static final double DURACION_MAXIMA_HORAS = 8.0;

    // Atributos privados
    // lecturaInicial y lecturaFinal: las dos lecturas que se comparan (noche y madrugada)
    // sinUsoConfirmado: el usuario confirma que nadie usó agua en ese periodo
    // limiteLitros: consumo máximo permitido (de 0 a 5 litros)
    // resultado: "Normal", "Posible fuga" o "Datos inválidos"
    // alerta: solo existe si el resultado es "Posible fuga" (0..1)
    private LecturaMedidor lecturaInicial;
    private LecturaMedidor lecturaFinal;
    private boolean sinUsoConfirmado;
    private double limiteLitros;
    private String resultado;
    private LocalDateTime fechaRegistro;
    private String observaciones;
    private AlertaFuga alerta;

    /** Crea una prueba sin evaluar; la fecha de registro es la fecha y hora actuales. */
    public PruebaFuga(int id, LecturaMedidor lecturaInicial, LecturaMedidor lecturaFinal,
                      boolean sinUsoConfirmado, double limiteLitros) {
        super(id);
        this.lecturaInicial = lecturaInicial;
        this.lecturaFinal = lecturaFinal;
        this.sinUsoConfirmado = sinUsoConfirmado;
        this.limiteLitros = limiteLitros;
        this.resultado = "Sin evaluar";
        this.fechaRegistro = LocalDateTime.now();
        this.observaciones = "";
        this.alerta = null;
    }

    /** Indica si la lectura final fue tomada en la madrugada (de 00:00 a 05:00). */
    public boolean finalEnMadrugada() {
        LocalTime hora = lecturaFinal.getHora();
        return !hora.isAfter(LocalTime.of(HORA_FIN_MADRUGADA, 0));
    }

    /**
     * Revisa que los datos sean coherentes con el criterio nocturno: lecturas en orden,
     * la final en la madrugada, duración máxima de 8 horas, límite entre 0 y 5 litros
     * y la confirmación de que nadie usó agua.
     */
    public boolean esValida() {
        return motivoInvalidez() == null;
    }

    /** Devuelve el motivo de invalidez o null si la prueba cumple sus reglas. */
    public String motivoInvalidez() {
        if (lecturaInicial == null || lecturaFinal == null) {
            return "Debe seleccionar la lectura inicial y la lectura final.\n"
                    + "Si el hogar tiene menos de dos lecturas, registre más en la pestaña Lecturas.";
        }
        if (lecturaInicial == lecturaFinal) {
            return "La lectura inicial y la final deben ser distintas.";
        }
        if (!lecturaFinal.getFechaHora().isAfter(lecturaInicial.getFechaHora())) {
            return "La lectura final debe ser posterior a la lectura inicial.";
        }
        if (Double.isNaN(limiteLitros) || Double.isInfinite(limiteLitros) || limiteLitros < 0) {
            return "El límite de consumo no puede ser negativo.";
        }
        if (limiteLitros > TOLERANCIA_LITROS) {
            return "En la madrugada el consumo debe ser casi cero. El límite no puede superar "
                    + TOLERANCIA_LITROS + " L (tolerancia del medidor).";
        }
        if (!sinUsoConfirmado) {
            return "Para ejecutar la prueba debe confirmar que no se usó agua en la vivienda durante el periodo.";
        }
        if (!finalEnMadrugada()) {
            return "La lectura final debe ser de la madrugada (entre 00:00 y "
                    + String.format("%02d:00", HORA_FIN_MADRUGADA) + ").";
        }
        if (calcularHoras() > DURACION_MAXIMA_HORAS) {
            return "Una prueba nocturna no puede durar más de " + (int) DURACION_MAXIMA_HORAS
                    + " horas. Elija una lectura inicial de la noche anterior (por ejemplo 23:00).";
        }
        if (lecturaFinal.getAcumuladoM3() < lecturaInicial.getAcumuladoM3()) {
            return "La lectura final no puede ser menor que la lectura inicial.";
        }
        return null;
    }

    /** Litros consumidos entre la lectura inicial y la final (redondeado a 2 decimales). */
    public double calcularPerdidaLitros() {
        double litros = lecturaInicial.litrosHasta(lecturaFinal);

        // Se redondea para evitar errores de decimales (por ejemplo 5.0000000001 L)
        return Math.round(litros * 100.0) / 100.0;
    }

    /** Litros por hora consumidos durante la prueba (0 si la duración no es válida). */
    public double calcularPerdidaLHora() {
        double horas = calcularHoras();
        if (horas <= 0) {
            return 0;
        }
        return calcularPerdidaLitros() / horas;
    }

    /** Duración de la prueba en horas (diferencia entre las dos lecturas). */
    public double calcularHoras() {
        long minutos = Duration.between(lecturaInicial.getFechaHora(), lecturaFinal.getFechaHora()).toMinutes();
        return minutos / 60.0;
    }

    /** Evalúa la prueba y guarda el resultado: "Datos inválidos", "Posible fuga" (si supera el límite) o "Normal". */
    public String evaluar() {
        if (!esValida()) {
            resultado = DATOS_INVALIDOS;
        } else if (calcularPerdidaLitros() > limiteLitros) {
            resultado = POSIBLE_FUGA;
        } else {
            resultado = NORMAL;
        }
        return resultado;
    }

    /** Guarda una observación sobre el resultado de la prueba. */
    public void registrarObservacion(String texto) {
        this.observaciones = texto;
    }

    // ---------- Getters y setters ----------
    public LecturaMedidor getLecturaInicial() {
        return lecturaInicial;
    }

    public LecturaMedidor getLecturaFinal() {
        return lecturaFinal;
    }

    public boolean isSinUsoConfirmado() {
        return sinUsoConfirmado;
    }

    public double getLimiteLitros() {
        return limiteLitros;
    }

    public void setLimiteLitros(double limiteLitros) {
        this.limiteLitros = limiteLitros;
    }

    public String getResultado() {
        return resultado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public AlertaFuga getAlerta() {
        return alerta;
    }

    public void setAlerta(AlertaFuga alerta) {
        this.alerta = alerta;
    }
}
