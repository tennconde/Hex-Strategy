package org.example.hexgame.core;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Point2D;
import javafx.util.Duration;
import org.example.hexgame.Entities.HexTile;
import org.example.hexgame.Entities.Unit;

import java.util.List;
import java.util.Map;

/**
 * Менеджер системы ходов и перемещения юнитов
 */
public class TurnManager {
    
    private Unit selectedUnit = null;
    private List<UnitManager.PathNode> currentPath = null;
    private boolean isMoving = false;
    private boolean isPlayerTurn = true;
    
    private final Map<String, HexTile> hexTiles;
    private final UnitManager unitManager;
    
    // Очки движения для разных типов юнитов
    private static final Map<String, Integer> UNIT_MOVEMENT_POINTS = Map.of(
        "warrior", 6,
        "archer", 7,
        "knight", 8
    );
    
    public TurnManager(Map<String, HexTile> hexTiles, UnitManager unitManager) {
        this.hexTiles = hexTiles;
        this.unitManager = unitManager;
    }
    
    /**
     * Выбор юнита при клике
     */
    public void trySelectUnit(int q, int r) {
        if (!isPlayerTurn || isMoving) return;
        
        // Проверяем, есть ли юнит в этой позиции
        Unit unit = findUnitAtPosition(q, r);
        
        if (unit != null && "player1".equals(unit.getPlayerName())) {
            selectUnit(unit);
        } else if (selectedUnit != null) {
            // Если кликнули на пустую клетку и юнит выбран - пытаемся переместить
            tryMoveTo(q, r);
        }
    }
    
    /**
     * Выбор юнита
     */
    private void selectUnit(Unit unit) {
        // Снимаем выделение с предыдущего юнита
        if (selectedUnit != null) {
            selectedUnit.setSelected(false);
            updateUnitVisual(selectedUnit);
        }
        
        selectedUnit = unit;
        selectedUnit.setSelected(true);
        updateUnitVisual(selectedUnit);
        
        System.out.println("Выбран юнит: " + unit);
    }
    
    /**
     * Попытка построения пути к целевой клетке
     */
    public void tryMoveTo(int targetQ, int targetR) {
        if (selectedUnit == null || isMoving) return;
        
        // Проверяем проходимость цели
        int movementCost = unitManager.getMovementCostForTile(targetQ, targetR);
        if (movementCost == -1) {
            System.out.println("Цель непроходима!");
            return;
        }
        
        // Находим путь
        List<UnitManager.PathNode> path = unitManager.findPath(
            selectedUnit.getQ(), 
            selectedUnit.getR(), 
            targetQ, 
            targetR
        );
        
        if (path.isEmpty() || path.size() <= 1) {
            System.out.println("Путь не найден или юнит уже в цели");
            return;
        }
        
        // Проверяем, хватает ли очков движения
        int totalCost = calculatePathCost(path);
        int maxMovement = getUnitMaxMovement(selectedUnit);
        
        if (totalCost > maxMovement) {
            System.out.println("Недостаточно очков движения! Нужно: " + totalCost + 
                             ", доступно: " + maxMovement);
            return;
        }
        
        // Сохраняем путь и визуализируем его
        currentPath = path;
        unitManager.visualizePath(path);
        
        System.out.println("Путь построен! Длина: " + path.size() + 
                         ", стоимость: " + totalCost);
    }
    
    /**
     * Подтверждение и выполнение перемещения
     */
    public boolean confirmMovement() {
        if (selectedUnit == null || currentPath == null || currentPath.isEmpty()) {
            return false;
        }
        
        // Начинаем анимацию перемещения
        executeMovementAlongPath(currentPath);
        return true;
    }
    
    /**
     * Отмена текущего перемещения
     */
    public void cancelMovement() {
        currentPath = null;
        unitManager.clearPathVisualization();
        System.out.println("Перемещение отменено");
    }
    
    /**
     * Выполнение перемещения по пути с анимацией
     */
    private void executeMovementAlongPath(List<UnitManager.PathNode> path) {
        if (path.size() <= 1) return;
        
        isMoving = true;
        unitManager.clearPathVisualization();
        
        // Перемещаем юнита по каждой точке пути с задержкой
        for (int i = 1; i < path.size(); i++) {
            final int index = i;
            UnitManager.PathNode node = path.get(i);
            
            FXGL.runOnce(() -> {
                moveUnitToNode(selectedUnit, node);
                
                // Если это последняя точка пути
                if (index == path.size() - 1) {
                    finishMovement();
                }
            }, javafx.util.Duration.seconds(0.3 * index));
        }
    }
    
    /**
     * Перемещение юнита в указанную узловую точку
     */
    private void moveUnitToNode(Unit unit, UnitManager.PathNode node) {
        // Обновляем координаты юнита
        unit.setQ(node.q);
        unit.setR(node.r);
        
        // Получаем новые пиксельные координаты
        Point2D pixelPos = HexGrid.axialToPixel(node.q, node.r);
        double centerX = FXGL.getAppWidth() / 2 + pixelPos.getX();
        double centerY = FXGL.getAppHeight() / 2 + pixelPos.getY();
        
        // Перемещаем визуальное представление
        if (unit.getEntity() != null) {
            unit.getEntity().setPosition(centerX, centerY);
        }
        
        System.out.println("Юнит перемещён в: (" + node.q + ", " + node.r + ")");
    }
    
    /**
     * Завершение перемещения
     */
    private void finishMovement() {
        isMoving = false;
        currentPath = null;
        
        // Снимаем выделение с юнита после перемещения
        if (selectedUnit != null) {
            selectedUnit.setSelected(false);
            updateUnitVisual(selectedUnit);
            selectedUnit = null;
        }
        
        System.out.println("Перемещение завершено");
    }
    
    /**
     * Расчёт общей стоимости пути
     */
    private int calculatePathCost(List<UnitManager.PathNode> path) {
        if (path.size() <= 1) return 0;
        
        int totalCost = 0;
        for (int i = 1; i < path.size(); i++) {
            UnitManager.PathNode node = path.get(i);
            int cost = unitManager.getMovementCostForTile(node.q, node.r);
            if (cost > 0) {
                totalCost += cost;
            }
        }
        return totalCost;
    }
    
    /**
     * Получение максимальных очков движения для юнита
     */
    private int getUnitMaxMovement(Unit unit) {
        return UNIT_MOVEMENT_POINTS.getOrDefault(unit.getUnitType(), 6);
    }
    
    /**
     * Поиск юнита в указанной позиции
     */
    private Unit findUnitAtPosition(int q, int r) {
        // В текущей реализации у нас только один юнит игрока
        // В будущем можно расширить для поиска среди всех юнитов
        if (selectedUnit != null && 
            selectedUnit.getQ() == q && 
            selectedUnit.getR() == r) {
            return selectedUnit;
        }
        return null; // Пока заглушка, нужно будет расширить при добавлении списка юнитов
    }
    
    /**
     * Обновление визуального представления юнита (цвета выделения)
     */
    private void updateUnitVisual(Unit unit) {
        if (unit.getEntity() == null || unit.getEntity().getViewComponent() == null) {
            return;
        }
        
        var view = unit.getEntity().getViewComponent();
        if (view.getChildren().isEmpty()) return;
        
        var node = view.getChildren().get(0);
        if (node instanceof javafx.scene.shape.Circle circle) {
            if (unit.isSelected()) {
                circle.setStroke(javafx.scene.paint.Color.WHITE);
                circle.setStrokeWidth(4);
            } else {
                circle.setStroke(javafx.scene.paint.Color.YELLOW);
                circle.setStrokeWidth(2);
            }
        }
    }
    
    /**
     * Завершение хода игрока
     */
    public void endPlayerTurn() {
        if (isMoving) {
            System.out.println("Нельзя завершить ход во время перемещения!");
            return;
        }
        
        // Отменяем любое незавершённое перемещение
        cancelMovement();
        
        // Снимаем выделение
        if (selectedUnit != null) {
            selectedUnit.setSelected(false);
            updateUnitVisual(selectedUnit);
            selectedUnit = null;
        }
        
        isPlayerTurn = false;
        System.out.println("Ход игрока завершён");
        
        // Здесь можно запустить ход ИИ
        startAITurn();
    }
    
    /**
     * Начало хода ИИ (заглушка для будущей реализации)
     */
    private void startAITurn() {
        System.out.println("Ход ИИ...");
        
        // Имитация хода ИИ
        FXGL.runOnce(() -> {
            isPlayerTurn = true;
            System.out.println("Ход игрока начался");
        }, javafx.util.Duration.seconds(2));
    }
    
    /**
     * Проверка, можно ли взаимодействовать с юнитами
     */
    public boolean canInteract() {
        return isPlayerTurn && !isMoving;
    }
    
    /**
     * Получить выбранный юнит
     */
    public Unit getSelectedUnit() {
        return selectedUnit;
    }
    
    /**
     * Получить текущий путь (для отладки)
     */
    public List<UnitManager.PathNode> getCurrentPath() {
        return currentPath;
    }
}
