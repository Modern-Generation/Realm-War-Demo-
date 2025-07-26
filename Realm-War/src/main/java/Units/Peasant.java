package Units;

import Grid.Position;
import RealmWar.Player;

public class Peasant extends Units {

    public Peasant(Player owner, Position position) {
        super(owner, position);
        this.hitPoints = 15;
        this.movementRange = 2;
        this.attackPower = 5;
        this.attackRange = 1;
        this.payment = 1;
        this.ration = 1;
        this.level = 1;
    }

    @Override
    public int getUnitSpace() {
        return 1;
    }
}
