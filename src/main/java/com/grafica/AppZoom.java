package com.grafica;
import org.lwjgl.opengl.GL15;
import java.nio.FloatBuffer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;
/**
 * Hello world!
 *
 */
public class AppZoom 
{
    private long window;
    private int programa;
    private int vao;
    private int vbo;

    private static final int ANCHO = 800;
    private static final int ALTO = 600;

    private int uZoomLocation; // Ubicación del uniform zoom en el shader

    private float zoom = 1.0f; // Valor inicial del zoom
    private static final float ZOOM_MIN = 0.25f; // Valor mínimo del zoom
    private static final float ZOOM_MAX = 3.00f; // Valor máximo del zoom
    private static final float VELOCIDAD_ZOOM = 1.25f; // Incremento del zoom


    public void run() {
        init();
        loop();

        // Liberar recursos y cerrar la ventana
        Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
        cleanup();
    }

    public void init() {
        // Inicializar GLFW
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("No se pudo inicializar GLFW");
        }

        // Configurar la ventana
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        window = GLFW.glfwCreateWindow(ANCHO, ALTO, "Mi Ventana OpenGL", MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("No se pudo crear la ventana");
        }

        //esuchador de eventos de teclado esta vez el esc
        //GLFW.glfwSetKeyCallback(En donde escuchar los eventos, le sigue un metodo)
        GLFW.glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                GLFW.glfwSetWindowShouldClose(window, true); // Cierra la ventana si se presiona ESC
            }
            
        });

        GLFW.glfwSetScrollCallback(window, (W, xoffset, yoffset) -> {
            zoom+= (float)yoffset * 0.10f;
            zoom = clamp(zoom, ZOOM_MIN, ZOOM_MAX);
        });
        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);
        GL.createCapabilities();

        crearShaders();
        crearTriangulo();   
    }
    // Método para limitar el valor del zoom dentro de los límites establecidos
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private void loop() {
        //tiempo ultimo
        float ultimoTiempo = (float)GLFW.glfwGetTime(); //tiempo inicial
        while (!GLFW.glfwWindowShouldClose(window)) {
            //movimiento desaclopado de FPS usando DeltaTime
            float tiempoActual = (float)GLFW.glfwGetTime();
            float deltaTiempo = tiempoActual - ultimoTiempo;
            ultimoTiempo = tiempoActual;
            procesarInput(deltaTiempo);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            // Dibujar el triángulo
            GL20.glUseProgram(programa);

            GL20.glUniform1f(uZoomLocation, zoom); // Actualizar el valor del uniform zoom en el shader
            GL30.glBindVertexArray(vao);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
            GL30.glBindVertexArray(0);
            GL20.glUseProgram(0);

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
    }

    public static void main( String[] args )
    {
        new AppZoom().run();
    }

    private void crearShaders() {
        // Stub: implementar carga/compilación de shaders
        //tomar en cuenta que usar """ es necesario un salto de linea al final del string para que compile correctamente"
        String vertexShaderSource =""" 
        #version 330 core
        layout (location = 0) in vec3 aPos;
        uniform float uZoom; // Uniform para el zoom
        void main()
        {
            vec3 pos = vec3(aPos.xy * uZoom, aPos.z); // Aplicar el zoom
            gl_Position = vec4(pos, 1.0);
        }
        """;

        String fragmentShaderSource = """
        #version 330 core
        out vec4 color;
        void main()
        {
            color = vec4(0.5, 0.0, 1.0, 1.0);
            }
        """;
        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexShader, vertexShaderSource);
        GL20.glCompileShader(vertexShader);

        if (GL20.glGetShaderi(vertexShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Error al compilar el shader de vértices: " + GL20.glGetShaderInfoLog(vertexShader));
        }

        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, fragmentShaderSource);
        GL20.glCompileShader(fragmentShader);

        if (GL20.glGetShaderi(fragmentShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Error al compilar el shader de fragmentos: " + GL20.glGetShaderInfoLog(fragmentShader));
        }

        programa = GL20.glCreateProgram();
        GL20.glAttachShader(programa, vertexShader);
        GL20.glAttachShader(programa, fragmentShader);
        GL20.glLinkProgram(programa);

        if (GL20.glGetProgrami(programa, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Error al enlazar el programa: " + GL20.glGetProgramInfoLog(programa));
        }

        uZoomLocation = GL20.glGetUniformLocation(programa, "uZoom"); // Obtener la ubicación del uniform zoom  
        // Verificar si se obtuvo correctamente la ubicación del uniform
        if (uZoomLocation == -1) {
            throw new RuntimeException("No se pudo obtener la ubicación del uniform uZoom");
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

        FloatBuffer buffer = MemoryUtil.memAllocFloat(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        // Liberar el vbo y vao poer que ya no haze falta matenerlos activos
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    private void procesarInput(float deltaTiempo) {
        float paso = VELOCIDAD_ZOOM * deltaTiempo; // Ajustar el paso de zoom según el tiempo transcurrido
        if(GLFW.glfwGetKey(window, GLFW.GLFW_KEY_KP_ADD) == GLFW.GLFW_PRESS || 
           GLFW.glfwGetKey(window, GLFW.GLFW_KEY_EQUAL) == GLFW.GLFW_PRESS) {
            zoom += paso;
            
        }

        if(GLFW.glfwGetKey(window, GLFW.GLFW_KEY_KP_SUBTRACT) == GLFW.GLFW_PRESS || 
           GLFW.glfwGetKey(window, GLFW.GLFW_KEY_MINUS) == GLFW.GLFW_PRESS) {
            zoom -= paso;
        }
        zoom = clamp(zoom, ZOOM_MIN, ZOOM_MAX); // Limitar el valor del zoom dentro de los límites establecidos
    }

    private void cleanup() {
        // Liberar recursos OpenGL si es necesario
        GL15.glDeleteBuffers(vbo);
        GL30.glDeleteVertexArrays(vao);
        GL20.glDeleteProgram(programa);
    }
}
