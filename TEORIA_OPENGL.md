# Teoria general de Maven, Java, GLFW, LWJGL y OpenGL

## 1. Java

Java es un lenguaje de programacion orientado a objetos. Permite crear aplicaciones mediante clases, metodos, atributos y objetos.

En una aplicacion grafica, Java se encarga normalmente de:

- Ejecutar la logica del programa.
- Crear y modificar variables.
- Leer entradas del usuario.
- Actualizar posiciones, velocidades y estados.
- Llamar a las funciones de las bibliotecas graficas.
- Organizar el ciclo de vida de la aplicacion.

### Metodo `main`

El metodo `main` es el punto de entrada de una aplicacion Java:

```java
public static void main(String[] args) {
    new Aplicacion().run();
}
```

El programa comienza a ejecutarse desde este metodo.

### Clase

Una clase es una plantilla que agrupa datos y comportamientos:

```java
public class Aplicacion {
    private int ancho;

    public void ejecutar() {
        // Logica de la aplicacion
    }
}
```

### Metodo `private void`

Una declaracion como:

```java
private void loop() {
    // Instrucciones
}
```

contiene tres partes:

- `private`: el metodo solo puede utilizarse desde la misma clase.
- `void`: el metodo no devuelve ningun valor.
- `loop`: nombre del metodo.
- `()`: lista de parametros; en este caso no recibe parametros.

Los metodos privados se utilizan para separar responsabilidades internas. Por ejemplo, una clase puede tener metodos privados llamados `init`, `loop`, `crearShaders` y `cleanup`.

### Metodo `public void`

```java
public void init() {
    // Inicializacion
}
```

- `public`: puede llamarse desde otras clases.
- `void`: no devuelve un resultado.
- `init`: nombre del metodo.

### Metodo `private int`

```java
private int crearRecurso() {
    return 1;
}
```

`int` indica que el metodo devuelve un numero entero.

---

## 2. Maven

Maven es una herramienta para construir y administrar proyectos Java. Automatiza tareas que de otro modo tendrian que hacerse manualmente.

Maven permite:

- Descargar dependencias.
- Compilar el codigo fuente.
- Ejecutar pruebas.
- Empaquetar la aplicacion.
- Ejecutar plugins.
- Mantener versiones de bibliotecas.
- Estandarizar la estructura del proyecto.

### Archivo `pom.xml`

La configuracion principal de Maven se encuentra en `pom.xml`.

Sus elementos mas importantes son:

- `groupId`: identificador del grupo o de la organizacion.
- `artifactId`: nombre del proyecto.
- `version`: version del proyecto.
- `packaging`: formato de salida, por ejemplo `jar`.
- `properties`: propiedades reutilizables.
- `dependencies`: bibliotecas externas.
- `build`: configuracion de compilacion.
- `plugins`: herramientas que amplian las funciones de Maven.

Ejemplo:

```xml
<groupId>org.ejemplo</groupId>
<artifactId>mi-aplicacion</artifactId>
<version>1.0</version>
<packaging>jar</packaging>
```

### Dependencias

Una dependencia es una biblioteca que el proyecto necesita para compilar o ejecutarse.

```xml
<dependency>
    <groupId>org.lwjgl</groupId>
    <artifactId>lwjgl</artifactId>
    <version>3.3.6</version>
</dependency>
```

Maven busca la dependencia en repositorios, la descarga y la agrega al proyecto.

### Ciclo de vida de Maven

Maven tiene fases comunes:

```text
validate -> compile -> test -> package -> verify -> install
```

- `validate`: verifica que el proyecto este correctamente configurado.
- `compile`: compila el codigo principal.
- `test`: compila y ejecuta las pruebas.
- `package`: crea un JAR u otro paquete.
- `verify`: realiza comprobaciones adicionales.
- `install`: instala el paquete en el repositorio local.

### Comandos principales

```bash
mvn clean
mvn compile
mvn test
mvn package
mvn install
```

- `mvn clean`: elimina archivos generados anteriormente.
- `mvn compile`: compila el codigo.
- `mvn test`: ejecuta las pruebas.
- `mvn package`: crea el paquete de la aplicacion.
- `mvn install`: guarda el paquete en el repositorio local de Maven.

### Estructura habitual

```text
proyecto/
|-- pom.xml
|-- src/
|   |-- main/
|   |   |-- java/
|   |   |-- resources/
|   |-- test/
|       |-- java/
|-- target/
```

- `src/main/java`: codigo principal.
- `src/main/resources`: archivos de configuracion y recursos.
- `src/test/java`: pruebas.
- `target`: archivos generados por Maven.

---

## 3. API, biblioteca y framework

### API

Una API es un conjunto de funciones y reglas que permite utilizar un sistema desde otro programa.

### Biblioteca

Una biblioteca es codigo reutilizable que una aplicacion puede llamar para realizar tareas concretas.

### Framework

Un framework proporciona una estructura general para construir una aplicacion. Normalmente define parte del flujo de ejecucion y los puntos donde el programador agrega su logica.

OpenGL es una API grafica. GLFW y LWJGL son bibliotecas que ayudan a utilizar OpenGL desde Java.

---

## 4. OpenGL

OpenGL es una API multiplataforma para renderizar imagenes y escenas 2D o 3D mediante la GPU.

OpenGL proporciona funciones para:

- Crear primitivas geometricas.
- Enviar datos a la GPU.
- Ejecutar shaders.
- Aplicar transformaciones.
- Trabajar con texturas.
- Calcular profundidad.
- Configurar iluminacion mediante shaders.
- Dibujar en un framebuffer.

OpenGL es una API de bajo nivel. El programador debe configurar explicitamente muchos recursos y estados.

### Estado de OpenGL

OpenGL mantiene un estado interno. Algunas llamadas cambian ese estado:

```java
glUseProgram(programa);
glBindVertexArray(vao);
glEnable(GL_DEPTH_TEST);
```

Las operaciones posteriores utilizan el programa, VAO o configuracion que este activa. Por eso es importante controlar que recurso esta enlazado en cada momento.

---

## 5. LWJGL

LWJGL significa *Lightweight Java Game Library*. Permite acceder desde Java a bibliotecas nativas relacionadas con graficos, ventanas, audio e imagenes.

LWJGL no es un motor grafico completo. No decide como debe organizarse la escena ni proporciona automaticamente un sistema de entidades, fisica o interfaz grafica.

LWJGL funciona como un puente entre Java y APIs nativas como:

- OpenGL para renderizado.
- GLFW para ventanas y entrada.
- OpenAL para audio.
- STB para imagenes, fuentes y otros recursos.

Las funciones de LWJGL suelen conservar nombres similares a las funciones originales de OpenGL:

```java
glClear(GL_COLOR_BUFFER_BIT);
glUseProgram(programa);
glDrawArrays(GL_TRIANGLES, 0, 3);
```

---

## 6. GLFW

GLFW es una biblioteca para crear ventanas y administrar la interaccion basica con el sistema operativo.

GLFW se encarga de:

- Crear y destruir ventanas.
- Crear el contexto de OpenGL.
- Leer teclado y mouse.
- Recibir eventos.
- Consultar el tiempo.
- Intercambiar buffers.

### Inicializar GLFW

```java
if (!glfwInit()) {
    throw new IllegalStateException("No se pudo inicializar GLFW");
}
```

### Configurar la ventana

```java
glfwDefaultWindowHints();
glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
```

`glfwWindowHint` establece opciones antes de crear la ventana.

### Crear la ventana

```java
long ventana = glfwCreateWindow(
    800,
    600,
    "Ventana OpenGL",
    0,
    0
);
```

Los parametros son el ancho, alto, titulo, monitor y ventana para compartir recursos.

### Crear el contexto

```java
glfwMakeContextCurrent(ventana);
GL.createCapabilities();
```

El contexto contiene el estado y los recursos de OpenGL asociados a una ventana. `GL.createCapabilities()` carga las funciones disponibles para poder llamarlas desde Java.

### Mostrar la ventana

```java
glfwShowWindow(ventana);
```

### Procesar eventos

```java
glfwPollEvents();
```

Procesa eventos pendientes de teclado, mouse, redimensionamiento y cierre.

### Intercambiar buffers

```java
glfwSwapBuffers(ventana);
```

Muestra en pantalla el frame que se dibujo. Normalmente se utiliza doble buffer para evitar que el usuario vea una imagen incompleta mientras se esta dibujando.

### Terminar GLFW

```java
glfwDestroyWindow(ventana);
glfwTerminate();
```

Libera la ventana y finaliza GLFW.

---

## 7. Ciclo de vida de una aplicacion grafica

Una aplicacion grafica suele dividirse en tres etapas:

```text
inicializacion -> bucle principal -> limpieza
```

### `init()`

`init` viene de *initialize*, que significa inicializar. Su funcion es preparar todo lo necesario antes de dibujar.

Normalmente realiza estas tareas:

1. Inicializar GLFW.
2. Configurar opciones de la ventana.
3. Crear la ventana.
4. Crear el contexto OpenGL.
5. Cargar las capacidades de OpenGL.
6. Registrar callbacks.
7. Crear y compilar shaders.
8. Crear VAO y VBO.
9. Configurar opciones iniciales de OpenGL.

Ejemplo conceptual:

```java
private void init() {
    inicializarGlfw();
    crearVentana();
    crearContextoOpenGL();
    crearShaders();
    crearBuffers();
}
```

### `loop()`

`loop` significa bucle. Es la parte que se repite mientras la ventana permanezca abierta.

Un bucle principal suele hacer lo siguiente:

1. Procesar eventos.
2. Calcular el tiempo transcurrido.
3. Leer la entrada del usuario.
4. Actualizar la logica.
5. Limpiar la pantalla.
6. Activar el programa de shaders.
7. Activar el VAO.
8. Actualizar uniforms.
9. Dibujar.
10. Intercambiar buffers.

Ejemplo general:

```java
private void loop() {
    while (!glfwWindowShouldClose(ventana)) {
        glfwPollEvents();
        actualizar();
        dibujar();
        glfwSwapBuffers(ventana);
    }
}
```

### `update()` o `actualizar()`

Actualiza el estado de la aplicacion. No necesariamente dibuja.

Puede modificar:

- Posiciones.
- Rotaciones.
- Escalas.
- Velocidades.
- Animaciones.
- Estados de entrada.

### `render()` o `dibujar()`

Realiza las operaciones de renderizado:

- Limpia los buffers.
- Selecciona shaders.
- Enlaza buffers.
- Envia uniforms.
- Ejecuta las llamadas de dibujo.

### `cleanup()` o `limpiar()`

Libera los recursos creados durante la inicializacion.

```java
private void cleanup() {
    glDeleteBuffers(vbo);
    glDeleteVertexArrays(vao);
    glDeleteProgram(programa);
    glfwDestroyWindow(ventana);
    glfwTerminate();
}
```

Separar estas etapas hace que el codigo sea mas claro y facilita detectar errores.

---

## 8. Tiempo y `deltaTime`

Las aplicaciones graficas se ejecutan muchas veces por segundo. El tiempo entre dos frames se llama `deltaTime` o `delta time`.

```java
double tiempoAnterior = glfwGetTime();

double tiempoActual = glfwGetTime();
float deltaTime = (float) (tiempoActual - tiempoAnterior);
tiempoAnterior = tiempoActual;
```

El movimiento debe multiplicarse por `deltaTime`:

```java
posicion += velocidad * deltaTime;
```

De esta forma, el objeto se mueve a una velocidad similar aunque el numero de frames por segundo cambie.

---

## 9. Shaders y GLSL

Un shader es un programa pequeno que se ejecuta en la GPU. Los shaders se escriben en GLSL, que significa *OpenGL Shading Language*.

Los shaders permiten programar partes del proceso de renderizado.

### Vertex shader

El vertex shader se ejecuta una vez por cada vertice. Su salida obligatoria es `gl_Position`.

```glsl
#version 330 core
layout (location = 0) in vec3 aPos;

void main() {
    gl_Position = vec4(aPos, 1.0);
}
```

Sirve para:

- Transformar posiciones.
- Aplicar desplazamiento.
- Aplicar escala.
- Aplicar rotacion.
- Aplicar camara.
- Aplicar perspectiva.
- Enviar datos al fragment shader.

### Fragment shader

El fragment shader calcula el color de cada fragmento del objeto.

```glsl
#version 330 core
out vec4 color;

void main() {
    color = vec4(1.0, 0.0, 0.0, 1.0);
}
```

Un `vec4` suele representar rojo, verde, azul y alpha.

### Compilar un shader

El proceso general es:

1. Crear el shader.
2. Enviar el codigo fuente.
3. Compilarlo.
4. Revisar el resultado.
5. Adjuntarlo a un programa.
6. Enlazar el programa.

```java
int shader = glCreateShader(GL_VERTEX_SHADER);
glShaderSource(shader, codigo);
glCompileShader(shader);

if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
    throw new RuntimeException(glGetShaderInfoLog(shader));
}
```

### Programa de shaders

Un programa de OpenGL combina normalmente un vertex shader y un fragment shader:

```java
int programa = glCreateProgram();
glAttachShader(programa, vertexShader);
glAttachShader(programa, fragmentShader);
glLinkProgram(programa);
```

Para usarlo al dibujar:

```java
glUseProgram(programa);
```

---

## 10. VBO: Vertex Buffer Object

Un VBO (*Vertex Buffer Object*) es un objeto de OpenGL que almacena datos de vertices, normalmente en memoria accesible por la GPU.

Un vertice puede contener:

- Posicion.
- Normal.
- Color.
- Coordenadas de textura.
- Datos personalizados.

Crear un VBO:

```java
int vbo = glGenBuffers();
glBindBuffer(GL_ARRAY_BUFFER, vbo);
glBufferData(GL_ARRAY_BUFFER, datos, GL_STATIC_DRAW);
```

Funciones principales:

- `glGenBuffers`: genera un identificador.
- `glBindBuffer`: selecciona el buffer.
- `glBufferData`: copia datos al buffer.
- `glDeleteBuffers`: elimina el buffer.

Usos de los modos de almacenamiento:

- `GL_STATIC_DRAW`: los datos cambian poco.
- `GL_DYNAMIC_DRAW`: los datos cambian varias veces.
- `GL_STREAM_DRAW`: los datos cambian continuamente.

El VBO contiene los datos, pero no explica por si solo como deben interpretarse. Esa configuracion se guarda normalmente en un VAO.

---

## 11. VAO: Vertex Array Object

Un VAO (*Vertex Array Object*) almacena la configuracion de los atributos de vertices.

Un VAO puede recordar:

- Que VBO esta asociado.
- Que atributos estan activos.
- Cuantos componentes tiene cada atributo.
- El tipo de dato.
- El salto entre vertices.
- La posicion de cada atributo dentro del VBO.

Configuracion basica:

```java
int vao = glGenVertexArrays();
glBindVertexArray(vao);

glBindBuffer(GL_ARRAY_BUFFER, vbo);
glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
glEnableVertexAttribArray(0);

glBindVertexArray(0);
```

La llamada siguiente describe el formato de los datos:

```java
glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
```

Sus parametros significan:

1. `0`: ubicacion del atributo en el vertex shader.
2. `3`: cantidad de componentes.
3. `GL_FLOAT`: tipo de dato.
4. `false`: no normalizar.
5. `3 * Float.BYTES`: distancia entre el comienzo de un vertice y el siguiente.
6. `0`: desplazamiento inicial dentro del vertice.

La diferencia basica es:

```text
VBO = almacena los datos
VAO = almacena como interpretar los datos
```

---

## 12. Atributos y uniforms

### Atributos

Los atributos son datos que normalmente pueden cambiar de un vertice a otro. Se declaran con `in` en el vertex shader:

```glsl
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aColor;
```

Los atributos se configuran con `glVertexAttribPointer`.

### Uniforms

Los uniforms son valores enviados desde el programa Java al shader. Se mantienen constantes durante una llamada de dibujo, hasta que se vuelven a actualizar.

```glsl
uniform float uZoom;
uniform vec2 uOffset;
uniform vec3 uColor;
```

En Java:

```java
int ubicacion = glGetUniformLocation(programa, "uZoom");
glUniform1f(ubicacion, zoom);
```

Funciones comunes:

```java
glUniform1f(ubicacion, valor);       // float
glUniform1i(ubicacion, valor);       // int
glUniform2f(ubicacion, x, y);        // vec2
glUniform3f(ubicacion, x, y, z);      // vec3
```

`glGetUniformLocation` devuelve la ubicacion de un uniform dentro del programa enlazado.

---

## 13. Dibujar con OpenGL

La funcion `glDrawArrays` ordena dibujar vertices usando los atributos del VAO activo:

```java
glUseProgram(programa);
glBindVertexArray(vao);
glDrawArrays(GL_TRIANGLES, 0, 3);
```

Los parametros indican:

- `GL_TRIANGLES`: interpreta cada grupo de tres vertices como un triangulo.
- `0`: indice del primer vertice.
- `3`: cantidad de vertices.

Modos de dibujo frecuentes:

- `GL_TRIANGLES`: triangulos independientes.
- `GL_TRIANGLE_STRIP`: triangulos conectados.
- `GL_LINES`: lineas.
- `GL_POINTS`: puntos.

---

## 14. Buffers, color y profundidad

### Limpiar la pantalla

```java
glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
glClear(GL_COLOR_BUFFER_BIT);
```

`glClearColor` establece el color utilizado para limpiar el buffer de color.

### Depth buffer

El depth buffer guarda la profundidad de los fragmentos. Permite determinar que objeto esta delante de otro.

```java
glEnable(GL_DEPTH_TEST);
glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
```

Sin el depth test, un objeto lejano podria aparecer delante de uno cercano dependiendo del orden en que se dibujen.

### Viewport

El viewport define el area donde OpenGL dibuja:

```java
glViewport(0, 0, ancho, alto);
```

Cuando la ventana cambia de tamano, normalmente se actualizan el viewport y la relacion de aspecto.

---

## 15. Transformaciones y camara

Las transformaciones permiten colocar los objetos dentro de una escena.

- **Traslacion**: mueve un objeto.
- **Escala**: cambia su tamano.
- **Rotacion**: gira un objeto.

En una escena 3D se utilizan habitualmente tres matrices:

1. **Modelo**: transforma el objeto desde sus coordenadas locales.
2. **Vista**: representa la posicion y orientacion de la camara.
3. **Proyeccion**: transforma la escena para mostrarla en la pantalla.

La transformacion completa suele expresarse como:

```text
posicion final = proyeccion * vista * modelo * posicion local
```

La proyeccion puede ser:

- **Perspectiva**: los objetos lejanos parecen mas pequenos.
- **Ortografica**: los objetos conservan su tamano aparente.

---

## 16. Entrada del usuario

GLFW permite recibir entradas de teclado y mouse.

### Callback de teclado

Un callback es una funcion que se ejecuta cuando ocurre un evento:

```java
glfwSetKeyCallback(ventana, (ventanaEvento, tecla, codigo, accion, modificadores) -> {
    if (tecla == GLFW_KEY_ESCAPE && accion == GLFW_PRESS) {
        glfwSetWindowShouldClose(ventana, true);
    }
});
```

### Consultar una tecla

Tambien se puede consultar el estado actual:

```java
if (glfwGetKey(ventana, GLFW_KEY_W) == GLFW_PRESS) {
    posicionY += velocidad * deltaTime;
}
```

El callback es adecuado para eventos puntuales. La consulta continua es adecuada para movimiento y acciones que deben mantenerse mientras una tecla esta presionada.

---

## 17. Liberacion de recursos

Los objetos creados por OpenGL deben eliminarse cuando ya no se utilizan:

```java
glDeleteBuffers(vbo);
glDeleteVertexArrays(vao);
glDeleteProgram(programa);
```

Los recursos de GLFW tambien deben liberarse:

```java
glfwDestroyWindow(ventana);
glfwTerminate();
```

No liberar recursos puede producir consumo innecesario de memoria o errores cuando se crean y destruyen ventanas repetidamente.

---

## 18. Resumen de funciones principales

| Funcion | Proposito |
|---|---|
| `main` | Punto de entrada de Java |
| `init` | Inicializa recursos y configuracion |
| `loop` | Repite la actualizacion y el dibujo |
| `update` | Actualiza la logica de la aplicacion |
| `render` | Dibuja la escena |
| `cleanup` | Libera recursos |
| `glfwInit` | Inicializa GLFW |
| `glfwCreateWindow` | Crea una ventana |
| `glfwMakeContextCurrent` | Activa el contexto OpenGL |
| `GL.createCapabilities` | Carga las funciones OpenGL |
| `glfwPollEvents` | Procesa eventos |
| `glfwSwapBuffers` | Presenta el frame |
| `glCreateShader` | Crea un shader |
| `glCompileShader` | Compila un shader |
| `glCreateProgram` | Crea un programa OpenGL |
| `glLinkProgram` | Enlaza shaders |
| `glUseProgram` | Activa un programa |
| `glGenBuffers` | Crea un buffer |
| `glBufferData` | Copia datos al buffer |
| `glGenVertexArrays` | Crea un VAO |
| `glVertexAttribPointer` | Define el formato de atributos |
| `glDrawArrays` | Dibuja vertices |
| `glClear` | Limpia buffers |
| `glEnable` | Activa una capacidad de OpenGL |
| `glDelete*` | Libera recursos |

---

## 19. Flujo general

```text
1. Maven configura el proyecto y descarga las dependencias.
2. Java inicia la aplicacion.
3. GLFW crea la ventana y el contexto.
4. LWJGL permite llamar a OpenGL desde Java.
5. Se crean los shaders.
6. Se crean y configuran VAO y VBO.
7. El loop procesa eventos y actualiza la logica.
8. OpenGL limpia la pantalla y dibuja los vertices.
9. GLFW muestra el frame.
10. cleanup libera los recursos al finalizar.
```

La idea principal es que Java controla la logica, GLFW administra la ventana y los eventos, Maven administra el proyecto, LWJGL conecta Java con las bibliotecas nativas y OpenGL ejecuta el proceso de renderizado mediante shaders y buffers.
