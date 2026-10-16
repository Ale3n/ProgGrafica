# Flujo de Ejecución Detallado: El Viaje entre Clases Hijas y Padres

Este documento explica con precisión el **"viaje de ida y vuelta"** que realizan los métodos en el proyecto mediante los mecanismos de **Herencia, Enlace Dinámico (Dynamic Dispatch) y llamadas a `super`** en Java.

---

## 1. El Objeto en Memoria y la Jerarquía

Cuando el programa arranca, solo existe **una única instancia** en la memoria RAM:

```
[ Objeto en Memoria: new decoracion() ]
  └── Capa 4: decoracion.java   (Entregas, Tráfico, Minimapa, HUD)
        └── Capa 3: iluminacion.java  (Día/Noche, Farolas, Faros)
              └── Capa 2: auto.java         (Física WASD, Cámara 3ra persona)
                    └── Capa 1: ciudad.java       (Ventana GLFW, Shaders GPU, Bucle loop)
```

> **Principio Clave de Java (Enlace Dinámico):**  
> Aunque un método esté escrito dentro del archivo `ciudad.java`, cuando llama a un método no estático ni privado (como `dibujarFrame()` o `actualizar()`), la Máquina Virtual de Java (JVM) busca la versión de la clase **más específica** en el objeto en memoria (es decir, baja hasta `decoracion`).

---

## 2. El Viaje Inicial: de Maven al Bucle de Juego

```mermaid
flowchart TD
    CMD["mvn exec:java"] --> POM["pom.xml: mainClass = com.grafica.decoracion"]
    POM --> MAIN["decoracion.main()"]
    MAIN --> NEW["aplicacion = new decoracion()"]
    NEW --> RUN_CALL["aplicacion.run()"]

    subgraph "SUBIDA 1: Búsqueda de run()"
        RUN_CALL -.->|"decoracion no tiene run() - SUBE"| I_RUN["iluminacion (tampoco tiene)"]
        I_RUN -.->|"SUBE"| A_RUN["auto (tampoco tiene)"]
        A_RUN -.->|"SUBE"| C_RUN["ciudad.run() ENCONTRADO"]
    end

    subgraph "EJECUCIÓN EN ciudad.java"
        C_RUN --> C_INIT["iniciar() (private en ciudad)"]
        C_INIT --> C_LOOP["loop() (private en ciudad)"]
    end
```

### Paso a paso del arranque:
1. `mvn exec:java` ejecuta `decoracion.main(args)`.
2. `decoracion.main` crea la instancia y llama a `aplicacion.run()`.
3. Como `decoracion`, `iluminacion` y `auto` **no definen `run()`**, Java sube automáticamente por la jerarquía hasta encontrar **`ciudad.run()`**.
4. `ciudad.run()` toma el control y ejecuta sus métodos internos:
   * **`iniciar()`**: Crea la ventana GLFW, inicializa OpenGL 3.3, compila shaders y crea el cubo en la GPU.
   * **`loop()`**: Comienza el bucle continuo a ~60 cuadros por segundo.

---

## 3. El Viaje de `dibujarFrame()`: Bajada y Subida en Cada Fotograma

Dentro de `ciudad.loop()` (línea 105), el código fuente dice textualmente:

```java
// Archivo: ciudad.java (línea 105)
dibujarFrame();
```

Aquí comienza el viaje más importante del proyecto:

```mermaid
sequenceDiagram
    autonumber
    participant C_LOOP as ciudad.loop()
    participant D as decoracion.java
    participant I as iluminacion.java
    participant A as auto.java
    participant C as ciudad.java

    Note over C_LOOP: 1. ciudad.loop() invoca dibujarFrame()
    C_LOOP->>D: [BAJADA POR ENLACE DINÁMICO]<br/>La JVM busca la clase real: salta a decoracion.dibujarFrame()
    
    Note over D: decoracion ejecuta: super.dibujarFrame()
    D->>I: [SUBIDA CON super]<br/>iluminacion no lo tiene, sube...
    I->>A: auto no lo tiene, sube...
    A->>C: Llega a ciudad.dibujarFrame()
    
    Note over C: ciudad configura OpenGL (glViewport, glClear, shaders)<br/>Ahora necesita cámara, luces y escena:
    
    C->>A: [BAJADA] configurarCamara() -> Salta a auto.configurarCamara()<br/>(Cámara de 3ra persona siguiendo al vehículo)
    C->>I: [BAJADA] prepararLuces() -> Salta a iluminacion.prepararLuces()<br/>(Uniforms de día/noche, farolas y faros)
    
    Note over C: ciudad invoca escena()
    C->>D: [BAJADA] Salta a decoracion.escena()
    
    rect rgb(235, 245, 255)
    Note over D,C: CASCADA ASCENDENTE DE escena()
    D->>I: super.escena() (SUBE a iluminacion)
    I->>A: super.escena() (SUBE a auto)
    A->>C: super.escena() (SUBE a ciudad)
    Note over C: Dibuja Asfalto base, 19 Edificios, 6 Parques, Aceras y Líneas
    C-->>A: Retorna a auto
    Note over A: Dibuja Auto del Jugador (rojo/azul en autoX, autoZ)
    A-->>I: Retorna a iluminacion
    Note over I: Dibuja Farolas (postes y bombillas emisivas)
    I-->>D: Retorna a decoracion
    Note over D: Dibuja Tráfico Autónomo (carril derecho), Árboles, Bancos, Semáforos y Baliza de entrega
    end
    
    C-->>D: Termina super.dibujarFrame() y retorna a decoracion
    
    Note over D: PASE 2 (Minimapa):<br/>Activa glScissor, uMapa=1 y redibuja escena() desde arriba
    Note over D: PASE 3 (HUD 2D):<br/>Dibuja velocímetro y menús en pantalla
    
    D-->>C_LOOP: Retorna a ciudad.loop() para hacer glfwSwapBuffers()
```

### Explicación detallada de este ciclo:

1. **La Bajada (`ciudad` $\rightarrow$ `decoracion`)**:  
   `ciudad.loop()` llama a `dibujarFrame()`. Aunque `ciudad` tiene un método `dibujarFrame()`, la variable oculta `this` es de tipo `decoracion`. La JVM ignora el método de `ciudad` y **desciende directamente hasta `decoracion.dibujarFrame()`**.

2. **La Subida con `super` (`decoracion` $\rightarrow$ `ciudad`)**:  
   En la línea 308 de `decoracion.java`, la primera instrucción es:
   ```java
   super.dibujarFrame();
   ```
   Java sube buscando un padre que tenga `dibujarFrame()`. Ni `iluminacion` ni `auto` lo sobrescriben, así que la llamada **sube hasta `ciudad.dibujarFrame()`**.

3. **La Distribución dentro de `ciudad.dibujarFrame()`**:  
   Una vez arriba en `ciudad.java`, este método prepara el lienzo OpenGL (`glViewport`, `glClear`, activa shaders) y llama a tres métodos clave:
   * **`configurarCamara()`**: En lugar de usar la cámara orbital de `ciudad`, la JVM desciende a **`auto.configurarCamara()`** para ubicar la vista en 3ra persona detrás del carro.
   * **`prepararLuces()`**: En lugar del método vacío de `ciudad`, la JVM desciende a **`iluminacion.prepararLuces()`** para enviar las luces nocturnas y faros.
   * **`escena()`**: En lugar de dibujar solo la ciudad, la JVM desciende a **`decoracion.escena()`**.

4. **La Gran Cascada de `escena()`**:  
   `decoracion.escena()` no dibuja todo desde cero; primero llama a `super.escena()`, provocando una cadena hacia arriba:
   $$\text{decoracion} \xrightarrow{super} \text{iluminacion} \xrightarrow{super} \text{auto} \xrightarrow{super} \text{ciudad}$$
   * Al llegar arriba, **`ciudad.escena()`** dibuja la plataforma de asfalto (110x110), aceras, los 19 edificios, parques y líneas amarillas.
   * Al regresar a **`auto.escena()`**, añade el modelo 3D del auto del jugador.
   * Al regresar a **`iluminacion.escena()`**, añade los postes y bombillas de las farolas.
   * Al regresar a **`decoracion.escena()`**, añade el tráfico en el carril derecho, árboles, bancos, semáforos, ventanas y la baliza dorada de la entrega.

5. **El Retorno y Pases Exclusivos de `decoracion`**:  
   Cuando `super.dibujarFrame()` termina por completo, el control vuelve a **`decoracion.dibujarFrame()`**, donde se procesan los elementos que solo `decoracion` conoce:
   * **Minimapa Aéreo**: Recorta con `glScissor` la esquina superior derecha, activa la proyección ortográfica cenital (`uMapa = 1`) y vuelve a llamar a `escena()` para dibujarla en miniatura.
   * **HUD 2D**: Dibuja el velocímetro gráfico y el menú de pausa flotante.
6. **Vuelta a `ciudad.loop()`**:  
   La función termina y regresa a la línea 107 de `ciudad.java`, donde `glfwSwapBuffers(ventana)` presenta la imagen generada en la pantalla.

---

## 4. El Viaje de `actualizar(deltaTime)` en Cada Fotograma

En la línea 100 de `ciudad.loop()`, se ejecuta:

```java
// Archivo: ciudad.java (línea 100)
actualizar(deltaTime);
```

Este método sigue un recorrido idéntico de bajada y subida:

```mermaid
sequenceDiagram
    autonumber
    participant C_LOOP as ciudad.loop()
    participant D as decoracion.java
    participant A as auto.java

    C_LOOP->>D: [BAJADA POR ENLACE DINÁMICO]<br/>ciudad llama a actualizar() -> salta a decoracion.actualizar()
    
    Note over D: decoracion ejecuta: super.actualizar(deltaTime)
    D->>A: [SUBIDA CON super]<br/>iluminacion no lo tiene, sube a auto.actualizar()
    
    Note over A: auto.actualizar() ejecuta:<br/>- Lectura WASD y freno (Espacio)<br/>- Fricción y ángulo de giro<br/>- puedeCircular() (Colisión con edificios)<br/>- actualizarTitulo()
    
    A-->>D: Retorna a decoracion.actualizar()
    
    Note over D: decoracion ejecuta su lógica exclusiva:<br/>- v.actualizar() para cada auto del tráfico autónomo<br/>- Chequeo de distancia a parada activa DESTINOS<br/>- Suma tiempo al cronómetro
    
    D-->>C_LOOP: Retorna a ciudad.loop()
```

### Puntos clave de `actualizar()`:
1. `ciudad.loop()` llama a `actualizar(deltaTime)` $\rightarrow$ La JVM **baja a `decoracion.actualizar()`**.
2. `decoracion.actualizar()` ejecuta `super.actualizar(deltaTime)` $\rightarrow$ **Sube a `auto.actualizar()`**.
3. `auto.actualizar()` no llama a `super` porque reemplaza la cámara de `ciudad` por el motor de física del vehículo (W/A/S/D, colisiones y velocímetro).
4. Al terminar `auto`, el control regresa a `decoracion`, que mueve los vehículos de tráfico autónomo y comprueba si el auto frenó en la marca de entrega.
5. El método finaliza y el control vuelve a `ciudad.loop()`.

---

## 5. El Viaje de los Eventos de Teclado: `tecla(key)`

El registro del callback de teclado está en `ciudad.iniciar()` (línea 78):

```java
// Archivo: ciudad.java
glfwSetKeyCallback(ventana, (..., key, ...) -> {
    if (action == GLFW_PRESS) {
        tecla(key); // <- Invocación polimórfica
    }
});
```

Cuando presionas cualquier tecla:

```mermaid
flowchart TD
    KEY["Usuario presiona una tecla"] --> CALLBACK["Callback GLFW en ciudad.java"]
    CALLBACK --> CALL_TECLA["tecla(key)"]

    subgraph "BAJADA DINÁMICA"
        CALL_TECLA --> D_TECLA["decoracion.tecla(key)"]
    end

    subgraph "SUBIDA EN CADENA CON super"
        D_TECLA -->|"super.tecla(key)"| I_TECLA["iluminacion.tecla(key)"]
        I_TECLA -->|"super.tecla(key)"| A_TECLA["auto.tecla(key)"]
        A_TECLA -->|"super.tecla(key)"| C_TECLA["ciudad.tecla(key)"]
    end

    subgraph "PROCESAMIENTO POR CAPAS"
        C_TECLA -->|"Si es ESC"| CLOSE["Cierra la ventana"]
        A_TECLA -->|"Si es C, P o R"| CAR_KEYS["C: Cámara aérea | P: Pausa | R: Reiniciar"]
        I_TECLA -->|"Si es N o F"| LIGHT_KEYS["N: Noche/Día | F: Faros ON/OFF"]
        D_TECLA -->|"Si es M"| MAP_KEY["M: Alternar minimapa"]
    end
```

Cada nivel procesa exclusivamente las teclas que le corresponden y delega el resto a sus ancestros mediante `super.tecla(key)`.

---

## 6. Mapa Completo del Viaje entre Clases

| Método invocado | Invocado desde | ¿Dónde aterriza primero? (Bajada) | ¿A dónde sube con `super`? (Subida) | Responsabilidad repartida |
| :--- | :--- | :--- | :--- | :--- |
| **`run()`** | `decoracion.main` | `ciudad.run()` *(sube directo)* | N/A (Es la cúspide) | `ciudad` envuelve el ciclo de vida en `try/finally`. |
| **`iniciar()`** | `ciudad.run` | `ciudad.iniciar()` | N/A (Es `private`) | `ciudad` crea la ventana GLFW y compila shaders. |
| **`loop()`** | `ciudad.run` | `ciudad.loop()` | N/A (Es `private`) | `ciudad` controla el ciclo `while (!glfwWindowShouldClose)`. |
| **`actualizar()`** | `ciudad.loop` | **`decoracion.actualizar()`** | Sube a **`auto.actualizar()`** | `auto` calcula física/colisiones; `decoracion` mueve el tráfico y entregas. |
| **`dibujarFrame()`**| `ciudad.loop`| **`decoracion.dibujarFrame()`** | Sube a **`ciudad.dibujarFrame()`** | `ciudad` prepara OpenGL; `decoracion` añade minimapa con `glScissor` y HUD 2D. |
| **`configurarCamara()`**| `ciudad.dibujarFrame`| **`auto.configurarCamara()`** | Sube a `ciudad` solo si `camaraAerea == true` | Si no es aérea, `auto` ubica la cámara detrás del vehículo en 3ra persona. |
| **`prepararLuces()`**| `ciudad.dibujarFrame`| **`iluminacion.prepararLuces()`**| N/A | `iluminacion` carga farolas y faros al shader. |
| **`escena()`** | `ciudad.dibujarFrame`| **`decoracion.escena()`** | Sube: `decoracion` $\rightarrow$ `iluminacion` $\rightarrow$ `auto` $\rightarrow$ `ciudad` | `ciudad`: mundo base; `auto`: carro; `iluminacion`: farolas; `decoracion`: tráfico y balizas. |
| **`tecla()`** | `ciudad.iniciar` (callback) | **`decoracion.tecla()`** | Sube: `decoracion` $\rightarrow$ `iluminacion` $\rightarrow$ `auto` $\rightarrow$ `ciudad` | Cada clase procesa sus propias teclas (`ESC`, `C/P/R`, `N/F`, `M`). |
| **`reiniciar()`** | `auto.tecla` (al pulsar 'R') | **`decoracion.reiniciar()`** | Sube a **`auto.reiniciar()`** | `auto` devuelve el carro al origen; `decoracion` resetea cronómetro, entregas y tráfico. |
