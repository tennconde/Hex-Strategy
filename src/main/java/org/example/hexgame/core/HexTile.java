package org.example.hexgame.core;

import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * Класс гексагонального тайла местности
 */
public class HexTile {
    private int q, r;
    private String terrainType;
    private Polygon hexPolygon;
    
    public HexTile(int q, int r, String terrainType) {
        this.q = q;
        this.r = r;
        this.terrainType = terrainType;
    }
    
    public int getQ() { return q; }
    public int getR() { return r; }
    public String getTerrainType() { return terrainType; }
    
    /**
     * Создание визуального представления тайла
     */
    public void createHexTile(double centerX, double centerY) {
        double size = 40; // Размер гекса
        Polygon hexagon = new Polygon();
        
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i);
            double x = centerX + size * Math.cos(angle);
            double y = centerY + size * Math.sin(angle);
            hexagon.getPoints().addAll(x, y);
        }
        
        // Цвет в зависимости от типа местности
        Color fillColor = switch (terrainType.toLowerCase()) {
            case "water" -> Color.BLUE;
            case "forest" -> Color.GREEN;
            case "mountain" -> Color.GRAY;
            case "non-going-mountain" -> Color.DARKGRAY;
            default -> Color.LIGHTGREEN; // grass
        };
        
        hexagon.setFill(fillColor);
        hexagon.setStroke(Color.BLACK);
        hexagon.setStrokeWidth(1);
        
        FXGL.getGameScene().addUINode(hexagon);
        this.hexPolygon = hexagon;
    }
    
    @Override
    public String toString() {
        return "HexTile{" +
                "pos=(" + q + "," + r + ")" +
                ", terrain='" + terrainType + '\'' +
                '}';
    }
}
