package negocio;

/** Clase base que comparte el manejo de errores de los gestores. */
public class GestorBase {

    private String mensajeError = "";

    /** Devuelve el mensaje del último error de validación. */
    public String getMensajeError() {
        return mensajeError;
    }

    /** Guarda el mensaje del error y devuelve false para facilitar las validaciones. */
    protected boolean error(String mensaje) {
        mensajeError = mensaje;
        return false;
    }
}
