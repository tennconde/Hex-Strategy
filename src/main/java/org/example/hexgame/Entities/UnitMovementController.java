package org.example.hexgame.Entities;

import java.util.Map;

public class UnitMovementController {

    private final Unit unit;
    private final Map<String, Integer> terrainMovementCost; // Стоимость перемещения по типу местности
    private HexTile currentTile;

    public UnitMovementController(Unit unit, HexTile startingTile) {
        this.unit = unit;
        this.currentTile = startingTile;

        // Пример стоимости движения, можно расширить
        terrainMovementCost = Map.of(
                "grass", 1,
                "forest", 2,
                "mountain", 3,
                "non-going-mountain", Integer.MAX_VALUE, // непроходимая
                "water", Integer.MAX_VALUE // непроходимая (или можно разрешить, если плавание)
        );
    }

    /**
     * Проверяет, можно ли юниту перейти на указанный тайл.
     */
    public boolean canMoveTo(HexTile targetTile) {
        if (targetTile == null) return false;
        if (targetTile.isOccupied()) return false;

        int cost = getMovementCost(targetTile);
        return cost != Integer.MAX_VALUE;
    }

    /**
     * Возвращает стоимость перемещения на тайл.
     */
    public int getMovementCost(HexTile tile) {
        return terrainMovementCost.getOrDefault(tile.getTerrainType(), 1);
    }

    /**
     * Пытается передвинуть юнита на новый тайл.
     * Возвращает true, если движение успешно.
     */
    public boolean moveTo(HexTile targetTile) {
        if (!canMoveTo(targetTile)) return false;

        // Обновляем занятость тайлов
        currentTile.setOccupied(false);
        targetTile.setOccupied(true);

        // Обновляем позицию юнита
        unit.setQ(targetTile.getQ());
        unit.setR(targetTile.getR());

        // Сохраняем текущий тайл
        currentTile = targetTile;

        // Можно добавить логику уменьшения очков движения, здоровья и т.п.

        return true;
    }

    public HexTile getCurrentTile() {
        return currentTile;
    }
}
