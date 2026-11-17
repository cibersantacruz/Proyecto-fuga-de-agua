package negocio;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import modelo.Hogar;
import modelo.LecturaMedidor;

/**
 * Clase de utilidad con cálculos de consumo a partir de listas de lecturas.
 * Solo tiene métodos estáticos (no se crean objetos de esta clase), por eso
 * es final y su constructor es privado.
 */
public final class CalculadoraConsumo {

    // Constructor privado: evita que se creen objetos de esta clase
    private CalculadoraConsumo() {
    }

    /**
     * Devuelve las lecturas del hogar cuya fecha está entre "desde" y "hasta" (ambas incluidas).
     * Si "desde" o "hasta" es null, ese límite no se aplica.
     */
    public static List<LecturaMedidor> lecturasEnRango(Hogar hogar, LocalDate desde, LocalDate hasta) {
        List<LecturaMedidor> resultado = new ArrayList<>();
        for (LecturaMedidor lectura : hogar.obtenerLecturas()) {
            LocalDate fecha = lectura.getFecha();
            if ((desde == null || !fecha.isBefore(desde))
                    && (hasta == null || !fecha.isAfter(hasta))) {
                resultado.add(lectura);
            }
        }
        return resultado;
    }

    /**
     * Días transcurridos entre la primera y la última lectura de la lista.
     * Devuelve 0 si hay menos de 2 lecturas. La lista debe venir ordenada por fecha.
     */
    public static double dias(List<LecturaMedidor> lecturas) {
        if (lecturas.size() < 2) {
            return 0;
        }
        LecturaMedidor primera = lecturas.get(0);
        LecturaMedidor ultima = lecturas.get(lecturas.size() - 1);
        long minutos = Duration.between(primera.getFechaHora(), ultima.getFechaHora()).toMinutes();
        return minutos / 1440.0;
    }

    /**
     * Consumo real promedio en litros por día = litros entre la primera y la última lectura / días.
     * Devuelve un OptionalDouble vacío si no se puede calcular (menos de 2 lecturas o 0 días).
     */
    public static OptionalDouble consumoRealDiario(List<LecturaMedidor> lecturas) {
        double dias = dias(lecturas);
        if (dias <= 0) {
            return OptionalDouble.empty();
        }
        LecturaMedidor primera = lecturas.get(0);
        LecturaMedidor ultima = lecturas.get(lecturas.size() - 1);
        return OptionalDouble.of(primera.litrosHasta(ultima) / dias);
    }
}
