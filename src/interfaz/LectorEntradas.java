package interfaz;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Convierte el texto de los formularios a los tipos que usa la capa de negocio. */
public class LectorEntradas {

    private LectorEntradas() {
    }

    public static int leerEntero(String texto, String mensajeError) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(mensajeError);
        }
    }

    public static double leerDecimal(String texto, String mensajeError) {
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(mensajeError);
        }
    }

    public static LocalDate leerFecha(String texto, String mensajeError) {
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(mensajeError);
        }
    }

    public static LocalTime leerHora(String texto, String mensajeError) {
        try {
            return LocalTime.parse(texto.trim(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(mensajeError);
        }
    }
}
