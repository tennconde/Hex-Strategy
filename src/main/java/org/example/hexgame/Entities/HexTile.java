package org.example.hexgame.Entities;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import javafx.scene.shape.Polygon;
import org.example.hexgame.core.HexGrid;

public class HexTile {
    private final int q; // axial координаты
    private final int r;
    private String terrainType; // тип местности
    private boolean isOccupied;
    private Entity hexEntity; // визуальное представление тайла

    public HexTile(int q, int r) {
        this.q = q;
        this.r = r;
        this.terrainType = "grass";
        this.isOccupied = false;
    }

    public HexTile(int q, int r, String terrainType) {
        this.q = q;
        this.r = r;
        this.terrainType = terrainType;
        this.isOccupied = false;
    }

    /**
     * Создает визуальное представление гекса и добавляет его в сцену.
     * @param centerX координата X центра гекса на экране
     * @param centerY координата Y центра гекса на экране
     */
    public void createHexTile(double centerX, double centerY) {
        Polygon hexPolygon = HexGrid.createHex(0, 0);

        switch (terrainType) {
            case "mountain":
                hexPolygon.setFill(javafx.scene.paint.Color.rgb(120, 120, 120)); // серый
                hexPolygon.setStroke(javafx.scene.paint.Color.rgb(80, 80, 80));
                break;
            case "non-going-mountain":
                hexPolygon.setFill(javafx.scene.paint.Color.rgb(62, 60, 50)); // тёмно-серый
                hexPolygon.setStroke(javafx.scene.paint.Color.rgb(80, 80, 80));
                break;
            case "water":
                hexPolygon.setFill(javafx.scene.paint.Color.rgb(65, 105, 225)); // синий
                hexPolygon.setStroke(javafx.scene.paint.Color.rgb(30, 70, 180));
                break;
            case "forest":
                hexPolygon.setFill(javafx.scene.paint.Color.rgb(34, 139, 34)); // тёмно-зелёный
                hexPolygon.setStroke(javafx.scene.paint.Color.rgb(20, 100, 20));
                break;
            case "grass":
            default:
                hexPolygon.setFill(javafx.scene.paint.Color.rgb(60, 120, 60)); // зелёный
                hexPolygon.setStroke(javafx.scene.paint.Color.rgb(40, 80, 40));
                break;
        }

        hexPolygon.setStrokeWidth(2);
        hexPolygon.setRotate(90);

        hexEntity = FXGL.entityBuilder()
                .at(centerX, centerY)
                .view(hexPolygon)
                .with("q", q)
                .with("r", r)
                .with("type", "hex")
                .with("terrain", terrainType)
                .buildAndAttach();
    }

    // Геттеры
    public int getQ() { return q; }
    public int getR() { return r; }
    public String getTerrainType() { return terrainType; }
    public boolean isOccupied() { return isOccupied; }
    public Entity getHexEntity() { return hexEntity; }

    // Сеттеры
    public void setOccupied(boolean occupied) {
        this.isOccupied = occupied;
    }

    public void setTerrainType(String terrainType) {
        this.terrainType = terrainType;
    }

    @Override
    public String toString() {
        return "HexTile{q=" + q + ", r=" + r + ", type=" + terrainType + "}";
    }
}
