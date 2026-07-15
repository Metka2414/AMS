package de.allinone.ams.data;

import java.util.UUID;

public class AmsAccount {

    public static final int MAX_LEVEL = 100;

    // Basiswert: wie viel Geld 1 AMS pro Zyklus bringt (bei Level 0)
    public static final double BASE_MONEY_PER_AMS = 10.0;

    private final UUID owner;

    private int amsCount = 0;
    private int moneyLevel = 0;   // 0-100, +2% pro Level
    private int timeLevel = 0;    // 0-100, Start 120s -> 5s

    private double storedMoney = 0;
    private long lastTick = System.currentTimeMillis();

    public AmsAccount(UUID owner) {
        this.owner = owner;
    }

    public UUID getOwner() {
        return owner;
    }

    public int getAmsCount() {
        return amsCount;
    }

    public void addAms(int amount) {
        this.amsCount += amount;
    }

    public void setAmsCount(int amsCount) {
        this.amsCount = amsCount;
    }

    public int getMoneyLevel() {
        return moneyLevel;
    }

    public void setMoneyLevel(int moneyLevel) {
        this.moneyLevel = Math.max(0, Math.min(moneyLevel, MAX_LEVEL));
    }

    public int getTimeLevel() {
        return timeLevel;
    }

    public void setTimeLevel(int timeLevel) {
        this.timeLevel = Math.max(0, Math.min(timeLevel, MAX_LEVEL));
    }

    public double getStoredMoney() {
        return storedMoney;
    }

    public void addStoredMoney(double amount) {
        this.storedMoney += amount;
    }

    public void clearStoredMoney() {
        this.storedMoney = 0;
    }

    public long getLastTick() {
        return lastTick;
    }

    public void setLastTick(long lastTick) {
        this.lastTick = lastTick;
    }

    /**
     * Intervall in Sekunden. Level 0 = 120s, Level 100 = 5s (linear).
     */
    public int getIntervalSeconds() {
        double progress = timeLevel / (double) MAX_LEVEL;
        return (int) Math.round(120 - (115 * progress));
    }

    /**
     * Geld pro Zyklus = Basiswert * Anzahl AMS * (1 + 2% je Money-Level).
     */
    public double getMoneyPerCycle() {
        double multiplier = 1.0 + (moneyLevel * 0.02);
        return BASE_MONEY_PER_AMS * amsCount * multiplier;
    }
}
