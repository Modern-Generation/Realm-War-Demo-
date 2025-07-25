package Units;

import Grid.Position;
import RealmWar.Player;

public class SwordMan extends Units {

    public SwordMan(Player owner, Position position) {
        super(owner, position);
        this.hitPoints = 20;
        this.movementRange = 2;
        this.attackPower = 3;
        this.attackRange = 2;
        this.payment = 3;
        this.ration = 2;
        this.level = 1;
    }

    @Override
    public int getUnitSpace() {
        return 2;
    }
}
