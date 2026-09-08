package org.example.hexgame.entities;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;

/**
 * Класс юнита - представляет бойца на карте
 */
public class Unit {
    private int q, r;
    private String unitType;
    private String playerName;
    private Entity entity;
    private boolean isSelected;
    
    public Unit(int q, int r, String unitType, String playerName) {
        this.q = q;
        this.r = r;
        this.unitType = unitType;
        this.playerName = playerName;
        this.isSelected = false;
    }
    
    public int getQ() { return q; }
    public int getR() { return r; }
    public void setQ(int q) { this.q = q; }
    public void setR(int r) { this.r = r; }
    public String getUnitType() { return unitType; }
    public String getPlayerName() { return playerName; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
    public Entity getEntity() { return entity; }
    public void setEntity(Entity entity) { this.entity = entity; }
    
    /**
     * Создание визуального представления юнита
     */
    public Polygon createVisualRepresentation() {
        // Создаем шестиугольник для юнита (меньше, чем тайл)
        Polygon hexagon = new Polygon();
        double radius = 20; // Радиус юнита
        
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i);
            double x = radius * Math.cos(angle);
            double y = radius * Math.sin(angle);
            hexagon.getPoints().addAll(x, y);
        }
        
        // Цвет зависит от типа юнита и игрока
        Color fillColor = switch (unitType.toLowerCase()) {
            case "warrior" -> Color.RED;
            case "archer" -> Color.GREEN;
            case "knight" -> Color.BLUE;
            default -> Color.GRAY;
        };
        
        hexagon.setFill(fillColor);
        hexagon.setStroke(Color.YELLOW);
        hexagon.setStrokeWidth(2);
        
        return hexagon;
    }
    
    @Override
    public String toString() {
        return "Unit{" +
                "type='" + unitType + '\'' +
                ", player='" + playerName + '\'' +
                ", pos=(" + q + "," + r + ")" +
                '}';
    }
}
