package modelo;

/** Clase base para las entidades que tienen un identificador. */
public class EntidadBase {

    private int id;

    public EntidadBase(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
