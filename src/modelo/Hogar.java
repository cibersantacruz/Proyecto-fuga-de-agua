package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Representa una vivienda con su medidor de agua.
 * Es la clase central del modelo: un hogar tiene muchas lecturas, pruebas de fuga,
 * metas de ahorro y puntos de consumo (relaciones de asociación del diagrama UML).
 */
public class Hogar extends EntidadBase {

    public static final double LITROS_POR_PERSONA_DIA = 100.0;

    // Datos básicos del hogar (atributos privados: encapsulamiento)
    private String nombreResponsable;
    private String direccion;
    private String codigoMedidor;
    private int habitantes;

    // Listas con los objetos relacionados al hogar (asociaciones del diagrama UML)
    private List<LecturaMedidor> lecturas;
    private List<PruebaFuga> pruebas;
    private List<MetaAhorro> metas;
    private List<PuntoConsumo> puntos;

    /** Crea un hogar con sus datos básicos y con las listas vacías. */
    public Hogar(int id, String nombreResponsable, String direccion, String codigoMedidor, int habitantes) {
        super(id);
        this.nombreResponsable = nombreResponsable;
        this.direccion = direccion;
        this.codigoMedidor = codigoMedidor;
        this.habitantes = habitantes;
        this.lecturas = new ArrayList<>();
        this.pruebas = new ArrayList<>();
        this.metas = new ArrayList<>();
        this.puntos = new ArrayList<>();
    }

    // ---------- Operaciones del hogar ----------

    /** Actualiza los datos editables del hogar sin cambiar sus metas ni otros registros. */
    public void actualizarDatos(String nombreResponsable, String direccion, String codigoMedidor, int habitantes) {
        this.nombreResponsable = nombreResponsable;
        this.direccion = direccion;
        this.codigoMedidor = codigoMedidor;
        this.habitantes = habitantes;
    }

    /** Consumo ideal diario del hogar según la referencia de 100 litros por persona. */
    public double calcularConsumoIdeal() {
        return calcularConsumoIdeal(LITROS_POR_PERSONA_DIA);
    }

    /** Consumo ideal diario del hogar con la referencia indicada en litros por persona. */
    public double calcularConsumoIdeal(double referenciaPorPersona) {
        return habitantes * referenciaPorPersona;
    }

    /** Agrega una lectura a la lista y la deja ordenada por fecha y hora. */
    public void registrarLectura(LecturaMedidor lectura) {
        lecturas.add(lectura);
        ordenarLecturas();
    }

    /** Elimina una lectura del hogar. */
    public boolean eliminarLectura(LecturaMedidor lectura) {
        return lecturas.remove(lectura);
    }

    /** Ordena las lecturas de la más antigua a la más reciente. */
    public void ordenarLecturas() {
        lecturas.sort(Comparator.comparing(LecturaMedidor::getFechaHora));
    }

    /** Guarda una prueba de fuga en el historial del hogar. */
    public void registrarPrueba(PruebaFuga prueba) {
        pruebas.add(prueba);
    }

    /** Guarda una meta de ahorro del hogar. */
    public void agregarMeta(MetaAhorro meta) {
        metas.add(meta);
    }

    /** Guarda un artefacto (punto de consumo) del hogar. */
    public void agregarPunto(PuntoConsumo punto) {
        puntos.add(punto);
    }

    /** Elimina un punto de consumo del hogar. */
    public boolean eliminarPunto(PuntoConsumo punto) {
        return puntos.remove(punto);
    }

    /** Devuelve las lecturas del medidor (ordenadas por fecha y hora). */
    public List<LecturaMedidor> obtenerLecturas() {
        return Collections.unmodifiableList(lecturas);
    }

    /** Devuelve las pruebas de fuga del hogar. */
    public List<PruebaFuga> obtenerPruebas() {
        return Collections.unmodifiableList(pruebas);
    }

    /** Devuelve las metas de ahorro del hogar. */
    public List<MetaAhorro> obtenerMetas() {
        return Collections.unmodifiableList(metas);
    }

    /** Devuelve los artefactos (puntos de consumo) del hogar. */
    public List<PuntoConsumo> obtenerPuntos() {
        return Collections.unmodifiableList(puntos);
    }

    // ---------- Getters y setters ----------
    public String getNombreResponsable() {
        return nombreResponsable;
    }

    public void setNombreResponsable(String nombreResponsable) {
        this.nombreResponsable = nombreResponsable;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCodigoMedidor() {
        return codigoMedidor;
    }

    public void setCodigoMedidor(String codigoMedidor) {
        this.codigoMedidor = codigoMedidor;
    }

    public int getHabitantes() {
        return habitantes;
    }

    public void setHabitantes(int habitantes) {
        this.habitantes = habitantes;
    }

    /** Texto que se muestra en las listas desplegables. Ej: "1 - Carlos Mendoza (MED-1001)". */
    @Override
    public String toString() {
        return getId() + " - " + nombreResponsable + " (" + codigoMedidor + ")";
    }
}
