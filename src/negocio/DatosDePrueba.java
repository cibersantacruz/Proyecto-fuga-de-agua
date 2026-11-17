package negocio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import modelo.CatalogoArtefactos;
import modelo.Hogar;
import modelo.LecturaMedidor;
import modelo.TipoArtefacto;

/** Carga en memoria los datos de ejemplo cada vez que inicia la aplicación. */
public class DatosDePrueba {

    private DatosDePrueba() {
    }

    public static void cargar(GestorHogares gHogares, GestorPuntosConsumo gPuntos,
                              GestorLecturas gLecturas, GestorFugas gFugas, GestorMetas gMetas) {
        gHogares.registrar(1, "Carlos Mendoza", "Av. Amazonas N24-135, Quito", "MED-1001", 4);
        gHogares.registrar(2, "Ana Villacís", "Av. 6 de Diciembre N34-120, Quito", "MED-2002", 5);
        gHogares.registrar(3, "Luis Paredes", "Calle Venezuela N3-86, Quito", "MED-3003", 2);

        Hogar h1 = gHogares.buscarPorId(1);
        Hogar h2 = gHogares.buscarPorId(2);
        Hogar h3 = gHogares.buscarPorId(3);

        CatalogoArtefactos catalogo = new CatalogoArtefactos();
        TipoArtefacto ducha = catalogo.buscarPorNombre("Ducha");
        TipoArtefacto grifo = catalogo.buscarPorNombre("Grifo de cocina / lavabo");
        TipoArtefacto inodoro = catalogo.buscarPorNombre("Inodoro");
        TipoArtefacto lavadora = catalogo.buscarPorNombre("Lavadora");
        TipoArtefacto manguera = catalogo.buscarPorNombre("Manguera de jardín");
        TipoArtefacto otros = catalogo.buscarPorNombre(CatalogoArtefactos.OTROS);

        // Puntos de consumo de los tres hogares.
        gPuntos.registrar(h1, "Baño principal", ducha, "", 9.0, 5, 4);
        gPuntos.registrar(h1, "Cocina", grifo, "", 5.0, 6, 3);
        gPuntos.registrar(h1, "Baño principal", inodoro, "", 6.0, 0, 10);
        gPuntos.registrar(h1, "Lavandería", lavadora, "", 50.0, 0, 1);
        gPuntos.registrar(h2, "Baño principal", ducha, "", 9.0, 6, 5);
        gPuntos.registrar(h2, "Baño social", grifo, "", 5.0, 3, 10);
        gPuntos.registrar(h2, "Patio", manguera, "", 15.0, 15, 1);
        gPuntos.registrar(h2, "Patio", otros, "Bomba de agua", 20.0, 10, 1);
        gPuntos.registrar(h3, "Baño", ducha, "", 9.0, 5, 2);
        gPuntos.registrar(h3, "Cocina", grifo, "", 5.0, 10, 2);

        // Lecturas del medidor. El valor acumulado está expresado en metros cúbicos.
        // Los datos de octubre permiten comparar el consumo histórico antes de noviembre.
        // Con todo el historial, h1 promedia 315.00 L/día y h2 501.00 L/día.
        // En el periodo 1-4 de noviembre con factor 1.5, h1 promedia 383.33 L/día
        // sobre un histórico de 283.33 L/día (Normal); h2 promedia 1503.33 L/día
        // sobre un histórico de 33.33 L/día (Posible fuga).
        agregarLectura(gLecturas, h1, 2026, 10, 25, 7, 0, 118.000);
        agregarLectura(gLecturas, h1, 2026, 10, 31, 7, 0, 119.700);
        agregarLectura(gLecturas, h1, 2026, 11, 1, 7, 0, 120.000);
        agregarLectura(gLecturas, h1, 2026, 11, 1, 23, 0, 120.250);
        agregarLectura(gLecturas, h1, 2026, 11, 2, 5, 0, 120.253);
        agregarLectura(gLecturas, h1, 2026, 11, 2, 7, 0, 120.380);
        agregarLectura(gLecturas, h1, 2026, 11, 3, 7, 0, 120.760);
        agregarLectura(gLecturas, h1, 2026, 11, 4, 7, 0, 121.150);

        agregarLectura(gLecturas, h2, 2026, 10, 25, 7, 0, 78.000);
        agregarLectura(gLecturas, h2, 2026, 10, 31, 7, 0, 78.200);
        agregarLectura(gLecturas, h2, 2026, 11, 1, 7, 0, 78.500);
        agregarLectura(gLecturas, h2, 2026, 11, 2, 7, 0, 79.500);
        agregarLectura(gLecturas, h2, 2026, 11, 2, 23, 0, 79.750);
        agregarLectura(gLecturas, h2, 2026, 11, 3, 5, 0, 79.820);
        agregarLectura(gLecturas, h2, 2026, 11, 3, 7, 0, 80.000);
        agregarLectura(gLecturas, h2, 2026, 11, 3, 23, 0, 81.500);
        agregarLectura(gLecturas, h2, 2026, 11, 4, 7, 0, 83.010);

        agregarLectura(gLecturas, h3, 2026, 11, 1, 7, 0, 45.000);
        agregarLectura(gLecturas, h3, 2026, 11, 1, 23, 0, 45.100);
        agregarLectura(gLecturas, h3, 2026, 11, 2, 5, 0, 45.103);
        agregarLectura(gLecturas, h3, 2026, 11, 2, 7, 0, 45.185);
        agregarLectura(gLecturas, h3, 2026, 11, 3, 7, 0, 45.370);

        // Pruebas nocturnas: h1 y h3 normales, h2 con posible fuga.
        gFugas.ejecutarPrueba(h1, buscarLectura(h1, 2026, 11, 1, 23, 0),
                buscarLectura(h1, 2026, 11, 2, 5, 0), 5, true);
        gFugas.ejecutarPrueba(h2, buscarLectura(h2, 2026, 11, 2, 23, 0),
                buscarLectura(h2, 2026, 11, 3, 5, 0), 5, true);
        gFugas.ejecutarPrueba(h3, buscarLectura(h3, 2026, 11, 1, 23, 0),
                buscarLectura(h3, 2026, 11, 2, 5, 0), 5, true);

        // Metas de ahorro para noviembre.
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 30);
        gMetas.crear(h1, inicio, fin, 15);
        gMetas.crear(h1, inicio, fin, 30);
        gMetas.crear(h2, inicio, fin, 10);
        gMetas.crear(h3, inicio, fin, 5);
    }

    private static void agregarLectura(GestorLecturas gestor, Hogar hogar,
                                       int anio, int mes, int dia, int hora, int minuto, double valor) {
        gestor.registrar(hogar, LocalDate.of(anio, mes, dia), LocalTime.of(hora, minuto), valor);
    }

    /** Busca una lectura por fecha y hora para que las pruebas no dependan del orden de la lista. */
    private static LecturaMedidor buscarLectura(Hogar hogar, int anio, int mes, int dia, int hora, int minuto) {
        LocalDateTime fechaHora = LocalDateTime.of(anio, mes, dia, hora, minuto);
        for (LecturaMedidor lectura : hogar.obtenerLecturas()) {
            if (lectura.getFechaHora().equals(fechaHora)) {
                return lectura;
            }
        }
        throw new IllegalStateException("No se encontró la lectura de prueba del " + fechaHora + ".");
    }
}
