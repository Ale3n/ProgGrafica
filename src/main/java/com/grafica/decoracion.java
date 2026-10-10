package com.grafica; // Reúne la versión final con las otras tres lecciones.

import static org.lwjgl.glfw.GLFW.*; // Permite consultar la tecla que controla el minimapa.
import static org.lwjgl.opengl.GL33.*; // Permite cambiar viewport, recorte y buffers de dibujo.

/**
 * CIUDAD TERMINADA, ENTREGAS Y MINIMAPA.
 * 
 */
public class decoracion extends iluminacion {

    // ==================== 1. ESTADO DEL JUEGO ====================
    private boolean mostrarMapa = true; // Muestra el minimapa desde el inicio.
    private int entregas = 0; // Cuenta las entregas completadas; también identifica el siguiente destino.
    private float tiempo = 0; // Acumula los segundos de la partida hasta completar el recorrido.
    private static final float[][] DESTINOS = { // Cada fila contiene X y Z de una parada sobre la calle en el mapa 11x11.
        {50, -50}, // Primera entrega: esquina noreste.
        {-50, -50}, // Segunda entrega: esquina noroeste.
        {-50, 50} // Tercera entrega: regreso a la esquina suroeste (punto inicial).
    };

    // ==================== 2. CONTROLES Y REINICIO ====================

    /** Añade el interruptor del minimapa a los controles anteriores. */
    @Override // Amplía las teclas definidas por clase3.
    protected void tecla(int key) {
        super.tecla(key); // Conserva salida, cámara, reinicio y luces.
        if (key == GLFW_KEY_M) { // Comprueba si se pulsó la tecla del mapa.
            mostrarMapa = !mostrarMapa; // Alterna entre mostrar y ocultar la vista superior.
        }
    }

    /** Reinicia el vehículo y el progreso de las entregas. */
    @Override // Amplía el reinicio definido en clase2.
    protected void reiniciar() {
        super.reiniciar(); // Restaura posición, velocidad y orientación del vehículo.
        entregas = 0; // Vuelve a seleccionar la primera parada.
        tiempo = 0; // Reinicia el cronómetro de la partida.
    }

    // ==================== 3. REGLAS DE LAS ENTREGAS ====================

    /** Actualiza el vehículo y comprueba si llegó y frenó en el destino activo. */
    @Override // Añade el objetivo del juego al movimiento heredado.
    protected void actualizar(float deltaTime) {
        super.actualizar(deltaTime); // Procesa aceleración, giro, colisiones y título de la ventana.
        if (pausa) {
            return; // No procesa entregas ni incrementa cronómetro si la pausa está activa.
        }
        if (entregas >= DESTINOS.length) { // Comprueba si ya se completaron todas las paradas.
            return; // Conserva el tiempo final y evita leer fuera del arreglo.
        }
        tiempo += deltaTime; // Suma los segundos de este cuadro al cronómetro.
        float distanciaX = autoX - DESTINOS[entregas][0]; // Calcula la separación horizontal al destino activo.
        float distanciaZ = autoZ - DESTINOS[entregas][1]; // Calcula la separación en profundidad al destino.
        float distanciaCuadrada = distanciaX * distanciaX + distanciaZ * distanciaZ; // Mide cercanía sin calcular raíz cuadrada.
        boolean estaCerca = distanciaCuadrada < 3 * 3; // Acepta un radio de llegada de tres unidades.
        boolean estaFrenando = Math.abs(velocidad) < 1; // Exige circular a menos de una unidad por segundo.
        if (estaCerca && estaFrenando) { // Solo completa la entrega si ambas condiciones se cumplen.
            entregas++; // Selecciona la siguiente parada o completa el juego.
        }
    }

    /** Compone el progreso que se añade al título de la ventana. */
    @Override // Amplía los indicadores de día/noche y faros de clase3.
    protected String estadoExtra() {
        String mensaje = super.estadoExtra(); // Recupera los indicadores de iluminación.
        mensaje += " | M: mapa | "; // Muestra la tecla que alterna el minimapa.
        if (entregas == DESTINOS.length) { // Selecciona el texto de victoria al completar las tres paradas.
            mensaje += "GANASTE en " + (int) tiempo + " s! R: jugar otra vez"; // Muestra tiempo final y opción de reinicio.
        } else { // Durante el recorrido muestra progreso e instrucciones.
            mensaje += "Entregas " + entregas + "/3"; // Indica cuántas paradas se completaron.
            mensaje += " | Frena en la marca dorada | " + (int) tiempo + " s"; // Explica la condición de entrega y el tiempo.
        }
        return mensaje; // Entrega el texto a actualizarTitulo() de clase2.
    }

    // ==================== 4. ESCENA FINAL Y DESTINO ====================

    /** Añade decoración y señal del destino a la escena iluminada. */
    @Override // Amplía el dibujo acumulado de las tres etapas anteriores.
    protected void escena() {
        super.escena(); // Dibuja la ciudad, el auto y las farolas.
        if (!vistaMapa) { // Los detalles pequeños solo son necesarios en la vista principal.
            decorarCiudad(); // Añade árboles, bancos, ventanas y señalización urbana.
            dibujarSenalizacion(); // Añade carteles viales y nombres de sectores.
        }
        if (entregas < DESTINOS.length) { // Dibuja un objetivo únicamente mientras queden entregas.
            dibujarDestino(); // Coloca la marca dorada en la parada activa.
        }
    }

    /** Marca la próxima parada con una plataforma y una baliza flotante. */
    private void dibujarDestino() {
        float x = DESTINOS[entregas][0]; // Lee el X de la próxima entrega.
        float z = DESTINOS[entregas][1]; // Lee el Z de la próxima entrega.
        entero("uEmision", 1); // Hace que el objetivo sea visible incluso de noche.
        caja(x, 0.06f, z, 5, 0.08f, 5, 1, 0.72f, 0.12f); // Dibuja una marca dorada sobre el asfalto.
        if (!vistaMapa) { // Evita añadir una baliza tridimensional al mapa pequeño.
            float alturaBaliza = 3.5f + (float) Math.sin(tiempo * 2) * 0.3f; // Hace oscilar la baliza suavemente.
            cajaGirada(x, alturaBaliza, z, 0.8f, 0.8f, 0.8f, 1, 0.8f, 0.15f, tiempo); // Dibuja el cubo giratorio del objetivo.
        }
        entero("uEmision", 0); // Devuelve a los siguientes objetos su iluminación normal.
    }

    // ==================== 5. DECORACIÓN DE LAS MANZANAS ====================

    /** Decide qué decoración corresponde a cada tipo de parcela. */
    private void decorarCiudad() {
        for (int fila = 0; fila < MAPA.length; fila++) { // Recorre las filas del mapa.
            for (int columna = 0; columna < MAPA[fila].length; columna++) { // Recorre las columnas de esa fila.
                float x = centro(columna); // Obtiene el centro horizontal de la parcela.
                float z = centro(fila); // Obtiene el centro de la parcela en profundidad.
                int tipo = MAPA[fila][columna]; // Lee el contenido de la celda.
                if (tipo == 2) { // Detecta una parcela de parque.
                    dibujarParque(x, z); // Añade árboles y un banco.
                }
                if (tipo == 1) { // Detecta una parcela con edificio.
                    float altura = 5 + (fila * 3 + columna * 7) % 9; // Recupera la misma altura calculada en clase1.
                    dibujarVentanas(x, z, altura); // Coloca ventanas en sus cuatro fachadas.
                }
                if (tipo != 0) { // La señalización se coloca junto a las manzanas, no en celdas de calle.
                    dibujarPasoPeatonal(x, z, fila,columna); // Añade el cruce conectando ambas aceras contiguas.
                    dibujarSemaforo(x + 4, z - 4); // Coloca el semáforo dentro de la acera.
                }
            }
        }
    }

    /** Construye cuatro árboles y un banco utilizando cajas. */
    private void dibujarParque(float x, float z) {
        float[] posiciones = {-2.5f, 2.5f}; // Define desplazamientos respecto al centro de la parcela.
        for (float desplazamientoX : posiciones) { // Selecciona el lado izquierdo o derecho del parque.
            for (float desplazamientoZ : posiciones) { // Selecciona el lado delantero o trasero.
                float arbolX = x + desplazamientoX; // Convierte el desplazamiento local en coordenada X mundial.
                float arbolZ = z + desplazamientoZ; // Convierte el desplazamiento local en coordenada Z mundial.
                caja(arbolX, 1.2f, arbolZ, 0.35f, 2, 0.35f, 0.38f, 0.22f, 0.12f); // Dibuja el tronco marrón.
                caja(arbolX, 2.7f, arbolZ, 2, 2.3f, 2, 0.12f, 0.42f, 0.23f); // Dibuja la copa verde del árbol.
            }
        }
        caja(x, 0.65f, z, 3, 0.25f, 0.8f, 0.55f, 0.30f, 0.13f); // Dibuja el asiento de madera del banco.
        caja(x, 0.4f, z, 2, 0.6f, 0.35f, 0.22f, 0.24f, 0.24f); // Dibuja el soporte oscuro del banco.
        caja(x, 1, z + 0.35f, 3, 0.7f, 0.15f, 0.55f, 0.30f, 0.13f); // Dibuja el respaldo detrás del asiento.
    }

    /** Distribuye ventanas por pisos en las cuatro paredes del edificio. */
    private void dibujarVentanas(float x, float z, float altura) {
        if (noche) { // Las ventanas simulan habitaciones encendidas en el ambiente nocturno.
            entero("uEmision", 1); // Permite ver el color de las ventanas sin depender de farolas.
        }
        for (float y = 1.7f; y < altura; y += 2) { // Recorre los pisos separados por dos unidades de altura.
            for (float desplazamiento = -2; desplazamiento <= 2; desplazamiento += 2) { // Coloca tres ventanas por fachada.
                caja(x + desplazamiento, y, z - 3.51f, 0.8f, 0.9f, 0.04f, 0.95f, 0.75f, 0.38f); // Ventana de la fachada norte.
                caja(x + desplazamiento, y, z + 3.51f, 0.8f, 0.9f, 0.04f, 0.95f, 0.75f, 0.38f); // Ventana de la fachada sur.
                caja(x - 3.51f, y, z + desplazamiento, 0.04f, 0.9f, 0.8f, 0.95f, 0.75f, 0.38f); // Ventana de la fachada oeste.
                caja(x + 3.51f, y, z + desplazamiento, 0.04f, 0.9f, 0.8f, 0.95f, 0.75f, 0.38f); // Ventana de la fachada este.
            }
        }
        entero("uEmision", 0); // Restablece la iluminación normal de los demás elementos.
    }

    /** Dibuja las franjas blancas del paso peatonal conectando de una acera a la acera de enfrente. */
    /** Paso de cebra: franjas paralelas a la calle, de una acera a la acera de enfrente. */
    private void dibujarPasoPeatonal(float x, float z, int fila, int columna) {
        // Calle al sur de la manzana, solo si al otro lado hay otra manzana con acera.
        if (fila + 2 < MAPA.length && MAPA[fila + 2][columna] != 0) {
            for (int i = 0; i < 8; i++) { // 8 franjas de 0.6 con huecos de 0.6
                float franjaZ = (z + 10) - 4.2f + i * 1.2f;
                caja(x + 2.5f, 0.035f, franjaZ, 4, 0.02f, 0.6f, 0.9f, 0.9f, 0.9f);
            }
        }
        // Calle al este, con la misma regla; aquí las franjas van a lo largo de Z.
        if (columna + 2 < MAPA[fila].length && MAPA[fila][columna + 2] != 0) {
            for (int i = 0; i < 8; i++) {
                float franjaX = (x + 10) - 4.2f + i * 1.2f;
                caja(franjaX, 0.035f, z+2.5f, 0.6f, 0.02f, 4, 0.9f, 0.9f, 0.9f);
            }
        }
    }

    /** Construye un semáforo decorativo que alterna rojo, verde y amarillo cada 12 segundos. */
    private void dibujarSemaforo(float x, float z) {
        caja(x, 1.7f, z, 0.18f, 2.8f, 0.18f, 0.18f, 0.20f, 0.22f); // Dibuja el poste sobre la acera.
        caja(x, 3.1f, z, 0.55f, 1.2f, 0.45f, 0.08f, 0.10f, 0.12f); // Dibuja la carcasa de las tres luces.
        int fase = (int) (tiempo % 12); // Repite un ciclo de segundos comprendidos entre 0 y 11.
        for (int indice = 0; indice < 3; indice++) { // Recorre rojo arriba, amarillo al centro y verde abajo.
            boolean encendida = false; // Parte de una bombilla apagada.
            if (indice == 0) { // Selecciona la bombilla roja.
                encendida = fase < 5; // Mantiene rojo durante los primeros cinco segundos.
            } else if (indice == 1) { // Selecciona la bombilla amarilla.
                encendida = fase >= 10; // Mantiene amarillo en los últimos dos segundos del ciclo.
            } else { // Selecciona la bombilla verde.
                encendida = fase >= 5 && fase < 10; // Mantiene verde durante los cinco segundos intermedios.
            }
            float brillo = 0.15f; // Conserva un color tenue cuando la bombilla está apagada.
            entero("uEmision", 0); // Configura inicialmente una superficie sin emisión.
            if (encendida) { // Comprueba si esta bombilla corresponde a la fase activa.
                brillo = 1; // Usa intensidad completa para su color.
                entero("uEmision", 1); // Hace que la bombilla se vea encendida.
            }
            float rojo = 0; // Componente roja inicialmente ausente.
            float verde = 0; // Componente verde inicialmente ausente.
            if (indice < 2) { // Rojo y amarillo necesitan componente roja.
                rojo = brillo; // Añade rojo con la intensidad elegida.
            }
            if (indice > 0) { // Amarillo y verde necesitan componente verde.
                verde = brillo; // Añade verde; rojo más verde produce amarillo.
            }
            float altura = 3.45f - indice * 0.35f; // Separa verticalmente las tres bombillas.
            caja(x, altura, z - 0.24f, 0.28f, 0.25f, 0.06f, rojo, verde, 0.02f); // Dibuja la bombilla frente a la carcasa.
        }
        entero("uEmision", 0); // Evita que el siguiente objeto herede la emisión del semáforo.
    }

    // ==================== 6. MINIMAPA: SEGUNDO PASE DE DIBUJO ====================

    /** Dibuja la escena principal y después la misma ciudad desde arriba, en un recuadro. */
    @Override // Amplía el cuadro completo definido en clase1.
    protected void dibujarFrame() {
        super.dibujarFrame(); // Dibuja primero la vista normal de la ciudad.
        if (mostrarMapa) { // Dibuja el minimapa solo cuando está habilitado.
            int dimensionMenor = Math.min(ancho, alto); // Busca la dimensión que limita el espacio disponible.
            int lado = Math.min(260, dimensionMenor / 3); // Limita el mapa a 260 píxeles y a un tercio de la ventana.
            int margen = Math.min(18, dimensionMenor / 20); // Calcula una separación adaptable respecto a los bordes.
            int x = ancho - lado - margen; // Ubica el recuadro cerca del borde derecho.
            int y = alto - lado - margen; // Ubica el recuadro arriba; OpenGL mide Y desde abajo.
            glEnable(GL_SCISSOR_TEST); // Activa el recorte para no borrar el resto de la escena.
            glScissor(x - 3, y - 3, lado + 6, lado + 6); // Selecciona el mapa más un borde de tres píxeles.
            glClearColor(0.8f, 0.87f, 0.94f, 1); // Define un color claro para el marco.
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Borra solo el recuadro exterior gracias al scissor.
            glScissor(x, y, lado, lado); // Reduce el recorte al interior del minimapa.
            glClearColor(0.06f, 0.10f, 0.15f, 1); // Define el fondo oscuro del mapa.
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Limpia color y profundidad dentro del mapa.
            glViewport(x, y, lado, lado); // Redirige la proyección al recuadro cuadrado.
            entero("uMapa", 1); // Selecciona la proyección ortográfica del shader de clase1.
            vistaMapa = true; // Indica a escena() que omita decoración pequeña y baliza flotante.
            try { // Asegura que el estado de dibujo se restaure incluso si el segundo pase falla.
                escena(); // Dibuja otra vez la misma ciudad, ahora vista desde arriba.
                dibujarIndicadorAuto(); // Resalta la posición y el frente del jugador en el mapa.
                //dibujarPasoPeatonal(x, z, lado,lado);
            } finally { // El siguiente cuadro debe volver a la configuración de pantalla completa.
                vistaMapa = false; // Reactiva los detalles de la escena principal.
                entero("uMapa", 0); // Recupera la proyección en perspectiva.
                glDisable(GL_SCISSOR_TEST); // Permite que la próxima limpieza abarque toda la pantalla.
                glViewport(0, 0, ancho, alto); // Recupera el área de dibujo de la ventana completa.
            }
            if (glGetError() != GL_NO_ERROR) { // Comprueba que el pase del mapa no haya generado errores OpenGL.
                throw new IllegalStateException("Error OpenGL en minimapa"); // Expone el error en la consola.
            }
        }
        dibujarHUD(); // Dibuja el panel dentro de la ventana: velocímetro, estado de luces y menú de pausa.
    }

    /** Dibuja una marca cian y una punta blanca por encima de los edificios del minimapa. */
    private void dibujarIndicadorAuto() {
        cajaGirada(autoX, 25, autoZ, 2.2f, 0.1f, 3.2f, 0.1f, 1, 1, angulo); // Marca la posición con un rectángulo cian orientado.
        float frenteX = -(float) Math.sin(angulo); // Calcula la dirección frontal en el eje X.
        float frenteZ = -(float) Math.cos(angulo); // Calcula la dirección frontal en el eje Z.
        float puntaX = autoX + frenteX * 2; // Desplaza la punta dos unidades hacia delante en X.
        float puntaZ = autoZ + frenteZ * 2; // Desplaza la punta dos unidades hacia delante en Z.
        caja(puntaX, 26, puntaZ, 0.9f, 0.1f, 0.9f, 1, 1, 1); // Dibuja la punta blanca encima del indicador cian.
    }

    // ==================== 7. SEÑALIZACIÓN URBANA Y PÓRTICOS ====================

    /** Añade señalización urbana en esquinas y pórticos con nombres de sectores sobre las avenidas. */
    private void dibujarSenalizacion() {
        // Pórticos elevados tipo autopista sobre avenidas principales con nombres de sectores
        dibujarPorticoVial(0, -30, 0.10f, 0.45f, 0.20f); // Pórtico Norte: Verde vial
        dibujarPorticoVial(0, 10, 0.14f, 0.38f, 0.65f); // Pórtico Central: Azul vial
        dibujarPorticoVial(0, 30, 0.10f, 0.45f, 0.20f); // Pórtico Sur: Verde vial

        // Señales de STOP / ALTO y de velocidad "40" en las esquinas de las aceras
        float[] esquinas = {-40, -20, 0, 20, 40};
        for (float ex : esquinas) {
            for (float ez : esquinas) {
                // Cartel de ALTO (poste + panel rojo con barra blanca) en esquina noreste
                caja(ex + 4.6f, 1.4f, ez - 4.6f, 0.1f, 2.8f, 0.1f, 0.4f, 0.44f, 0.48f); // Poste metálico
                caja(ex + 4.6f, 2.7f, ez - 4.6f, 0.75f, 0.34f, 0.08f, 0.88f, 0.12f, 0.12f);
                caja(ex + 4.6f, 2.48f, ez - 4.6f, 0.55f, 0.20f, 0.08f, 0.88f, 0.12f, 0.12f);
                caja(ex + 4.6f, 2.92f, ez - 4.6f, 0.55f, 0.20f, 0.08f, 0.88f, 0.12f, 0.12f);
                caja(ex + 4.6f, 2.7f, ez - 4.6f, 0.48f, 0.10f, 0.10f, 0.95f, 0.95f, 0.95f); // Banda reflectante central

                // Cartel de velocidad "40" en esquina suroeste
                caja(ex - 4.6f, 1.4f, ez + 4.6f, 0.1f, 2.8f, 0.1f, 0.4f, 0.44f, 0.48f); // Poste metálico
                caja(ex - 4.6f, 2.7f, ez + 4.6f, 0.75f, 0.75f, 0.08f, 0.92f, 0.15f, 0.15f); // Aro rojo
                caja(ex - 4.6f, 2.7f, ez + 4.6f, 0.55f, 0.55f, 0.09f, 0.95f, 0.95f, 0.95f); // Fondo blanco
                dibujarCifraSenal('4', ex - 4.6f, 2.7f, ez + 4.6f, -0.05f);
                dibujarCifraSenal('0', ex - 4.6f, 2.7f, ez + 4.6f, 0.10f);
            }
        }
    }

    /** Dibuja una cifra 3x5 en ambas caras de una señal de velocidad. */
    private void dibujarCifraSenal(char cifra, float x, float y, float z, float desplazamientoX) {
        String glifo = patronGlifo(cifra);
        float tamanoCelda = 0.055f;
        for (int fila = 0; fila < 5; fila++) {
            for (int columna = 0; columna < 3; columna++) {
                if (glifo.charAt(fila * 3 + columna) != '1') {
                    continue;
                }
                float pixelX = x + desplazamientoX + (columna - 1) * tamanoCelda;
                float pixelY = y + (2 - fila) * tamanoCelda;
                caja(pixelX, pixelY, z - 0.052f, 0.042f, 0.045f, 0.018f, 0.12f, 0.14f, 0.16f);
                caja(pixelX, pixelY, z + 0.052f, 0.042f, 0.045f, 0.018f, 0.12f, 0.14f, 0.16f);
            }
        }
    }

    /** Construye un pórtico elevado tipo autopista con cartel de sector sobre la calzada. */
    private void dibujarPorticoVial(float x, float z, float r, float g, float b) {
        caja(x, 2.7f, z-5, 0.25f, 5f, 0.25f, 0.32f, 0.36f, 0.40f); // Poste izquierdo
        caja(x, 2.7f, z+5, 0.25f, 5f, 0.25f, 0.32f, 0.36f, 0.40f); // Poste derecho
        caja(x, 5.3f, z, 0.25f, 0.25f, 10f, 0.36f, 0.40f, 0.44f); // Viga superior metálica
        caja(x, 4.3f, z, 0.25f, 1.5f, 9.5f, 0.92f, 0.92f, 0.92f); // Marco blanco exterior del cartel
        caja(x, 4.3f, z, 0.29f, 1.3f, 5.9f, r, g, b); // Fondo reflectante del cartel
        caja(x, 4.3f, z, 0.3f, 0.15f, 2.5f, 0.95f, 0.95f, 0.95f); // Franja central de información
    }

    // ==================== 8. INTERFAZ HUD 2D Y MENÚ DE PAUSA ====================

    /** Renderiza la interfaz gráfica en pantalla: dashboard, velocímetro, estado de luces y menú modal de pausa. */
    private void dibujarHUD() {
        glDisable(GL_DEPTH_TEST); // Superpone la interfaz por encima del mundo 3D.
        glEnable(GL_BLEND); // Activa transparencia para efecto de cristal oscuro (glassmorphism).
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glViewport(0, 0, ancho, alto); // Conecta la proyección a la ventana completa.
        try {
            rect2D(-0.68f, -0.73f, 0.60f, 0.44f, 0.035f, 0.065f, 0.10f, 0.88f);
            rect2D(-0.68f, -0.515f, 0.60f, 0.008f, 0.18f, 0.75f, 0.92f, 0.95f);
            dibujarTexto("VELOCIDAD", -0.94f, -0.585f, 0.026f, 0.60f, 0.78f, 0.85f);

            // Tacómetro gráfico: barra dinámica que crece y cambia de color según la aceleración
            float fraccionVelocidad = Math.min(1.0f, Math.abs(velocidad) / 16.0f);
            rect2D(-0.68f, -0.68f, 0.52f, 0.045f, 0.12f, 0.16f, 0.22f, 0.95f); // Fondo de la barra
            if (fraccionVelocidad > 0.02f) {
                rect2D(-0.94f + 0.26f * fraccionVelocidad, -0.68f, 0.52f * fraccionVelocidad, 0.035f,
                    0.20f + 0.80f * fraccionVelocidad, 0.82f - 0.48f * fraccionVelocidad, 0.18f, 1.0f);
            }
            // Marcas de nivel en el tacómetro (25%, 50%, 75%)
            rect2D(-0.81f, -0.68f, 0.004f, 0.045f, 0.30f, 0.38f, 0.46f, 0.85f);
            rect2D(-0.68f, -0.68f, 0.004f, 0.045f, 0.30f, 0.38f, 0.46f, 0.85f);
            rect2D(-0.55f, -0.68f, 0.004f, 0.045f, 0.30f, 0.38f, 0.46f, 0.85f);

            float rojoFaros = faros ? 1.0f : 0.35f;
            float verdeFaros = faros ? 0.76f : 0.40f;
            rect2D(-0.93f, -0.80f, 0.025f, 0.025f, rojoFaros, verdeFaros, 0.22f, 1.0f);
            dibujarTexto(faros ? "FAROS ON" : "FAROS OFF", -0.90f, -0.81f, 0.022f,
                faros ? 0.96f : 0.62f, faros ? 0.82f : 0.67f, 0.52f);
            rect2D(-0.62f, -0.80f, 0.025f, 0.025f, noche ? 0.35f : 0.98f, 0.75f, 0.30f, 1.0f);
            dibujarTexto(noche ? "NOCHE" : "DIA", -0.59f, -0.81f, 0.022f, 0.82f, 0.87f, 0.94f);

            dibujarTexto("ENTREGAS", -0.93f, -0.90f, 0.020f, 0.72f, 0.80f, 0.86f);
            int totalDestinos = Math.max(1, DESTINOS.length);
            float espacio = totalDestinos > 1 ? Math.min(0.075f, 0.18f / (totalDestinos - 1)) : 0.075f;
            float anchoBloque = Math.min(0.052f, espacio * 0.72f);
            for (int indice = 0; indice < totalDestinos; indice++) {
                float colorR = indice < entregas ? 0.18f : (indice == entregas ? 1.0f : 0.25f);
                float colorG = indice < entregas ? 0.88f : (indice == entregas ? 0.72f : 0.28f);
                float colorB = indice < entregas ? 0.35f : (indice == entregas ? 0.15f : 0.32f);
                rect2D(-0.57f + indice * espacio, -0.895f, anchoBloque, 0.025f,
                    colorR, colorG, colorB, indice < entregas + 1 ? 0.95f : 0.60f);
            }

            rect2D(-0.80f, 0.91f, 0.32f, 0.06f, 0.06f, 0.10f, 0.16f, 0.88f);
            rect2D(-0.80f, 0.882f, 0.32f, 0.005f, 0.18f, 0.75f, 0.95f, 0.95f);
            dibujarTexto("P AYUDA", -0.92f, 0.90f, 0.022f, 0.70f, 0.88f, 0.94f);

            if (pausa) {
                rect2D(0.0f, 0.0f, 2.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.68f);
                rect2D(0.0f, 0.04f, 0.96f, 0.86f, 0.06f, 0.09f, 0.14f, 0.96f);
                rect2D(0.0f, 0.46f, 0.96f, 0.015f, 0.18f, 0.82f, 0.95f, 1.0f);
                rect2D(0.0f, -0.38f, 0.96f, 0.012f, 0.18f, 0.82f, 0.95f, 1.0f);
                rect2D(0.0f, 0.39f, 0.88f, 0.08f, 0.12f, 0.22f, 0.36f, 0.95f);
                dibujarTexto("PAUSA Y CONTROLES", -0.30f, 0.365f, 0.04f, 0.96f, 0.98f, 1.0f);

                dibujarFilaControl(0.29f, "WASD", "CONDUCIR", 0.95f, 0.30f, 0.20f);
                dibujarFilaControl(0.21f, "ESPACIO", "FRENAR", 0.95f, 0.65f, 0.20f);
                dibujarFilaControl(0.13f, "C", "CAMARA", 0.20f, 0.75f, 0.95f);
                dibujarFilaControl(0.05f, "N", "DIA / NOCHE", 0.45f, 0.60f, 0.95f);
                dibujarFilaControl(-0.03f, "F", "FAROS", 0.95f, 0.85f, 0.25f);
                dibujarFilaControl(-0.11f, "M", "MINIMAPA", 0.20f, 0.85f, 0.70f);
                dibujarFilaControl(-0.19f, "R", "REINICIAR", 0.90f, 0.25f, 0.25f);
                dibujarFilaControl(-0.27f, "P / ESC", "REANUDAR / SALIR", 0.20f, 0.88f, 0.35f);

                rect2D(0.0f, -0.33f, 0.36f, 0.06f, 0.16f, 0.75f, 0.36f, 0.95f);
                dibujarTexto("P REANUDAR", -0.115f, -0.34f, 0.024f, 1.0f, 1.0f, 1.0f);
            }
        } finally {
            glDisable(GL_BLEND);
            glEnable(GL_DEPTH_TEST); // Restablece profundidad para la escena 3D del próximo cuadro.
        }
    }

    /** Dibuja una fila estilizada de control con su tecla y su acción representativa. */
    private void dibujarFilaControl(float y, String tecla, String accion, float r, float g, float b) {
        rect2D(-0.26f, y, 0.32f, 0.055f, r, g, b, 0.92f);
        rect2D(0.18f, y, 0.50f, 0.055f, 0.14f, 0.18f, 0.24f, 0.85f);
        dibujarTexto(tecla, -0.40f, y - 0.012f, 0.022f, 0.06f, 0.08f, 0.10f);
        dibujarTexto(accion, -0.045f, y - 0.012f, 0.022f, 0.84f, 0.89f, 0.94f);
    }

    /** Renderiza texto compacto con glifos 3x5 para no depender de una biblioteca externa. */
    private void dibujarTexto(String texto, float x, float y, float altoTexto, float r, float g, float b) {
        float altoCelda = altoTexto / 5.0f;
        float anchoCelda = altoCelda * (float) alto / ancho;
        float avance = anchoCelda * 4.0f;

        for (int indice = 0; indice < texto.length(); indice++) {
            String glifo = patronGlifo(texto.charAt(indice));
            for (int fila = 0; fila < 5; fila++) {
                for (int columna = 0; columna < 3; columna++) {
                    if (glifo.charAt(fila * 3 + columna) == '1') {
                        float pixelX = x + indice * avance + (columna + 0.5f) * anchoCelda;
                        float pixelY = y + (4 - fila + 0.5f) * altoCelda;
                        rect2D(pixelX, pixelY, anchoCelda * 0.82f, altoCelda * 0.82f, r, g, b, 1.0f);
                    }
                }
            }
        }
    }

    /** Devuelve cinco filas de tres píxeles para letras, cifras y separadores del HUD. */
    private String patronGlifo(char caracter) {
        switch (Character.toUpperCase(caracter)) {
            case 'A': return "010101111101101";
            case 'B': return "110101110101110";
            case 'C': return "011100100100011";
            case 'D': return "110101101101110";
            case 'E': return "111100110100111";
            case 'F': return "111100110100100";
            case 'G': return "011100101101011";
            case 'H': return "101101111101101";
            case 'I': return "111010010010111";
            case 'J': return "001001001101010";
            case 'K': return "101101110101101";
            case 'L': return "100100100100111";
            case 'M': return "101111111101101";
            case 'N': return "101111111111101";
            case 'O': return "010101101101010";
            case 'P': return "110101110100100";
            case 'Q': return "010101101111011";
            case 'R': return "110101110101101";
            case 'S': return "011100010001110";
            case 'T': return "111010010010010";
            case 'U': return "101101101101111";
            case 'V': return "101101101101010";
            case 'W': return "101101111111101";
            case 'X': return "101101010101101";
            case 'Y': return "101101010010010";
            case 'Z': return "111001010100111";
            case '0': return "111101101101111";
            case '1': return "010110010010111";
            case '2': return "110001010100111";
            case '3': return "110001010001110";
            case '4': return "101101111001001";
            case '5': return "111100110001110";
            case '6': return "011100110101010";
            case '7': return "111001010010010";
            case '8': return "010101010101010";
            case '9': return "010101011001110";
            case '/': return "001001010100100";
            case ':': return "000010000010000";
            case '-': return "000000111000000";
            default: return "000000000000000";
        }
    }

    /** Punto de entrada del proyecto final. */
    public static void main(String[] args) {
        decoracion aplicacion = new decoracion(); // Crea la versión con todas las etapas acumuladas.
        aplicacion.run(); // Inicia el ciclo de vida completo de la aplicación.
    }
}
