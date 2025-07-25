package Units;

import Grid.Position;
import RealmWar.Player;

public class SpearMan extends Units {

    public SpearMan(Player owner, Position position) {
        super(owner, position);
        this.hitPoints = 20;
        this.movementRange = 2;
        this.attackPower = 6;
        this.attackRange = 1;
        this.payment = 2;
        this.ration = 1;
        this.level = 1;
    }

    @Override
    public int getUnitSpace() {
        return 1;
    }
}
