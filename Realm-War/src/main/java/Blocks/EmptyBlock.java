package Blocks;

import Grid.Position;
import RealmWar.Player;

public class EmptyBlock extends Blocks {

    public EmptyBlock(Position position) {
        super(position);
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public Player getOwner() {
        return super.getOwner();
    }

    public boolean isOwned() {
        return owner != null;
    }

    @Override
    public boolean canBuildStructure() {
        return isOwned();
    }

    @Override
    public int getGoldPerTurn() {
        return isOwned() ? 5 : 0;
    }

    @Override
    public int getFoodPerTurn() {
        return 0;
    }
}
