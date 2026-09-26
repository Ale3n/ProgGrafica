package com.grafica;
import java.nio.FloatBuffer;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;
/**
 * Hello world!
 *
 */
public class App 
{
    // Guarda el identificador de la ventana creada por GLFW.
    private long window;
    // Guarda el identificador del programa que contiene los shaders enlazados.
    private int programa;
    // Guarda el identificador del VAO, que recuerda la configuracion de los atributos de vertices.
    private int vao;
    // Guarda el identificador del VBO, que almacena los datos de los vertices.
    private int vbo;

    // Define el ancho inicial de la ventana en pixeles.
    private static final int ANCHO = 800;
    // Define el alto inicial de la ventana en pixeles.
    private static final int ALTO = 600;

    // Coordina la inicializacion, el bucle principal y la liberacion de recursos.
    public void run() {
        // Prepara GLFW, la ventana, OpenGL, los shaders y los buffers.
        init();
        // Mantiene la aplicacion dibujando y procesando eventos hasta que se cierre la ventana.
        loop();

        // Libera los callbacks asociados a la ventana para evitar que queden registrados.
        Callbacks.glfwFreeCallbacks(window);
        // Destruye la ventana y libera sus recursos del sistema operativo.
        GLFW.glfwDestroyWindow(window);
        // Finaliza GLFW y libera los recursos globales de la biblioteca.
        GLFW.glfwTerminate();
        // Elimina los recursos de OpenGL creados por esta aplicacion.
        cleanup();
    }

    public void init() {
        // Inicializa GLFW y comprueba que la biblioteca pudo arrancar correctamente.
        if (!GLFW.glfwInit()) {
            // Detiene la inicializacion si GLFW no esta disponible.
            throw new IllegalStateException("No se pudo inicializar GLFW");
        }

        // Restablece las opciones de ventana a sus valores predeterminados.
        GLFW.glfwDefaultWindowHints();
        // Solicita que la ventana se cree oculta hasta terminar la configuracion de OpenGL.
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        // Permite al usuario cambiar el tamano de la ventana.
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        // Solicita la version mayor 3 de OpenGL.
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        // Solicita la version menor 3, para usar OpenGL 3.3.
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        // Solicita el perfil core, que contiene las funciones modernas de OpenGL.
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        // Solicita compatibilidad forward, requerida por algunas plataformas como macOS.
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        // Crea la ventana con el ancho, alto, titulo y sin monitor ni contexto compartido.
        window = GLFW.glfwCreateWindow(ANCHO, ALTO, "Mi Ventana OpenGL", MemoryUtil.NULL, MemoryUtil.NULL);
        // Comprueba si GLFW fallo al crear la ventana.
        if (window == MemoryUtil.NULL) {
            // Informa el fallo y evita continuar sin una ventana valida.
            throw new RuntimeException("No se pudo crear la ventana");
        }
        // Asocia el contexto OpenGL de la ventana al hilo actual.
        GLFW.glfwMakeContextCurrent(window);
        // Activa la sincronizacion vertical para sincronizar los frames con el monitor.
        GLFW.glfwSwapInterval(1);
        // Hace visible la ventana despues de preparar su contexto.
        GLFW.glfwShowWindow(window);
        // Carga las funciones OpenGL disponibles en el contexto actual.
        GL.createCapabilities();

        // Compila y enlaza los shaders que utilizara el renderizado.
        crearShaders();
        // Crea y configura los buffers con los datos de los vertices del triangulo.
        crearTriangulo();   
    }

    private void loop() {
        // Repite el ciclo mientras GLFW indique que la ventana sigue abierta.
        while (!GLFW.glfwWindowShouldClose(window)) {
            // Limpia el buffer de color y el de profundidad antes de dibujar el nuevo frame.
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            // Activa el programa de shaders que se utilizara para dibujar.
            GL20.glUseProgram(programa);

            // Activa el VAO con la configuracion de los vertices del triangulo.
            GL30.glBindVertexArray(vao);
            // Dibuja tres vertices interpretandolos como un triangulo.
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
            // Desactiva el VAO despues de terminar el dibujo.
            GL30.glBindVertexArray(0);
            // Desactiva el programa de shaders despues del dibujo.
            GL20.glUseProgram(0);

            // Presenta en la ventana el frame que acaba de dibujarse.
            GLFW.glfwSwapBuffers(window);
            // Atiende eventos pendientes de teclado, mouse y ventana.
            GLFW.glfwPollEvents();
        }
    }

    public static void main( String[] args )
    {
        new App().run();
    }

    private void crearShaders() {
        // Guarda el codigo GLSL del vertex shader, que posiciona cada vertice recibido.
        String vertexShaderSource =
            "#version 330 core\n" +
            "layout (location = 0) in vec3 aPos;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = vec4(aPos, 1.0);\n" +
            "}\n";

        // Guarda el codigo GLSL del fragment shader, que asigna el color final de cada fragmento.
        String fragmentShaderSource =
            "#version 330 core\n" +
            "out vec4 color;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    color = vec4(0.5, 0.0, 1.0, 1.0);\n" +
            "}\n";

        // Crea un objeto de shader del tipo vertex shader.
        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        // Envia el codigo GLSL al vertex shader.
        GL20.glShaderSource(vertexShader, vertexShaderSource);
        // Compila el codigo del vertex shader en la GPU.
        GL20.glCompileShader(vertexShader);

        // Comprueba si la compilacion del vertex shader fallo.
        if (GL20.glGetShaderi(vertexShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            // Lanza una excepcion con el registro de error que proporciona OpenGL.
            throw new RuntimeException("Error al compilar el shader de vértices: " + GL20.glGetShaderInfoLog(vertexShader));
        }

        // Crea un objeto de shader del tipo fragment shader.
        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        // Envia el codigo GLSL al fragment shader.
        GL20.glShaderSource(fragmentShader, fragmentShaderSource);
        // Compila el codigo del fragment shader en la GPU.
        GL20.glCompileShader(fragmentShader);

        // Comprueba si la compilacion del fragment shader fallo.
        if (GL20.glGetShaderi(fragmentShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            // Lanza una excepcion con el registro de error que proporciona OpenGL.
            throw new RuntimeException("Error al compilar el shader de fragmentos: " + GL20.glGetShaderInfoLog(fragmentShader));
        }

        // Crea el programa OpenGL que agrupara los shaders compilados.
        programa = GL20.glCreateProgram();
        // Adjunta el vertex shader al programa.
        GL20.glAttachShader(programa, vertexShader);
        // Adjunta el fragment shader al programa.
        GL20.glAttachShader(programa, fragmentShader);
        // Enlaza los shaders para crear un programa ejecutable por OpenGL.
        GL20.glLinkProgram(programa);

        // Comprueba si el enlace del programa fallo.
        if (GL20.glGetProgrami(programa, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            // Lanza una excepcion con el registro de error del enlace.
            throw new RuntimeException("Error al enlazar el programa: " + GL20.glGetProgramInfoLog(programa));
        }

        // Elimina el vertex shader temporal; el programa ya conserva el codigo enlazado.
        GL20.glDeleteShader(vertexShader);
        // Elimina el fragment shader temporal despues del enlace exitoso.
        GL20.glDeleteShader(fragmentShader);

    }

    private void crearTriangulo() {
        // Stub: crear VAO/VBO y cargar datos del triángulo
        float[] vertices = {
            -0.5f, -0.5f, 0.0f,
             0.5f, -0.5f, 0.0f,
             0.0f,  0.5f, 0.0f
        };
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);

        FloatBuffer buffer = MemoryUtil.memAllocFloat(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        // Liberar el vbo y vao poer que ya no haze falta matenerlos activos
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    private void cleanup() {
        // Liberar recursos OpenGL si es necesario
        GL15.glDeleteBuffers(vbo);
        GL30.glDeleteVertexArrays(vao);
        GL20.glDeleteProgram(programa);
    }
}
