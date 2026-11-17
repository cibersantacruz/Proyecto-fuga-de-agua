package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Catálogo de artefactos y sus caudales predeterminados. */
public class CatalogoArtefactos {

    public static final String OTROS = "Otros";

    private final List<TipoArtefacto> tipos;

    public CatalogoArtefactos() {
        tipos = new ArrayList<>();
        tipos.add(new TipoArtefacto("Ducha", 9.0, TipoArtefacto.UNIDAD_MINUTO));
        tipos.add(new TipoArtefacto("Grifo de cocina / lavabo", 5.0, TipoArtefacto.UNIDAD_MINUTO));
        tipos.add(new TipoArtefacto("Inodoro", 6.0, TipoArtefacto.UNIDAD_DESCARGA));
        tipos.add(new TipoArtefacto("Lavadora", 50.0, TipoArtefacto.UNIDAD_CICLO));
        tipos.add(new TipoArtefacto("Manguera de jardín", 15.0, TipoArtefacto.UNIDAD_MINUTO));
        tipos.add(new TipoArtefacto(OTROS, 0.0, TipoArtefacto.UNIDAD_MINUTO));
    }

    public List<TipoArtefacto> obtenerTipos() {
        return Collections.unmodifiableList(tipos);
    }

    public TipoArtefacto buscarPorNombre(String nombre) {
        for (TipoArtefacto tipo : tipos) {
            if (tipo.getNombre().equals(nombre)) {
                return tipo;
            }
        }
        return null;
    }
}
