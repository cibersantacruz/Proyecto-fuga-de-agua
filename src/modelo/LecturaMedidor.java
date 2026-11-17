package modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa una lectura del medidor de agua en metros cúbicos (m3),
 * con la fecha y la hora en que se tomó.
 */
public class LecturaMedidor extends EntidadBase {

    // Atributos privados (encapsulamiento)
    private LocalDateTime fechaHora;
    private double acumuladoM3;
    private String codigoMedidor;
    private String origenDato;     // Ej: "Manual"
    private String observacion;

    /** Crea una lectura. El origen del dato queda como "Manual" y la observación vacía. */
    public LecturaMedidor(int id, LocalDateTime fechaHora, double acumuladoM3, String codigoMedidor) {
        super(id);
        this.fechaHora = fechaHora;
        this.acumuladoM3 = acumuladoM3;
        this.codigoMedidor = codigoMedidor;
        this.origenDato = "Manual";
        this.observacion = "";
    }

    /** Revisa que la lectura tenga fecha y que el valor del medidor no sea negativo. */
    public boolean validar() {
        return fechaHora != null && acumuladoM3 >= 0;
    }

    /** Litros consumidos entre esta lectura y otra posterior (1 m3 = 1000 litros). */
    public double litrosHasta(LecturaMedidor fin) {
        return (fin.getAcumuladoM3() - this.acumuladoM3) * 1000;
    }

    /** Cambia el texto de observación de la lectura (por ejemplo "Consumo de 250 L"). */
    public void actualizarObservacion(String texto) {
        this.observacion = texto;
    }

    // ---------- Getters y setters ----------
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    /** Devuelve solo la fecha (sin la hora). */
    public LocalDate getFecha() {
        return fechaHora.toLocalDate();
    }

    /** Devuelve solo la hora (sin la fecha). */
    public LocalTime getHora() {
        return fechaHora.toLocalTime();
    }

    public double getAcumuladoM3() {
        return acumuladoM3;
    }

    public void setAcumuladoM3(double acumuladoM3) {
        this.acumuladoM3 = acumuladoM3;
    }

    public String getCodigoMedidor() {
        return codigoMedidor;
    }

    public void setCodigoMedidor(String codigoMedidor) {
        this.codigoMedidor = codigoMedidor;
    }

    public String getOrigenDato() {
        return origenDato;
    }

    public void setOrigenDato(String origenDato) {
        this.origenDato = origenDato;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    /** Texto que se muestra en las listas desplegables. Ej: "#1  2026-11-01 07:00  (120.000 m3)". */
    @Override
    public String toString() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return "#" + getId() + "  " + fechaHora.format(formato) + "  (" + String.format("%.3f", acumuladoM3) + " m3)";
    }
}
