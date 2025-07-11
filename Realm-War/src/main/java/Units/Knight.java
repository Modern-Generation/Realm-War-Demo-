package Units;

import Grid.Position;
import RealmWar.Player;

public class Knight extends Units {

    public Knight(Player owner, Position position) {
        super(owner, position);
        this.hitPoints = 30;
        this.movementRange = 3;
        this.attackPower = 5;
        this.attackRange = 1;
        this.payment = 5;
        this.ration = 3;
        this.level = 1;
    }

    @Override
    public int getUnitSpace() {
        return 3;
    }
}
