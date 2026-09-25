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
public class AppMovimientoTeclado 
{
    private long window;
    private int programa;
    private int vao;
    private int vbo;

    //uOffsetLocation almacena la ubicacion(ID INTERNA) del uniform offset en el shader
    //lo vamos a utilizar para actualizar la posicion del triangulo en el shader
    private int uOffsetLocation;

    //offset del triangulo que se manda al shader cada frame
    private float offsetX = 0.0f;
    private float offsetY = 0.0f;

    //velocidad
    private static final float VELOCIDAD = 1.2f;

    //limite de desplazamiento del triangulo
    private static final float LIMITE = 0.9f;


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
        //esuchador de eventos de teclado esta vez el esc
        //GLFW.glfwSetKeyCallback(En donde escuchar los eventos, le sigue un metodo)
        GLFW.glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE) {
                glfwSetWindowShouldClose(window, true); // Cierra la ventana si se presiona ESC
            }
            
        });
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
        GL.createCapabilities();

        crearShaders();
        crearTriangulo();   
    }
    private void procesarInput(float deltaTiempo) {
        // Aquí puedes procesar el input del teclado para mover el triángulo
        // Por ejemplo, podrías usar las teclas WASD para moverlo
        float paso = VELOCIDAD * deltaTiempo;
        if (GLFW.glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            offsetY += paso;
            
        }
        if (GLFW.glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            offsetY -= paso;
          
        }
        if (GLFW.glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            offsetX -= paso;
            
        }
        if (GLFW.glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            offsetX += paso;
         
        }

        offsetX = Math.max(-LIMITE, Math.min(LIMITE, offsetX));
        offsetY = Math.max(-LIMITE, Math.min(LIMITE, offsetY));
    }

    private void loop() {
        //tiempo ultimo
        float ultimoTiempo = (float)glfwGetTime(); //tiempo inicial
        while (!glfwWindowShouldClose(window)) {
            //movimiento desaclopado de FPS usando DeltaTime
            float tiempoActual = (float)glfwGetTime();
            float deltaTiempo = tiempoActual - ultimoTiempo;
            ultimoTiempo = tiempoActual;
            //actualiza el offset x y el offset y segun el teclado
            procesarInput(deltaTiempo);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Dibujar el triángulo
            glUseProgram(programa);
            //enviamos el offset al shader antes de dibujar
            //apartir de este valor el vertex shader desplaza cada vertice del triangulo
            GL20.glUniform2f(uOffsetLocation, offsetX, offsetY); // Actualizar el uniform offset en el shader
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
        new AppMovimientoTeclado().run();
    }

    private void crearShaders() {
        // Stub: implementar carga/compilación de shaders
        //tomar en cuenta que usar """ es necesario un salto de linea al final del string para que compile correctamente"
        String vertexShaderSource =""" 
        #version 330 core
        layout (location = 0) in vec3 aPos;
        uniform vec2 uOffset; // Uniform para el desplazamiento
        void main()
        {
            vec3 pos = vec3(aPos.xy + uOffset, aPos.z); // Aplicar el desplazamiento
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

        //comprobar si el programa se enlazo correctamente con la gpu
        if (GL20.glGetProgrami(programa, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error al enlazar el programa: " + GL20.glGetProgramInfoLog(programa));
        }
        //obtener la ubicacion del uniform offset en el shader
        uOffsetLocation = GL20.glGetUniformLocation(programa, "uOffset");
        if(uOffsetLocation == -1){
            throw new RuntimeException("No se pudo obtener la ubicación del uniform uOffset");
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
