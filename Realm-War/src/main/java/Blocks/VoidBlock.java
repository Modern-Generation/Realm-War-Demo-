package Blocks;

import Grid.Position;
import RealmWar.Player;

public class VoidBlock extends Blocks {

    public VoidBlock(Position position) {
        super(position);
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public Player getOwner() {
        return super.getOwner();
    }

    public boolean isOwned() {
        return getOwner() != null;
    }

    @Override
    public boolean canBuildStructure() {
        return false;
    }

    @Override
    public int getGoldPerTurn() {
        return 0;
    }

    @Override
    public int getFoodPerTurn() {
        return 0;
    }
}
