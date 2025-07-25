package Structures;

import RealmWar.Player;

public class TownHall extends Structures {

    // TownHall has only one level and no maintenance cost
    public TownHall(Player player) {
        super(50, 0, 1);
        this.owner = player;
    }

    @Override
    public int getGoldPerTurn() {
        return 5;
    }

    @Override
    public int getFoodPerTurn() {
        return 5;
    }

    @Override
    public int getUnitSpace() {
        return 5;
    }

    @Override
    public int getBuildingCost() {
        return 10;
    }

    @Override
    public int levelUpCost() {
        return 0;
    }
}
