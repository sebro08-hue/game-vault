package gestor;

import gestor.api.Stat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mantiene el top 3 de resultados por juego y lo persiste en un archivo de texto.
 * Formato de cada linea: {@code juego;clave;nombre;valor}. Un valor mas alto es mejor.
 */
public class GestorRecords {

    public static final int TOP = 3;

    private final Path archivo;
    private final Map<String, List<Stat>> records = new HashMap<>();
    private int lineasInvalidas;

    public GestorRecords(Path archivo) {
        this.archivo = archivo;
    }

    /**
     * Carga los records del archivo. Si no existe, empieza vacio. Las lineas con formato
     * invalido se omiten (ver {@link #getLineasInvalidas()}).
     *
     * @throws RecordsException si el archivo existe pero no se puede leer
     */
    public void cargar() throws RecordsException {
        records.clear();
        lineasInvalidas = 0;
        if (!Files.exists(archivo)) {
            return;
        }
        List<String> lineas;
        try {
            lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RecordsException("No se pudo leer el archivo de records: " + archivo, e);
        }
        for (String linea : lineas) {
            if (linea.isBlank()) {
                continue;
            }
            String[] partes = linea.split(";", -1);
            try {
                if (partes.length != 4) {
                    throw new NumberFormatException("columnas: " + partes.length);
                }
                agregar(partes[0], new Stat(partes[1], partes[2], Integer.parseInt(partes[3].trim())));
            } catch (NumberFormatException e) {
                lineasInvalidas++;
            }
        }
    }

    /**
     * Registra un resultado si entra en el top 3 del juego.
     *
     * @return {@code true} si el resultado entro al top 3 y se guardo
     * @throws RecordsException si no se pudo escribir el archivo
     */
    public boolean registrar(String juego, Stat stat) throws RecordsException {
        List<Stat> top = records.getOrDefault(juego, List.of());
        boolean entra = top.size() < TOP || stat.getValor() > top.get(top.size() - 1).getValor();
        if (!entra) {
            return false;
        }
        agregar(juego, new Stat(stat.getClave(), limpiar(stat.getNombre()), stat.getValor()));
        guardar();
        return true;
    }

    /** @return copia del top 3 del juego, de mejor a peor (vacio si no hay records). */
    public List<Stat> getTop(String juego) {
        return new ArrayList<>(records.getOrDefault(juego, List.of()));
    }

    /** @return cantidad de lineas omitidas por formato invalido en la ultima carga. */
    public int getLineasInvalidas() {
        return lineasInvalidas;
    }

    private void agregar(String juego, Stat stat) {
        List<Stat> top = records.computeIfAbsent(juego, j -> new ArrayList<>());
        top.add(stat);
        // sort estable: ante empate queda primero el record mas antiguo
        top.sort(Comparator.comparingInt(Stat::getValor).reversed());
        while (top.size() > TOP) {
            top.remove(top.size() - 1);
        }
    }

    private void guardar() throws RecordsException {
        List<String> lineas = new ArrayList<>();
        records.forEach((juego, top) -> {
            for (Stat s : top) {
                lineas.add(String.join(";", limpiar(juego), limpiar(s.getClave()),
                        limpiar(s.getNombre()), String.valueOf(s.getValor())));
            }
        });
        try {
            Path padre = archivo.toAbsolutePath().getParent();
            if (padre != null) {
                Files.createDirectories(padre);
            }
            Files.write(archivo, lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RecordsException("No se pudo guardar el archivo de records: " + archivo, e);
        }
    }

    /** Evita que el separador o saltos de linea rompan el formato del archivo. */
    private static String limpiar(String texto) {
        return texto == null ? "" : texto.replace(';', ',').replaceAll("[\\r\\n]+", " ").trim();
    }
}
