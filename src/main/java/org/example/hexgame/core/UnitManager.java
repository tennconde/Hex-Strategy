package org.example.hexgame.core;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import org.example.hexgame.entities.Unit;
import org.example.hexgame.core.HexTile;
import org.example.hexgame.ui.UnitSelectionDialog;

import java.util.*;

public class UnitManager {

    private Unit playerUnit = null;
    private boolean unitSpawned = false;
    private final Map<String, HexTile> hexTiles;
    private UnitSelectionDialog selectionDialog;
    private Entity unitEntity;
    private final List<Entity> pathEntities = new ArrayList<>(); // Для визуализации пути

    public UnitManager(Map<String, HexTile> hexTiles) {
        this.hexTiles = hexTiles;
    }
    
    /**
     * Получить игрока
     */
    public Unit getPlayerUnit() {
        return playerUnit;
    }

    // Создание игрока с выбором типа юнита
    public void spawnPlayerUnit(Runnable onUnitCreated) {
        if (unitSpawned) return;
        showUnitSelectionDialog(onUnitCreated);
    }

    private void showUnitSelectionDialog(Runnable onUnitCreated) {
        selectionDialog = new UnitSelectionDialog(() -> {
            String selectedType = selectionDialog.getSelectedUnitType();
            if (selectedType != null) {
                System.out.println("Выбран юнит: " + selectedType);
                spawnUnitAtValidPosition(selectedType, onUnitCreated);
            }
        });
        selectionDialog.show();
    }

    private void spawnUnitAtValidPosition(String unitType, Runnable onUnitCreated) {
        Random random = new Random();
        int maxAttempts = 100;
        int attempts = 0;
        int mapRadius = 10;

        while (attempts < maxAttempts) {
            int q = random.nextInt(mapRadius * 2 + 1) - mapRadius;
            int r = random.nextInt(mapRadius * 2 + 1) - mapRadius;
            int s = -q - r;

            if (Math.abs(s) <= mapRadius) {
                String key = q + "," + r;
                if (hexTiles.containsKey(key)) {
                    HexTile tile = hexTiles.get(key);
                    String terrain = tile.getTerrainType();

                    if (!"water".equals(terrain) && !"non-going-mountain".equals(terrain)) {
                        playerUnit = new Unit(q, r, unitType, "player1");
                        createUnitVisual(playerUnit);
                        unitSpawned = true;
                        System.out.println("Юнит создан в позиции: (" + q + ", " + r + ")");
                        System.out.println("Тип местности: " + terrain);

                        if (onUnitCreated != null) {
                            onUnitCreated.run();
                        }
                        return;
                    }
                }
            }
            attempts++;
        }

        // Если не удалось найти позицию, пробуем центр карты
        String centerKey = "0,0";
        if (hexTiles.containsKey(centerKey)) {
            HexTile centerTile = hexTiles.get(centerKey);
            String terrain = centerTile.getTerrainType();

            if (!"water".equals(terrain) && !"non-going-mountain".equals(terrain)) {
                playerUnit = new Unit(0, 0, unitType, "player1");
                createUnitVisual(playerUnit);
                unitSpawned = true;
                System.out.println("Юнит создан в центре: (0, 0)");

                if (onUnitCreated != null) {
                    onUnitCreated.run();
                }
            } else {
                System.out.println("Ошибка: центральная позиция непроходима!");
            }
        }
    }

    private void createUnitVisual(Unit unit) {
        System.out.println("=== Создание визуального представления юнита ===");
        System.out.println("Позиция юнита: (" + unit.getQ() + ", " + unit.getR() + ")");

        var pixelPos = HexGrid.axialToPixel(unit.getQ(), unit.getR());
        double centerX = FXGL.getAppWidth() / 2 + pixelPos.getX();
        double centerY = FXGL.getAppHeight() / 2 + pixelPos.getY();

        System.out.println("Создаем entity на позиции: (" + centerX + ", " + centerY + ")");

        unitEntity = FXGL.entityBuilder()
                .at(centerX, centerY)
                .view(unit.createVisualRepresentation())
                .with("q", unit.getQ())
                .with("r", unit.getR())
                .with("type", "unit")
                .with("unitObject", unit)
                .buildAndAttach();

        System.out.println("Entity создан: " + unitEntity);
        System.out.println("Entity позиция после создания: (" + unitEntity.getX() + ", " + unitEntity.getY() + ")");

        unit.setEntity(unitEntity);
        System.out.println("Entity установлен в объект юнита: " + unit.getEntity());
        System.out.println("=== Конец создания визуального представления ===");
    }

    // Получить стоимость перемещения для заданной клетки
    public int getMovementCostForTile(int q, int r) {
        String key = q + "," + r;
        if (!hexTiles.containsKey(key)) {
            return -1;
        }

        HexTile tile = hexTiles.get(key);
        String terrain = tile.getTerrainType();

        switch (terrain) {
            case "water":
            case "non-going-mountain":
                return -1; // Непроходимо
            case "forest":
            case "mountain":
                return 2; // 2 хода
            case "grass":
            default:
                return 1; // 1 ход
        }
    }

    // Алгоритм поиска пути A*
    public List<PathNode> findPath(int startQ, int startR, int endQ, int endR) {
        if (getMovementCostForTile(endQ, endR) == -1) {
            return new ArrayList<>(); // Цель непроходима
        }

        PriorityQueue<PathNode> openSet = new PriorityQueue<>(Comparator.comparingInt(node -> node.fCost));
        Set<String> closedSet = new HashSet<>();
        Map<String, PathNode> openSetMap = new HashMap<>();

        PathNode startNode = new PathNode(startQ, startR);
        startNode.gCost = 0;
        startNode.hCost = calculateHeuristic(startQ, startR, endQ, endR);
        startNode.calculateFCost();

        openSet.add(startNode);
        openSetMap.put(startQ + "," + startR, startNode);

        int[][] directions = {
                {1, 0}, {1, -1}, {0, -1},
                {-1, 0}, {-1, 1}, {0, 1}
        };

        while (!openSet.isEmpty()) {
            PathNode currentNode = openSet.poll();
            openSetMap.remove(currentNode.q + "," + currentNode.r);
            closedSet.add(currentNode.q + "," + currentNode.r);

            if (currentNode.q == endQ && currentNode.r == endR) {
                return reconstructPath(currentNode);
            }

            for (int[] dir : directions) {
                int neighborQ = currentNode.q + dir[0];
                int neighborR = currentNode.r + dir[1];
                String neighborKey = neighborQ + "," + neighborR;

                if (closedSet.contains(neighborKey)) {
                    continue;
                }

                int movementCost = getMovementCostForTile(neighborQ, neighborR);
                if (movementCost == -1) {
                    continue;
                }

                int tentativeGCost = currentNode.gCost + movementCost;
                PathNode neighborNode = openSetMap.get(neighborKey);

                if (neighborNode == null) {
                    neighborNode = new PathNode(neighborQ, neighborR);
                    neighborNode.hCost = calculateHeuristic(neighborQ, neighborR, endQ, endR);
                    openSetMap.put(neighborKey, neighborNode);
                } else if (tentativeGCost >= neighborNode.gCost) {
                    continue;
                }

                neighborNode.parent = currentNode;
                neighborNode.gCost = tentativeGCost;
                neighborNode.calculateFCost();

                if (!openSet.contains(neighborNode)) {
                    openSet.add(neighborNode);
                }
            }
        }

        return new ArrayList<>(); // Путь не найден
    }

    private int calculateHeuristic(int q1, int r1, int q2, int r2) {
        // Расстояние в гексагональной сетке (Manhattan-like)
        return (Math.abs(q1 - q2) + Math.abs(r1 - r2) + Math.abs(q1 + r1 - q2 - r2)) / 2;
    }

    private List<PathNode> reconstructPath(PathNode endNode) {
        List<PathNode> path = new ArrayList<>();
        PathNode currentNode = endNode;

        while (currentNode != null) {
            path.add(currentNode);
            currentNode = currentNode.parent;
        }

        Collections.reverse(path);
        return path;
    }

    // Визуализация пути
    public void visualizePath(List<PathNode> path) {
        clearPathVisualization();

        if (path.size() <= 1) return;

        for (int i = 1; i < path.size(); i++) {
            PathNode node = path.get(i);
            var pixelPos = HexGrid.axialToPixel(node.q, node.r);
            double centerX = FXGL.getAppWidth() / 2 + pixelPos.getX();
            double centerY = FXGL.getAppHeight() / 2 + pixelPos.getY();

            Entity pathEntity = FXGL.entityBuilder()
                    .at(centerX, centerY)
                    .viewWithBBox(new javafx.scene.shape.Rectangle(10, 10, javafx.scene.paint.Color.YELLOW))
                    .buildAndAttach();

            pathEntity.getViewComponent().setOpacity(0.6);
            pathEntities.add(pathEntity);
        }
    }

    public void clearPathVisualization() {
        for (Entity e : pathEntities) {
            FXGL.getGameWorld().removeEntity(e);
        }
        pathEntities.clear();
    }

    // Класс для узлов пути A*
    public static class PathNode {
        public final int q, r;
        public int gCost = Integer.MAX_VALUE;
        public int hCost = 0;
        public int fCost = Integer.MAX_VALUE;
        public PathNode parent = null;

        public PathNode(int q, int r) {
            this.q = q;
            this.r = r;
        }

        public void calculateFCost() {
            fCost = gCost + hCost;
        }
    }
}
