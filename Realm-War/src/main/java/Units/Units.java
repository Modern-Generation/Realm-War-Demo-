package Units;

import Grid.Grid;
import Grid.Position;
import RealmWar.Player;

public abstract class Units {

    protected int hitPoints;
    protected int attackPower;
    protected int attackRange;
    protected int movementRange;
    protected int payment;
    protected int ration;
    protected int unitSpace;
    protected int level;
    protected transient Player owner;
    protected int ownerId;
    protected Position position;

    public Units(Player owner, Position position) {
        this.owner = owner;
        this.ownerId = owner.getId();
        this.position = position;
        this.level = 1;
    }

    public void setHitPoints(int hitPoints) {
        this.hitPoints = hitPoints;
    }

    public void setAttackPower(int attackPower) {
        this.attackPower = attackPower;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getAttackPower() {
        return attackPower;
    }

    public int getPayment() {
        return payment;
    }

    public int getRation() {
        return ration;
    }

    public int getUNITSpace() {
        return unitSpace;
    }

    public int getLevel() {
        return level;
    }

    public Player getOwner() {
        return owner;
    }

    // Unit takes damage
    public void takeDamage(int damage) {
        this.hitPoints -= damage;
        if (this.hitPoints < 0)
            this.hitPoints = 0;
    }

    // Check if unit is alive
    public boolean isAlive() {
        return hitPoints > 0;
    }

    public boolean isInRange(Position targetPos) {
        int dx = Math.abs(this.position.getX() - targetPos.getX());
        int dy = Math.abs(this.position.getY() - targetPos.getY());
        return dx + dy <= this.attackRange;
    }

    public abstract int getUnitSpace();
}
