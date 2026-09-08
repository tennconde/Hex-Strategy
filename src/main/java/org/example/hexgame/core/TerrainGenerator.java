package org.example.hexgame.core;

import java.util.Random;

public class TerrainGenerator {
    private static long seedBase = System.currentTimeMillis();

    public static String generateTerrainRealistic(int q, int r) {
        // Используем координаты + базовый seed
        long seed = (long) q * 123 + (long) r * 56 + seedBase;
        Random localRandom = new Random(seed);

        // Более разнообразная генерация
        double noise1 = localRandom.nextDouble(); // от 0 до 1
        double noise2 = localRandom.nextDouble(); // дополнительная случайность
        double distanceFromCenter = Math.sqrt(q * q + r * r) / 17.0; // Уменьшил делитель для большего влияния

        // Комбинируем несколько факторов
        double terrainFactor = (noise1 * 0.7) + (noise2 * 0.3) + (distanceFromCenter * 0.4);

        // Расширяем диапазон значений для большего разнообразия
        if (terrainFactor > 0.99) {
            return "non-going-mountain";
        } else if (terrainFactor >0.9) {
            return "mountain";
        } else if (terrainFactor > 0.7) {
            return "forest";
        }else if (terrainFactor > 0.5) {
            return "water";
        } else if (terrainFactor > 0.45) {
            return "grass";
        } else {
            // Добавляем немного вариативности в низкие значения
            double extra = localRandom.nextDouble();
            if (extra > 0.9) {
                return "mountain";
            } else if (extra > 0.4) {
                return "forest";
            } else {
                return "grass";
            }
        }
    }

    public static void resetSeed() {
        seedBase = System.currentTimeMillis();
    }
}