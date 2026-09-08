package org.example.hexgame.entities;

import com.almasb.fxgl.dsl.FXGL;

/**
 * Контроллер камеры для управления видом карты
 */
public class CameraController {
    private double cameraX = 0;
    private double cameraY = 0;
    private double zoom = 1.0;
    
    public void moveCamera(double dx, double dy) {
        cameraX += dx;
        cameraY += dy;
        updateCamera();
    }
    
    public void zoomCamera(double zoomFactor) {
        zoom *= zoomFactor;
        zoom = Math.max(0.5, Math.min(2.0, zoom)); // Ограничение зума
        updateCamera();
    }
    
    public void resetCamera() {
        cameraX = 0;
        cameraY = 0;
        zoom = 1.0;
        updateCamera();
    }
    
    private void updateCamera() {
        // В текущей реализации FXGL не требует явного обновления камеры
        // Метод getMousePositionWorld() автоматически учитывает смещение
    }
    
    public double getCameraX() { return cameraX; }
    public double getCameraY() { return cameraY; }
    public double getZoom() { return zoom; }
}
