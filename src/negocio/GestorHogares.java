package negocio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import modelo.Hogar;
import modelo.LecturaMedidor;

/**
 * RF1 y RF2: administra los hogares (registrar, consultar, actualizar y eliminar)
 * y compara el consumo ideal con el consumo real.
 */
public class GestorHogares extends GestorBase {

    // hogares: lista de viviendas registradas
    private List<Hogar> hogares;

    /** Crea el gestor con la lista de hogares vacía. */
    public GestorHogares() {
        hogares = new ArrayList<>();
    }

    // ---------- RF1: Gestionar viviendas ----------

    /**
     * Registra un hogar nuevo. Valida que el ID y los habitantes sean válidos y que no se repitan
     * el ID ni el código de medidor.
     * Devuelve false (y guarda el mensaje de error) si algo falla.
     */
    public boolean registrar(int id, String responsable, String direccion, String medidor, int habitantes) {

        if (!validarTextos(responsable, direccion, medidor)) {
            return error("Todos los campos son obligatorios.");
        }
        if (id <= 0) {
            return error("El ID debe ser mayor que 0.");
        }
        if (habitantes < 1) {
            return error("El número de habitantes debe ser mayor o igual a 1.");
        }
        if (buscarPorId(id) != null) {
            return error("Ya existe un hogar con ese ID.");
        }
        if (buscarPorMedidor(medidor.trim()) != null) {
            return error("Ya existe un hogar con ese código de medidor.");
        }

        hogares.add(new Hogar(id, responsable.trim(), direccion.trim(), medidor.trim(), habitantes));
        return true;
    }

    /** Actualiza los datos de un hogar existente, sin modificar sus metas. */
    public boolean actualizar(int id, String responsable, String direccion, String medidor, int habitantes) {
        if (!validarTextos(responsable, direccion, medidor)) {
            return error("Todos los campos son obligatorios.");
        }
        Hogar hogar = buscarPorId(id);
        if (hogar == null) {
            return error("No existe un hogar con el ID " + id + ".");
        }
        if (habitantes < 1) {
            return error("El número de habitantes debe ser mayor o igual a 1.");
        }
        Hogar medidorExistente = buscarPorMedidor(medidor.trim());
        if (medidorExistente != null && medidorExistente != hogar) {
            return error("Ya existe un hogar con ese código de medidor.");
        }
        hogar.actualizarDatos(responsable.trim(), direccion.trim(), medidor.trim(), habitantes);
        return true;
    }

    private boolean validarTextos(String responsable, String direccion, String medidor) {
        return responsable != null && direccion != null && medidor != null
                && !responsable.trim().isEmpty()
                && !direccion.trim().isEmpty()
                && !medidor.trim().isEmpty();
    }

    /** Elimina un hogar por su ID (junto con todos sus datos). */
    public boolean eliminar(int id) {
        Hogar hogar = buscarPorId(id);
        if (hogar == null) {
            return error("No existe un hogar con el ID " + id + ".");
        }
        hogares.remove(hogar);
        return true;
    }

    /** Busca un hogar por su ID. Devuelve null si no existe. */
    public Hogar buscarPorId(int id) {
        for (Hogar h : hogares) {
            if (h.getId() == id) {
                return h;
            }
        }
        return null;
    }

    /** Busca un hogar por su código de medidor (sin distinguir mayúsculas). Devuelve null si no existe. */
    private Hogar buscarPorMedidor(String codigo) {
        for (Hogar h : hogares) {
            if (h.getCodigoMedidor().equalsIgnoreCase(codigo)) {
                return h;
            }
        }
        return null;
    }

    /** Devuelve la lista de hogares registrados. */
    public List<Hogar> getHogares() {
        return Collections.unmodifiableList(hogares);
    }

    // ---------- RF2: Consumo ideal vs consumo real ----------

    /** Consumo ideal diario del hogar en litros. */
    public double calcularConsumoIdeal(Hogar hogar) {
        return hogar.calcularConsumoIdeal();
    }

    /** Consumo ideal diario con el valor de referencia elegido, en litros por persona. */
    public double calcularConsumoIdeal(Hogar hogar, double referenciaPorPersona) {
        return hogar.calcularConsumoIdeal(referenciaPorPersona);
    }

    /**
     * Consumo real diario (litros por día), calculado con la primera y la última lectura
     * del medidor del hogar. Devuelve -1 si no hay datos suficientes.
     */
    public double calcularConsumoRealDiario(Hogar hogar) {
        OptionalDouble real = CalculadoraConsumo.consumoRealDiario(hogar.obtenerLecturas());
        return real.isPresent() ? real.getAsDouble() : -1;
    }

    /**
     * Compara el consumo real con el ideal usando solo las lecturas entre "desde" y "hasta"
     * (null = sin límite). Devuelve null si el hogar es null o si las fechas están al revés
     * (el mensaje queda en getMensajeError()).
     */
    public ComparacionConsumo comparar(Hogar hogar, LocalDate desde, LocalDate hasta) {
        return comparar(hogar, desde, hasta, Hogar.LITROS_POR_PERSONA_DIA);
    }

    /** Compara consumo real e ideal usando la referencia indicada en litros por persona al día. */
    public ComparacionConsumo comparar(Hogar hogar, LocalDate desde, LocalDate hasta,
                                       double referenciaPorPersona) {
        if (hogar == null) {
            error("Debe seleccionar un hogar.");
            return null;
        }
        if (Double.isNaN(referenciaPorPersona) || Double.isInfinite(referenciaPorPersona)
                || referenciaPorPersona <= 0) {
            error("El consumo de referencia por persona debe ser mayor que 0.");
            return null;
        }
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            error("La fecha «Desde» no puede ser posterior a la fecha «Hasta».");
            return null;
        }
        List<LecturaMedidor> lecturas = CalculadoraConsumo.lecturasEnRango(hogar, desde, hasta);
        double dias = CalculadoraConsumo.dias(lecturas);
        double ideal = calcularConsumoIdeal(hogar, referenciaPorPersona);
        OptionalDouble real = CalculadoraConsumo.consumoRealDiario(lecturas);
        return new ComparacionConsumo(hogar, desde, hasta, lecturas.size(), dias,
                referenciaPorPersona, ideal, real);
    }

    /** Diferencia entre el consumo real y el ideal (positivo = se gasta de más). Es 0 si no hay datos. */
    public double calcularDiferencia(Hogar hogar) {
        double real = calcularConsumoRealDiario(hogar);
        if (real < 0) {
            return 0;
        }
        return real - calcularConsumoIdeal(hogar);
    }

    /** Devuelve el estado del consumo con los nombres usados en la comparación. */
    public String obtenerEstadoConsumo(Hogar hogar) {
        double real = calcularConsumoRealDiario(hogar);
        if (real < 0) {
            return ComparacionConsumo.SIN_DATOS;
        }
        if (real <= calcularConsumoIdeal(hogar)) {
            return ComparacionConsumo.EFICIENTE;
        }
        return ComparacionConsumo.EXCESIVO;
    }

}
