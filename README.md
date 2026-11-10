# game-vault

Plataforma de juegos simples en Java (Swing, `JInternalFrame`) que carga juegos como módulos
mediante la interfaz `GameFunction` y mantiene un top 3 de records por juego.

Proyecto del curso Programación Orientada a Objetos – TEC, Sede San Carlos.

## Estructura
- `src/gestor/` – ventana principal, records y carga de juegos
- `src/gestor/api/` – `GameFunction` y `Stat`
- `src/juegos/<juego>/` – un paquete por juego
- `data/` – archivo de records
- `docs/` – enunciado y documentación externa

## Compilar y ejecutar
```bash
javac -d out $(find src -name "*.java")
java -cp out gestor.Main
```

## Cómo agregar un juego
1. Crear el paquete `src/juegos/<nombre>/` con una clase **Singleton** (`getInstance()`, constructor privado) que implemente `GameFunction` y extienda `JInternalFrame` (con `setDefaultCloseOperation(HIDE_ON_CLOSE)`).
2. `iniciar()` reinicia el estado para una partida nueva; `getStats()` devuelve el resultado de la última partida terminada (o `null`). Un valor más alto es mejor.
3. Si el `Stat` devuelto tiene `nombre == null`, la plataforma usa el nombre del jugador de la sesión.
4. Registrar el juego en `gestor/CargadorJuegos.java` (`new JuegoInfo("Nombre", MiJuego::getInstance)`).
5. Al cerrar la ventana del juego, la plataforma consulta `getStats()` y lo guarda si entra al top 3 (`data/records.txt`).

Juego externo (de otro grupo): copiar su código a su propio paquete y adaptarlo a estas reglas.
