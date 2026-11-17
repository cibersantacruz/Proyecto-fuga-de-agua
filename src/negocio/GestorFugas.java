package negocio;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import modelo.AlertaFuga;
import modelo.Hogar;
import modelo.LecturaMedidor;
import modelo.PruebaFuga;

/**
 * RF7: evalúa las pruebas de fuga con el criterio de consumo nocturno:
 * de 00:00 a 05:00 un hogar sin problemas consume casi 0 litros (tolerancia de 5 L
 * por el medidor). Si lo supera, el estado pasa a "Posible fuga" y se crea una
 * AlertaFuga con recomendaciones automáticas.
 */
public class GestorFugas extends GestorBase {

    // contadorPruebas y contadorAlertas: dan un ID único a cada prueba y a cada alerta nueva
    private int contadorPruebas;
    private int contadorAlertas;

    /** Crea el gestor con los contadores de pruebas y alertas iniciados en 1. */
    public GestorFugas() {
        contadorPruebas = 1;
        contadorAlertas = 1;
    }

    /**
     * Ejecuta una prueba de fuga con dos lecturas del mismo hogar.
     * Devuelve la prueba ya evaluada, o null si hubo un error de validación
     * (el mensaje se obtiene con getMensajeError()).
     */
    public PruebaFuga ejecutarPrueba(Hogar hogar, LecturaMedidor inicial, LecturaMedidor fin,
                                     double limite, boolean sinUsoConfirmado) {
        // Validaciones de los datos recibidos
        if (hogar == null) {
            error("Debe seleccionar un hogar.");
            return null;
        }
        // La prueba contiene las reglas para validar sus lecturas y el periodo nocturno.
        PruebaFuga prueba = new PruebaFuga(contadorPruebas, inicial, fin, sinUsoConfirmado, limite);
        String motivo = prueba.motivoInvalidez();
        if (motivo != null) {
            error(motivo);
            return null;
        }
        contadorPruebas++;
        prueba.evaluar();

        // Si supera el límite, el estado es "Posible fuga" y se genera la alerta con recomendaciones
        if (prueba.getResultado().equals(PruebaFuga.POSIBLE_FUGA)) {
            AlertaFuga alerta = new AlertaFuga(contadorAlertas);
            contadorAlertas++;
            alerta.setRecomendaciones(generarRecomendaciones());
            alerta.setObservacion("Se detectaron " + String.format("%.1f", prueba.calcularPerdidaLitros())
                    + " L en " + String.format("%.1f", prueba.calcularHoras())
                    + " h de madrugada, más que el límite de " + String.format("%.1f", limite) + " L.");
            prueba.setAlerta(alerta);
            prueba.registrarObservacion(alerta.getObservacion());
        } else {
            prueba.registrarObservacion("Consumo nocturno dentro de la tolerancia del medidor.");
        }

        hogar.registrarPrueba(prueba);
        return prueba;
    }

    /** Texto con las recomendaciones automáticas cuando hay posible fuga (grifos, tuberías, inodoros, mangueras). */
    public String generarRecomendaciones() {
        return "En la madrugada nadie usa agua, por lo que el medidor no debería avanzar.\n"
                + "1. Revise que todos los grifos (cocina, baños, patio) estén bien cerrados.\n"
                + "2. Inspeccione las tuberías visibles y las uniones en busca de humedad o goteo.\n"
                + "3. Revise los inodoros: una fuga silenciosa en el tanque es muy común\n"
                + "   (ponga unas gotas de colorante en el tanque y espere 15 minutos).\n"
                + "4. Verifique las mangueras de lavadora y de jardín.\n"
                + "5. Si el medidor sigue avanzando con todo cerrado, llame a un plomero.";
    }

    /** Pasa la alerta de "Pendiente" a "En revisión" y guarda, si se indicó, el lugar sospechoso. */
    public boolean iniciarRevision(AlertaFuga alerta, String ubicacion) {
        if (alerta == null) {
            return error("Seleccione una prueba que tenga una alerta.");
        }
        if (!alerta.getEstado().equals(AlertaFuga.PENDIENTE)) {
            return error("Solo se puede iniciar la revisión de una alerta pendiente.");
        }
        alerta.iniciarRevision();
        if (ubicacion != null && !ubicacion.trim().isEmpty()) {
            alerta.registrarUbicacion(ubicacion.trim());
        }
        return true;
    }

    /** Cierra la alerta guardando la acción realizada (la acción es obligatoria). */
    public boolean cerrarAlerta(AlertaFuga alerta, String accion) {
        if (alerta == null) {
            return error("Seleccione una prueba que tenga una alerta.");
        }
        if (alerta.getEstado().equals(AlertaFuga.CERRADA)) {
            return error("La alerta ya está cerrada.");
        }
        if (accion == null || accion.trim().isEmpty()) {
            return error("Escriba la acción realizada para poder cerrar la alerta.");
        }
        alerta.registrarAccion(accion.trim());
        alerta.cerrar();
        return true;
    }

    /**
     * Compara el consumo diario promedio del periodo con el promedio previo al periodo.
     * Este análisis informa el resultado y no crea una alerta.
     */
    public ComparacionHistorica analizarConsumoHistorico(Hogar hogar, LocalDate desde,
                                                          LocalDate hasta, double factorAumento) {
        if (hogar == null) {
            error("Debe seleccionar un hogar.");
            return null;
        }
        if (desde == null || hasta == null) {
            error("Debe indicar las fechas desde y hasta.");
            return null;
        }
        if (desde.isAfter(hasta)) {
            error("La fecha «Desde» no puede ser posterior a la fecha «Hasta».");
            return null;
        }
        if (Double.isNaN(factorAumento) || Double.isInfinite(factorAumento) || factorAumento <= 1) {
            error("El factor de aumento debe ser mayor que 1.");
            return null;
        }

        List<LecturaMedidor> lecturasPeriodo = CalculadoraConsumo.lecturasEnRango(hogar, desde, hasta);
        List<LecturaMedidor> lecturasHistoricas = desde.equals(LocalDate.MIN)
                ? Collections.<LecturaMedidor>emptyList()
                : CalculadoraConsumo.lecturasEnRango(hogar, null, desde.minusDays(1));
        OptionalDouble consumoPeriodo = CalculadoraConsumo.consumoRealDiario(lecturasPeriodo);
        OptionalDouble promedioHistorico = CalculadoraConsumo.consumoRealDiario(lecturasHistoricas);

        if (!consumoPeriodo.isPresent() || !promedioHistorico.isPresent()) {
            String motivo;
            if (!consumoPeriodo.isPresent() && !promedioHistorico.isPresent()) {
                motivo = "Se necesitan al menos 2 lecturas válidas dentro del periodo y 2 anteriores a él.";
            } else if (!consumoPeriodo.isPresent()) {
                motivo = "Se necesitan al menos 2 lecturas válidas dentro del periodo.";
            } else {
                motivo = "Se necesitan al menos 2 lecturas válidas anteriores al periodo.";
            }
            return new ComparacionHistorica(ComparacionHistorica.DATOS_INSUFICIENTES,
                    consumoPeriodo, promedioHistorico, factorAumento, motivo);
        }

        double consumo = consumoPeriodo.getAsDouble();
        double historico = promedioHistorico.getAsDouble();
        if (consumo > historico * factorAumento) {
            String motivo = String.format("El consumo del periodo supera %.2f veces el promedio histórico.",
                    factorAumento);
            return new ComparacionHistorica(ComparacionHistorica.POSIBLE_FUGA,
                    consumoPeriodo, promedioHistorico, factorAumento, motivo);
        }
        return new ComparacionHistorica(ComparacionHistorica.NORMAL, consumoPeriodo,
                promedioHistorico, factorAumento,
                "El consumo del periodo no supera el umbral de aumento configurado.");
    }

}
