package Blocks;

import RealmWar.Player;
import Grid.Position;
import Structures.Structures;
import Units.Units;

public abstract class Blocks {
    protected boolean isOwned;
    protected transient Player owner;
    private Position position;
    private transient Structures structure;
    private transient Units units;

    public Blocks(Position position) {
        this.position = position;
    }

    public boolean isOwned() {
        return owner != null;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
        this.isOwned = (owner != null);
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setUnit(Units unit) {
        this.units = unit;
    }

    public Units getUnit() {
        return units;
    }

    public void removeUnit() {
        this.units = null;
    }

    public Structures getStructure() {
        return structure;
    }

    public void setStructure(Structures structure) {
        this.structure = structure;
    }

    public abstract boolean canBuildStructure();

    public abstract int getGoldPerTurn();

    public abstract int getFoodPerTurn();
}
