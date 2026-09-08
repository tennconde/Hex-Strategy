package org.example.hexgame.ui;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class GameUI {
    private Text yearText;
    private Text dateText;
    private Button nextTurnButton;
    private Button confirmMoveButton;
    private int currentYear = 5264;
    private int currentDay = 60;
    private Text movementCostText;
    private Runnable pendingMoveCommand = null;
    private Runnable gameCallback = null;

    private static final String[] MONTHS = {
            "Пентаварь", "Феврафан", "Мартомай", "Туямель",
            "Майоган", "Юнисон", "Юлисон", "Авгруль",
            "Сентябрин", "Октовир", "Ноябрей", "Декабран"
    };

    private static final int DAYS_PER_MONTH = 30;

    public void createUI() {
        yearText = new Text("Год: " + currentYear);
        yearText.setFill(Color.WHITE);
        yearText.setFont(Font.font(16));

        dateText = new Text("Дата: " + getDateString());
        dateText.setFill(Color.WHITE);
        dateText.setFont(Font.font(14));

        movementCostText = new Text("Стоимость перемещения: -");
        movementCostText.setFill(Color.YELLOW);
        movementCostText.setFont(Font.font(14));

        confirmMoveButton = new Button("Подтвердить перемещение");
        confirmMoveButton.setOnAction(e -> {
            if (gameCallback != null) {
                gameCallback.run();
            }
        });
        confirmMoveButton.setStyle(
                "-fx-background-color: #4a8a4a; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5px 10px;"
        );
        confirmMoveButton.setDisable(true);

        nextTurnButton = new Button("Следующий ход");
        nextTurnButton.setOnAction(e -> nextTurn());
        nextTurnButton.setStyle(
                "-fx-background-color: #4a4a4a; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5px 10px;"
        );

        VBox uiContainer = new VBox(10, yearText, dateText, movementCostText, confirmMoveButton, nextTurnButton);
        uiContainer.setAlignment(Pos.CENTER_RIGHT);

        FXGL.getGameScene().addUINode(uiContainer);
        uiContainer.setLayoutX(FXGL.getAppWidth() - 200);
        uiContainer.setLayoutY(20);
    }

    private void nextTurn() {
        if (pendingMoveCommand != null) {
            pendingMoveCommand.run();
            pendingMoveCommand = null;
            confirmMoveButton.setDisable(true);
        }

        currentDay++;
        if (currentDay > 360) {
            currentDay = 1;
            currentYear++;
        }

        yearText.setText("Год: " + currentYear);
        dateText.setText("Дата: " + getDateString());

        onTurnChanged();
    }

    private String getDateString() {
        int month = (currentDay - 1) / DAYS_PER_MONTH;
        int day = ((currentDay - 1) % DAYS_PER_MONTH) + 1;
        String monthName = (month < MONTHS.length) ? MONTHS[month] : "Неизвестный месяц";
        return day + " " + monthName;
    }

    private void onTurnChanged() {
        // Дополнительная логика при смене хода (если нужна)
    }

    public void updateMovementCost(int cost) {
        if (cost <= 0) {
            movementCostText.setText("Стоимость перемещения: -");
        } else {
            movementCostText.setText("Стоимость перемещения: " + cost + " ходов");
        }
    }

    public void setPendingMoveCommand(Runnable command) {
        this.pendingMoveCommand = command;
        if (confirmMoveButton != null) {
            confirmMoveButton.setDisable(false);
        }
    }

    public void setGameCallback(Runnable callback) {
        this.gameCallback = callback;
    }

    // Геттеры
    public int getCurrentYear() {
        return currentYear;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public String getCurrentDateString() {
        return getDateString();
    }

    public boolean isConfirmButtonEnabled() {
        return confirmMoveButton != null && !confirmMoveButton.isDisabled();
    }
}
