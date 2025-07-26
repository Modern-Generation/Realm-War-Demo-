package Structures;

import RealmWar.Player;

public abstract class Structures {

    protected int durability;
    protected int maintenanceCost;
    protected int maxLevel;
    protected int currentLevel;
    protected transient Player owner;

    public Structures(int durability, int maintenanceCost, int maxLevel) {
        this.durability = durability;
        this.maintenanceCost = maintenanceCost;
        this.maxLevel = maxLevel;
        this.currentLevel = 1;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public boolean isAlive() {
        return durability > 0;
    }

    public void takeDamage(int damage) {
        this.durability -= damage;
        if (this.durability < 0) {
            this.durability = 0;
        }
    }

    public abstract int getGoldPerTurn();

    public abstract int getFoodPerTurn();

    public abstract int getUnitSpace();

    public abstract int getBuildingCost();

    public abstract int levelUpCost();

    public abstract int getExtraUnitSpacePerTurn();

    public int getDurability() {
        return durability;
    }

    public int getMaintenanceCost() {
        return maintenanceCost;
    }

    public void levelUp() {
        if (currentLevel < maxLevel) {
            currentLevel++;
        } else {
            throw new IllegalStateException("Structure is already at max level");
        }
    }
}
