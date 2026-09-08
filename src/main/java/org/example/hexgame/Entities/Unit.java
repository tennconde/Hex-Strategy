package org.example.hexgame.Entities;

import com.almasb.fxgl.entity.Entity;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Unit {
    private int q, r; // axial координаты
    private String unitType;
    private String playerName;
    private int health;
    private int maxHealth;
    private boolean isSelected;
    private Entity entity; // Добавляем ссылку на визуальное представление

    public Unit(int q, int r, String unitType, String playerName) {
        this.q = q;
        this.r = r;
        this.unitType = unitType;
        this.playerName = playerName;
        this.isSelected = false;

        // Установка параметров в зависимости от типа юнита
        switch (unitType) {
            case "warrior":
                this.maxHealth = 100;
                this.health = 100;
                break;
            case "archer":
                this.maxHealth = 80;
                this.health = 80;
                break;
            case "knight":
                this.maxHealth = 120;
                this.health = 120;
                break;
            default:
                this.maxHealth = 50;
                this.health = 50;
                break;
        }
    }

    // Геттеры и сеттеры
    public int getQ() { return q; }
    public int getR() { return r; }
    public String getUnitType() { return unitType; }
    public String getPlayerName() { return playerName; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public boolean isSelected() { return isSelected; }
    public Entity getEntity() { return entity; } // Добавляем геттер
    public void setEntity(Entity entity) { this.entity = entity; } // Добавляем сеттер

    public void setQ(int q) { this.q = q; }
    public void setR(int r) { this.r = r; }
    public void setSelected(boolean selected) { this.isSelected = selected; }

    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(health, maxHealth));
    }
    
    // Очки движения
    private int movePoints = 3;
    private int maxMovePoints = 3;
    
    public boolean hasMovePoints(int cost) {
        return movePoints >= cost;
    }
    
    public void spendMovePoints(int cost) {
        movePoints -= cost;
    }
    
    public int getMovePoints() {
        return movePoints;
    }
    
    public void resetMovePoints() {
        movePoints = maxMovePoints;
    }

    public void takeDamage(int damage) {
        this.health = Math.max(0, this.health - damage);
    }

    public boolean isAlive() {
        return this.health > 0;
    }

    // Создание визуального представления юнита
    public Circle createVisualRepresentation() {
        Circle circle = new Circle(12); // Радиус юнита

        // Цвет в зависимости от игрока и типа
        if ("player1".equals(playerName)) {
            switch (unitType) {
                case "warrior":
                    circle.setFill(Color.RED);
                    break;
                case "archer":
                    circle.setFill(Color.DARKRED);
                    break;
                case "knight":
                    circle.setFill(Color.CRIMSON);
                    break;
                default:
                    circle.setFill(Color.RED);
                    break;
            }
        } else {
            circle.setFill(Color.BLUE); // Для других игроков
        }

        // Обводка для выделенного юнита
        circle.setStroke(Color.YELLOW);
        circle.setStrokeWidth(2);
        circle.setOpacity(0.8);

        return circle;
    }

    @Override
    public String toString() {
        return "Unit{type=" + unitType + ", player=" + playerName +
                ", pos=(" + q + "," + r + "), health=" + health + "}";
    }
}