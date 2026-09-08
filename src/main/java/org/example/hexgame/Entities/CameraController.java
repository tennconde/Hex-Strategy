package org.example.hexgame.Entities;

import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.input.KeyCode;

public class CameraController {
    
    private static final double MOVE_SPEED = 200.0;
    private static final double MIN_ZOOM = 0.3;
    private static final double MAX_ZOOM = 5.0;
    
    public void moveCamera(double deltaX, double deltaY) {
        var viewport = FXGL.getGameScene().getViewport();
        viewport.setX(viewport.getX() + deltaX);
        viewport.setY(viewport.getY() + deltaY);
    }
    
    public void zoomCamera(double factor) {
        var viewport = FXGL.getGameScene().getViewport();
        double oldZoom = viewport.getZoom();
        double newZoom = oldZoom * factor;
        newZoom = Math.max(MIN_ZOOM, Math.min(newZoom, MAX_ZOOM));

        if (newZoom != oldZoom) {
            // Получаем текущий центр камеры в мировых координатах
            double centerX = FXGL.getAppWidth() / 2;
            double centerY = FXGL.getAppHeight() / 2;
            double worldCenterX = viewport.getX() + centerX / oldZoom;
            double worldCenterY = viewport.getY() + centerY / oldZoom;

            // Устанавливаем новый зум
            viewport.setZoom(newZoom);

            // Корректируем позицию камеры, чтобы центр оставался на месте
            viewport.setX(worldCenterX - centerX / newZoom);
            viewport.setY(worldCenterY - centerY / newZoom);
        }
    }
    
    public void resetCamera() {
        var viewport = FXGL.getGameScene().getViewport();
        viewport.setZoom(1.0);
        viewport.setX(0);
        viewport.setY(0);
    }
    
    // Метод для плавного движения (будет вызываться в onUpdate)
    public void update(double tpf) {
        double speed = MOVE_SPEED * tpf;
        
        // Здесь можно добавить плавное движение, если найдем правильные методы
        // Пока оставим для одиночных нажатий
    }
}