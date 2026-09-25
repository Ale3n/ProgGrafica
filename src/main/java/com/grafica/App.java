package com.grafica;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import org.lwjgl.opengl.GL15;
import static org.lwjgl.system.MemoryUtil.*;
import java.nio.FloatBuffer;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
/**
 * Hello world!
 *
 */
public class App 
{
    private long window;
    private int programa;
    private int vao;
    private int vbo;

    private static final int ANCHO = 800;
    private static final int ALTO = 600;

    public void run() {
        init();
        loop();

        // Liberar recursos y cerrar la ventana
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        cleanup();
    }

    public void init() {
        // Inicializar GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("No se pudo inicializar GLFW");
        }

        // Configurar la ventana
        glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        window = glfwCreateWindow(ANCHO, ALTO, "Mi Ventana OpenGL", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("No se pudo crear la ventana");
        }
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
        GL.createCapabilities();

        crearShaders();
        crearTriangulo();   
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Dibujar el triángulo
            glUseProgram(programa);

            
            glBindVertexArray(vao);
            glDrawArrays(GL_TRIANGLES, 0, 3);
            glBindVertexArray(0);
            glUseProgram(0);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main( String[] args )
    {
        new App().run();
    }

    private void crearShaders() {
        // Stub: implementar carga/compilación de shaders
        String vertexShaderSource =
            "#version 330 core\n" +
            "layout (location = 0) in vec3 aPos;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = vec4(aPos, 1.0);\n" +
            "}\n";

        String fragmentShaderSource =
            "#version 330 core\n" +
            "out vec4 color;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    color = vec4(0.5, 0.0, 1.0, 1.0);\n" +
            "}\n";
        int vertexShader = GL20.glCreateShader(GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexShader, vertexShaderSource);
        GL20.glCompileShader(vertexShader);

        if (GL20.glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error al compilar el shader de vértices: " + GL20.glGetShaderInfoLog(vertexShader));
        }

        int fragmentShader = GL20.glCreateShader(GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, fragmentShaderSource);
        GL20.glCompileShader(fragmentShader);

        if (GL20.glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error al compilar el shader de fragmentos: " + GL20.glGetShaderInfoLog(fragmentShader));
        }

        programa = GL20.glCreateProgram();
        GL20.glAttachShader(programa, vertexShader);
        GL20.glAttachShader(programa, fragmentShader);
        GL20.glLinkProgram(programa);

        if (GL20.glGetProgrami(programa, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error al enlazar el programa: " + GL20.glGetProgramInfoLog(programa));
        }

        GL20.glDeleteShader(vertexShader);
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

        FloatBuffer buffer = memAllocFloat(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
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
