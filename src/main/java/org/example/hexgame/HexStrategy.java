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

import java.util.HashMap;
import java.util.Map;

public class HexStrategy extends GameApplication {

    private Map<String, HexTile> hexTiles = new HashMap<>();
    private CameraController cameraController = new CameraController();
    private GameUI gameUI = new GameUI();
    private UnitManager unitManager;
    private TurnManager turnManager;

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
        turnManager = new TurnManager(hexTiles, unitManager);

        FXGL.runOnce(() -> {
            unitManager.spawnPlayerUnit(() -> {
                System.out.println("Юнит успешно создан!");
            });
        }, javafx.util.Duration.seconds(1));

        gameUI.createUI();
        
        // Устанавливаем callback для подтверждения перемещения
        gameUI.setGameCallback(() -> {
            if (turnManager.confirmMovement()) {
                System.out.println("Перемещение подтверждено");
            }
        });
    }

    @Override
    protected void initInput() {
        // Управление камерой оставляем
        FXGL.onKeyDown(KeyCode.S, () -> cameraController.moveCamera(0, 50));
        FXGL.onKeyDown(KeyCode.W, () -> cameraController.moveCamera(0, -50));
        FXGL.onKeyDown(KeyCode.D, () -> cameraController.moveCamera(50, 0));
        FXGL.onKeyDown(KeyCode.A, () -> cameraController.moveCamera(-50, 0));

        FXGL.onKeyDown(KeyCode.Q, () -> cameraController.zoomCamera(1.1));
        FXGL.onKeyDown(KeyCode.E, () -> cameraController.zoomCamera(0.9));
        FXGL.onKeyDown(KeyCode.R, () -> cameraController.resetCamera());

        // Обработчик клика мыши для выбора юнита и перемещения
        FXGL.onBtnDown(MouseButton.PRIMARY, this::handleMouseClick);
        
        // Обработка клавиши Escape для отмены перемещения
        FXGL.onKeyDown(KeyCode.ESCAPE, () -> {
            if (turnManager != null) {
                turnManager.cancelMovement();
            }
        });
        
        // Обработка кнопки завершения хода
        FXGL.onKeyDown(KeyCode.SPACE, () -> {
            if (turnManager != null && gameUI != null) {
                turnManager.endPlayerTurn();
            }
        });
    }
    
    /**
     * Обработка клика мыши по игровому полю
     */
    private void handleMouseClick() {
        if (turnManager == null || !turnManager.canInteract()) {
            return;
        }
        
        // Получаем позицию курсора в мировых координатах
        var mousePos = FXGL.getInput().getMousePositionWorld();
        if (mousePos == null) return;
        
        // Преобразуем в экранные координаты с учётом камеры
        var viewport = FXGL.getGameScene().getViewport();
        double screenX = mousePos.getX() * viewport.getZoom() + viewport.getX();
        double screenY = mousePos.getY() * viewport.getZoom() + viewport.getY();
        
        // Преобразуем в axial координаты гекса
        int[] axialCoords = HexGrid.pixelToAxial(screenX - FXGL.getAppWidth() / 2, 
                                                   screenY - FXGL.getAppHeight() / 2);
        int q = axialCoords[0];
        int r = axialCoords[1];
        
        System.out.println("Клик по гексу: (" + q + ", " + r + ")");
        
        // Передаём координаты в TurnManager
        turnManager.trySelectUnit(q, r);
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
