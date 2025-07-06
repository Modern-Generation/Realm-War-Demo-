package Structures;

import RealmWar.Player;

public class Tower extends Structures {

    public Tower(Player player) {
        super(50, 5, 3);
    }

    @Override
    public int getGoldPerTurn() {
        return 0;
    }

    @Override
    public int getFoodPerTurn() {
        return 0;
    }

    @Override
    public int getUnitSpace() {
        return 0;
    }

    @Override
    public int getBuildingCost() {
        return 5;
    }

    @Override
    public int levelUpCost() {
        return 5 * currentLevel;
    }
}
