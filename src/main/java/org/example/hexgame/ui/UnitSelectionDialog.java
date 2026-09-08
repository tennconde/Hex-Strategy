package org.example.hexgame.ui;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.ArrayList;
import java.util.List;

public class UnitSelectionDialog {
    private Runnable onUnitSelected;
    private String selectedUnitType;
    private List<Object> dialogElements; // Сохраняем ссылки на элементы диалога

    public UnitSelectionDialog(Runnable onUnitSelected) {
        this.onUnitSelected = onUnitSelected;
        this.dialogElements = new ArrayList<>();
    }

    public void show() {
        // Создаем затемнение фона
        Rectangle background = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight());
        background.setFill(Color.rgb(0, 0, 0, 0.7));

        // Создаем заголовок
        Label titleLabel = new Label("Выберите тип юнита");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // Создаем кнопки для каждого типа юнита
        Button warriorButton = createUnitButton("Воин", "warrior", Color.RED);
        Button archerButton = createUnitButton("Лучник", "archer", Color.DARKRED);
        Button knightButton = createUnitButton("Рыцарь", "knight", Color.CRIMSON);

        // Создаем контейнер для кнопок
        VBox buttonContainer = new VBox(15);
        buttonContainer.getChildren().addAll(warriorButton, archerButton, knightButton);
        buttonContainer.setAlignment(Pos.CENTER);

        // Создаем основной контейнер
        VBox mainContainer = new VBox(30);
        mainContainer.getChildren().addAll(titleLabel, buttonContainer);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPrefSize(400, 300);

        // Сохраняем ссылки на элементы
        dialogElements.add(background);
        dialogElements.add(mainContainer);

        // Добавляем элементы в сцену
        FXGL.getGameScene().addUINode(background);
        FXGL.getGameScene().addUINode(mainContainer);

        // Центрируем диалог
        mainContainer.setLayoutX((FXGL.getAppWidth() - mainContainer.getPrefWidth()) / 2);
        mainContainer.setLayoutY((FXGL.getAppHeight() - mainContainer.getPrefHeight()) / 2);
    }

    private Button createUnitButton(String displayName, String unitType, Color color) {
        Button button = new Button(displayName);
        button.setPrefSize(200, 50);
        button.setStyle(
                "-fx-background-color: " + toRGBCode(color) + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-border-color: white; " +
                        "-fx-border-width: 2px; " +
                        "-fx-background-radius: 10px; " +
                        "-fx-border-radius: 10px;"
        );

        button.setOnAction(e -> {
            selectedUnitType = unitType;
            // Убираем диалог безопасным способом
            closeDialog();
            // Вызываем callback
            if (onUnitSelected != null) {
                onUnitSelected.run();
            }
        });

        return button;
    }

    private void closeDialog() {
        // Удаляем элементы по одному
        for (Object element : dialogElements) {
            if (element instanceof javafx.scene.Node) {
                FXGL.getGameScene().removeUINode((javafx.scene.Node) element);
            }
        }
        dialogElements.clear();
    }

    // Вспомогательный метод для конвертации Color в CSS формат
    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    public String getSelectedUnitType() {
        return selectedUnitType;
    }
}