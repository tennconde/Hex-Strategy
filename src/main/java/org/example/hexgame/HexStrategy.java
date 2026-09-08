package org.example.hexgame;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.input.MouseButton;
import org.example.hexgame.Entities.Unit;
import org.example.hexgame.core.*;
import org.example.hexgame.ui.GameUI;
import com.almasb.fxgl.localization.Language;
import javafx.scene.input.KeyCode;
import org.example.hexgame.Entities.CameraController;
import org.example.hexgame.Entities.HexTile;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class HexStrategy extends GameApplication {

    private Map<String, HexTile> hexTiles = new HashMap<>();
    private CameraController cameraController = new CameraController();
    private GameUI gameUI = new GameUI();
    private UnitManager unitManager;
    
    // Состояние системы перемещения
    private Unit selectedUnit = null;
    private Point2D targetPosition = null;
    private List<UnitManager.PathNode> currentPath = null;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Hex Strategy");
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setDefaultLanguage(Language.RUSSIAN);
        settings.setFullScreenAllowed(true);
    }

    @Override
    protected void initGame() {
        FXGL.getGameScene().setBackgroundColor(Color.rgb(40, 40, 40));
        createHexGrid();

        unitManager = new UnitManager(hexTiles);

        FXGL.runOnce(() -> {
            unitManager.spawnPlayerUnit(() -> {
                System.out.println("Юнит успешно создан!");
            });
        }, javafx.util.Duration.seconds(1));

        gameUI.createUI();
    }

    @Override
    protected void initInput() {
        // Управление камерой
        FXGL.onKeyDown(KeyCode.S, () -> cameraController.moveCamera(0, 50));
        FXGL.onKeyDown(KeyCode.W, () -> cameraController.moveCamera(0, -50));
        FXGL.onKeyDown(KeyCode.D, () -> cameraController.moveCamera(50, 0));
        FXGL.onKeyDown(KeyCode.A, () -> cameraController.moveCamera(-50, 0));

        FXGL.onKeyDown(KeyCode.Q, () -> cameraController.zoomCamera(1.1));
        FXGL.onKeyDown(KeyCode.E, () -> cameraController.zoomCamera(0.9));
        FXGL.onKeyDown(KeyCode.R, () -> cameraController.resetCamera());

        // Обработка клика мыши для выбора юнита и перемещения
        FXGL.onBtnDown(MouseButton.PRIMARY, this::handleMouseClick);
    }
    
    private void handleMouseClick() {
        Point2D mousePos = FXGL.getInput().getMousePositionWorld();
        
        // Преобразуем экранные координаты в гексагональные
        double centerX = FXGL.getAppWidth() / 2;
        double centerY = FXGL.getAppHeight() / 2;
        
        // Учитываем смещение камеры
        Point2D cameraOffset = cameraController.getCameraOffset();
        double worldX = (mousePos.getX() - centerX) / cameraController.getZoom();
        double worldY = (mousePos.getY() - centerY) / cameraController.getZoom();
        
        int[] hexCoords = HexGrid.pixelToAxial(worldX, worldY);
        int q = hexCoords[0];
        int r = hexCoords[1];
        
        String key = q + "," + r;
        System.out.println("Клик по гексу: (" + q + ", " + r + ")");
        
        // Проверяем, есть ли тайл в этой позиции
        if (!hexTiles.containsKey(key)) {
            System.out.println("Тайл не найден для ключа: " + key);
            return;
        }
        
        HexTile clickedTile = hexTiles.get(key);
        
        // Проверяем, есть ли юнит в этой позиции
        Unit clickedUnit = getUnitAtPosition(q, r);
        
        if (clickedUnit != null && "player1".equals(clickedUnit.getPlayerName())) {
            // Кликнули по своему юниту - выбираем его
            selectUnit(clickedUnit);
        } else if (selectedUnit != null) {
            // Кликнули по пустой клетке - пытаемся переместить выбранного юнита
            attemptMove(selectedUnit, q, r);
        }
    }
    
    private Unit getUnitAtPosition(int q, int r) {
        // Получаем юнита из UnitManager
        Unit playerUnit = unitManager.getPlayerUnit();
        if (playerUnit != null && playerUnit.getQ() == q && playerUnit.getR() == r) {
            return playerUnit;
        }
        return null;
    }
    
    private void selectUnit(Unit unit) {
        // Снимаем выделение с предыдущего юнита
        if (selectedUnit != null) {
            selectedUnit.setSelected(false);
            updateUnitVisual(selectedUnit);
        }
        
        // Выделяем новый юнит
        selectedUnit = unit;
        selectedUnit.setSelected(true);
        updateUnitVisual(selectedUnit);
        
        System.out.println("Юнит выбран: " + selectedUnit.getUnitType() + " на позиции (" + 
                          selectedUnit.getQ() + ", " + selectedUnit.getR() + ")");
        
        // Очищаем предыдущий путь
        if (currentPath != null) {
            unitManager.clearPathVisualization();
            currentPath = null;
        }
    }
    
    private void updateUnitVisual(Unit unit) {
        if (unit.getEntity() == null) {
            System.out.println("updateUnitVisual: entity is null");
            return;
        }
        
        var viewComponent = unit.getEntity().getViewComponent();
        if (viewComponent == null || viewComponent.getChildren().isEmpty()) {
            System.out.println("updateUnitVisual: viewComponent или дети null/empty");
            return;
        }
        
        var node = viewComponent.getChildren().get(0);
        if (node instanceof javafx.scene.shape.Circle) {
            javafx.scene.shape.Circle circle = (javafx.scene.shape.Circle) node;
            
            if (unit.isSelected()) {
                circle.setStroke(Color.WHITE);
                circle.setStrokeWidth(3);
                System.out.println("updateUnitVisual: WHITE selection applied");
            } else {
                circle.setStroke(Color.YELLOW);
                circle.setStrokeWidth(2);
                System.out.println("updateUnitVisual: YELLOW deselection applied");
            }
        } else {
            System.out.println("updateUnitVisual: node is not Circle, it's " + 
                              (node != null ? node.getClass().getSimpleName() : "null"));
        }
    }
    
    private void attemptMove(Unit unit, int targetQ, int targetR) {
        // Находим путь
        List<UnitManager.PathNode> path = unitManager.findPath(
            unit.getQ(), unit.getR(), targetQ, targetR);
        
        if (path == null || path.size() <= 1) {
            System.out.println("Путь не найден или цель совпадает с текущей позицией");
            return;
        }
        
        // Проверяем, хватает ли очков движения
        int totalCost = 0;
        for (int i = 1; i < path.size(); i++) {
            int cost = unitManager.getMovementCostForTile(path.get(i).q, path.get(i).r);
            if (cost == -1) {
                System.out.println("Непроходимая клетка на пути");
                return;
            }
            totalCost += cost;
        }
        
        if (!unit.hasMovePoints(totalCost)) {
            System.out.println("Недостаточно очков движения: нужно " + totalCost + 
                             ", доступно " + unit.getMovePoints());
            return;
        }
        
        // Сохраняем путь и визуализируем его
        currentPath = path;
        unitManager.visualizePath(path);
        
        // Запускаем перемещение
        moveUnitAlongPath(unit, path, totalCost);
    }
    
    private void moveUnitAlongPath(Unit unit, List<UnitManager.PathNode> path, int totalCost) {
        // Spending movement points
        unit.spendMovePoints(totalCost);
        
        // Перемещаем юнита по пути с анимацией
        for (int i = 1; i < path.size(); i++) {
            final int stepIndex = i;
            UnitManager.PathNode node = path.get(i);
            
            var pixelPos = HexGrid.axialToPixel(node.q, node.r);
            double centerX = FXGL.getAppWidth() / 2 + pixelPos.getX();
            double centerY = FXGL.getAppHeight() / 2 + pixelPos.getY();
            
            FXGL.run(() -> {
                unit.getEntity().setPosition(centerX, centerY);
                unit.setQ(node.q);
                unit.setR(node.r);
                
                // Если это последний шаг, обновляем состояние
                if (stepIndex == path.size() - 1) {
                    System.out.println("Перемещение завершено на позицию (" + node.q + ", " + node.r + ")");
                    unitManager.clearPathVisualization();
                    currentPath = null;
                    
                    // Снимаем выделение после перемещения
                    unit.setSelected(false);
                    updateUnitVisual(unit);
                    selectedUnit = null;
                }
            }, javafx.util.Duration.millis((long)(stepIndex * 300)));
        }
    }

    private void createHexGrid() {
        int mapRadius = 12;

        for (int q = -mapRadius; q <= mapRadius; q++) {
            for (int r = -mapRadius; r <= mapRadius; r++) {
                int s = -q - r;
                if (Math.abs(s) <= mapRadius) {
                    createHexTile(q, r);
                }
            }
        }
    }

    private void createHexTile(int q, int r) {
        String terrainType = TerrainGenerator.generateTerrainRealistic(q, r);
        var pixelPos = HexGrid.axialToPixel(q, r);

        double centerX = FXGL.getAppWidth() / 2 + pixelPos.getX();
        double centerY = FXGL.getAppHeight() / 2 + pixelPos.getY();

        HexTile tile = new HexTile(q, r, terrainType);
        tile.createHexTile(centerX, centerY);
        hexTiles.put(q + "," + r, tile);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
