package negocio;

import java.time.LocalDate;
import modelo.Hogar;
import modelo.MetaAhorro;

/**
 * Crea las metas de ahorro mensual de cada hogar.
 * La meta se calcula sobre el consumo ideal de la OMS (100 L por persona al día).
 */
public class GestorMetas extends GestorBase {

    // contadorId: da un número de ID único a cada elemento que se registra
    private int contadorId;

    /** Crea el gestor con el contador de ID de las metas iniciado en 1. */
    public GestorMetas() {
        contadorId = 1;
    }

    /**
     * Crea una meta de ahorro para el hogar. Valida que la fecha fin sea posterior a la de inicio
     * y que la reducción esté entre 0 y 100.
     */
    public boolean crear(Hogar hogar, LocalDate inicio, LocalDate fin, double reduccion) {
        if (hogar == null) {
            return error("Debe seleccionar un hogar.");
        }

        if (!fin.isAfter(inicio)) {
            return error("La fecha fin debe ser posterior a la fecha de inicio.");
        }
        if (Double.isNaN(reduccion) || Double.isInfinite(reduccion) || reduccion <= 0 || reduccion >= 100) {
            return error("El porcentaje de reducción debe estar entre 0 y 100.");
        }

        MetaAhorro meta = new MetaAhorro(contadorId, inicio, fin,
                Hogar.LITROS_POR_PERSONA_DIA, reduccion, hogar.getHabitantes());
        contadorId++;
        hogar.agregarMeta(meta);
        return true;
    }

}
