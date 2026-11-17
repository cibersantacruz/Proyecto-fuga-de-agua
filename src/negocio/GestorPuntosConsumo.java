package negocio;

import modelo.Hogar;
import modelo.CatalogoArtefactos;
import modelo.PuntoConsumo;
import modelo.TipoArtefacto;

/**
 * RF3 y RF4: registra, actualiza y elimina los artefactos de consumo de agua de cada hogar y
 * calcula su consumo estimado (volumen diario, mensual y porcentaje de impacto).
 */
public class GestorPuntosConsumo extends GestorBase {

    // contadorId: da un número de ID único a cada elemento que se registra
    private int contadorId;

    /** Crea el gestor con el contador de ID de los puntos iniciado en 1. */
    public GestorPuntosConsumo() {
        contadorId = 1;
    }

    // ---------- RF3: Gestión de puntos de consumo ----------

    /**
     * Registra un artefacto en el hogar. Devuelve false si hay un error de validación.
     *
     * @param ubicacion            dónde está el artefacto (ej. "Baño principal")
     * @param artefacto            tipo elegido en el catálogo
     * @param nombrePersonalizado  nombre escrito por el usuario (solo se usa si eligió "Otros")
     */
    public boolean registrar(Hogar hogar, String ubicacion, TipoArtefacto artefacto,
                             String nombrePersonalizado, double caudal, double duracion, int frecuencia) {
        if (hogar == null) {
            return error("Debe seleccionar un hogar.");
        }
        if (!validarDatos(ubicacion, artefacto, nombrePersonalizado, caudal, duracion, frecuencia)) {
            return false;
        }

        // Si eligió "Otros", el artefacto se guarda con el nombre que escribió el usuario
        String tipo = obtenerTipo(artefacto, nombrePersonalizado);
        String unidad = artefacto.getUnidad();

        hogar.agregarPunto(new PuntoConsumo(contadorId, ubicacion.trim(), tipo,
                caudal, unidad, duracion, frecuencia));
        contadorId++;
        return true;
    }

    /** Actualiza los datos de un artefacto existente del hogar. */
    public boolean actualizar(Hogar hogar, int idPunto, String ubicacion, TipoArtefacto artefacto,
                              String nombrePersonalizado, double caudal, double duracion, int frecuencia) {
        if (hogar == null) {
            return error("Debe seleccionar un hogar.");
        }
        PuntoConsumo punto = buscarPunto(hogar, idPunto);
        if (punto == null) {
            return error("No se encontró el artefacto seleccionado.");
        }
        if (!validarDatos(ubicacion, artefacto, nombrePersonalizado, caudal, duracion, frecuencia)) {
            return false;
        }
        punto.setNombre(ubicacion.trim());
        punto.setTipo(obtenerTipo(artefacto, nombrePersonalizado));
        punto.setCaudal(caudal);
        punto.setUnidad(artefacto.getUnidad());
        punto.setDuracionMinutos(duracion);
        punto.setFrecuenciaDiaria(frecuencia);
        return true;
    }

    /** Comparte entre registrar y actualizar las reglas de los datos de un punto de consumo. */
    private boolean validarDatos(String ubicacion, TipoArtefacto artefacto, String nombrePersonalizado,
                                 double caudal, double duracion, int frecuencia) {
        if (ubicacion == null || ubicacion.trim().isEmpty()) {
            return error("Debe ingresar el nombre o la ubicación del artefacto (por ejemplo: Baño principal).");
        }
        if (artefacto == null) {
            return error("Debe seleccionar un tipo de artefacto.");
        }
        boolean esOtros = artefacto.getNombre().equals(CatalogoArtefactos.OTROS);
        if (esOtros && (nombrePersonalizado == null || nombrePersonalizado.trim().isEmpty())) {
            return error("Escriba el nombre del artefacto personalizado.");
        }
        boolean usaDuracion = artefacto.getUnidad().equals(TipoArtefacto.UNIDAD_MINUTO);
        if (Double.isNaN(caudal) || Double.isInfinite(caudal) || caudal <= 0) {
            return error("El caudal debe ser mayor que 0.");
        }
        if (usaDuracion && (Double.isNaN(duracion) || Double.isInfinite(duracion) || duracion <= 0)) {
            return error("La duración debe ser mayor que 0.");
        }
        if (frecuencia <= 0) {
            return error("La frecuencia diaria debe ser mayor que 0.");
        }
        if (usaDuracion && duracion * frecuencia > 1440) {
            return error("El tiempo total de uso al día no puede superar 1440 minutos (24 horas).");
        }
        return true;
    }

    private String obtenerTipo(TipoArtefacto artefacto, String nombrePersonalizado) {
        return artefacto.getNombre().equals(CatalogoArtefactos.OTROS)
                ? nombrePersonalizado.trim() : artefacto.getNombre();
    }

    private PuntoConsumo buscarPunto(Hogar hogar, int idPunto) {
        for (PuntoConsumo punto : hogar.obtenerPuntos()) {
            if (punto.getId() == idPunto) {
                return punto;
            }
        }
        return null;
    }

    /** Da de baja un artefacto del hogar. */
    public boolean eliminar(Hogar hogar, int idPunto) {
        if (hogar == null) {
            return error("Debe seleccionar un hogar.");
        }
        PuntoConsumo punto = buscarPunto(hogar, idPunto);
        if (punto != null) {
            return hogar.eliminarPunto(punto);
        }
        return error("No se encontró el artefacto seleccionado.");
    }

    // ---------- RF4: Cálculos de consumo estimado ----------

    /** Suma del volumen diario (litros) de todos los artefactos del hogar. */
    public double calcularTotalDiarioLitros(Hogar hogar) {
        double total = 0;
        for (PuntoConsumo p : hogar.obtenerPuntos()) {
            total += p.calcularVolumenDiario();
        }
        return total;
    }

    /** Suma del consumo mensual (m3) de todos los artefactos del hogar. */
    public double calcularTotalMensualM3(Hogar hogar) {
        double total = 0;
        for (PuntoConsumo p : hogar.obtenerPuntos()) {
            total += p.calcularConsumoMensualM3();
        }
        return total;
    }

    /** Porcentaje que representa un artefacto frente al consumo total estimado del hogar. */
    public double calcularPorcentajeImpacto(Hogar hogar, PuntoConsumo punto) {
        return punto.calcularPorcentajeImpacto(calcularTotalMensualM3(hogar));
    }

}
