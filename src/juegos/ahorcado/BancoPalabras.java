package juegos.ahorcado;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** Palabras del juego. Se entregan en orden aleatorio sin repetirse hasta que se acaben. */
public final class BancoPalabras {

    // Las palabras van en el codigo para que el juego no dependa de archivos externos
    private static final String[] PALABRAS = {
        "COMPUTADORA", "TECLADO", "PANTALLA", "PROGRAMA", "VARIABLE", "OBJETO",
        "INTERFAZ", "PAQUETE", "HERENCIA", "METODO", "ARREGLO", "EXCEPCION",
        "ITERADOR", "COMPILADOR", "ALGORITMO", "SERVIDOR", "INTERNET", "MEMORIA",
        "PROCESADOR", "BIBLIOTECA", "VENTANA", "JAVA", "POLIMORFISMO", "CONSTRUCTOR",
        "ATRIBUTO", "VOLCAN", "MONTAÑA", "PIÑA", "CAFETAL", "GUANACASTE",
        "PUNTARENAS", "CARTAGO", "HEREDIA", "ALAJUELA", "LIMON", "TUCAN",
        "PEREZOSO", "MARIPOSA", "QUETZAL", "JAGUAR", "COLIBRI", "TORTUGA",
        "DELFIN", "CATARATA", "PLAYA", "BOSQUE", "RIO", "LAGUNA",
        "CARRETA", "GALLOPINTO", "TAMAL", "CASADO", "CHIFRIJO", "AGUACATE",
        "MANGO", "SANDIA", "GUANABANA", "MARACUYA", "FUTBOL", "GUITARRA",
        "MARIMBA", "BICICLETA", "CUADERNO", "LAPICERO", "PIZARRA", "ESCUELA",
        "UNIVERSIDAD", "TECNOLOGICO", "CIUDAD", "LLUVIA", "ARCOIRIS", "NARANJA",
        "CHOCOLATE"
    };

    private final List<String> palabras = new ArrayList<>(Arrays.asList(PALABRAS));
    private final Random aleatorio;
    private Iterator<String> recorrido;

    public BancoPalabras(Random aleatorio) {
        if (aleatorio == null) {
            throw new IllegalArgumentException("El generador aleatorio es obligatorio");
        }
        this.aleatorio = aleatorio;
        barajar();
    }

    private void barajar() {
        Collections.shuffle(palabras, aleatorio);
        recorrido = palabras.iterator();
    }

    public String siguiente() {
        if (!recorrido.hasNext()) {
            barajar();
        }
        return recorrido.next();
    }
}
