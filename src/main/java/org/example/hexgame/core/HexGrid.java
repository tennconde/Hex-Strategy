package org.example.hexgame.core;

import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;

public class HexGrid {
    public static final int HEX_SIZE = 25; // размер гекса
    public static final double WIDTH = HEX_SIZE * 2;
    public static final double HEIGHT = Math.sqrt(3) * HEX_SIZE;



    // Axial координаты (q, r) в пиксельные координаты
    public static Point2D axialToPixel(int q, int r) {
        double x = HEX_SIZE * Math.sqrt(3) * (q + r / 2.0);
        double y = HEX_SIZE * 3.0 / 2.0 * r;
        return new Point2D(x, y);
    }

    // Создание гекса как полигона
    public static Polygon createHex(double centerX, double centerY) {
        Polygon hex = new Polygon();
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i);
            double x = centerX + HEX_SIZE * Math.cos(angle);
            double y = centerY + HEX_SIZE * Math.sin(angle);
            hex.getPoints().addAll(x, y);
        }
        return hex;
    }


    // Округление кубических координат до правильных
    public static int[] cubeRound(double x, double y, double z) {
        int rx = (int) Math.round(x);
        int ry = (int) Math.round(y);
        int rz = (int) Math.round(z);

        int dx = (int) Math.abs(rx - x);
        int dy = (int) Math.abs(ry - y);
        int dz = (int) Math.abs(rz - z);

        if (dx > dy && dx > dz) {
            rx = -ry - rz;
        } else if (dy > dz) {
            ry = -rx - rz;
        } else {
            rz = -rx - ry;
        }

        return new int[]{rx, ry, rz};
    }

    // Пиксельные координаты в axial
    public static int[] pixelToAxial(double x, double y) {
        double q = (Math.sqrt(3)/3 * x - 1./3 * y) / HEX_SIZE;
        double r = (2./3 * y) / HEX_SIZE;

        int[] cube = cubeRound(q, -q - r, r);
        return new int[]{cube[0], cube[2]}; // q, r
    }
}