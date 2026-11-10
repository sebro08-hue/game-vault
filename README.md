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
