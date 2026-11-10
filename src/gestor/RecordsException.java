package gestor;

/** Error al leer o escribir el archivo de records. */
public class RecordsException extends Exception {

    public RecordsException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
