package Blocks;

import Grid.Position;
import RealmWar.Player;

public class ForestBlock extends Blocks {

    private boolean forestDestroyed = false;

    public ForestBlock(Position position) {
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
        return true;
    }

    @Override
    public int getGoldPerTurn() {
        return 0;
    }

    @Override
    public int getFoodPerTurn() {
        return (isOwned() && !forestDestroyed) ? 5 : 0;
    }

    public void destroyForest() {
        forestDestroyed = true;
    }

    public boolean hasForest() {
        return !forestDestroyed;
    }
}
