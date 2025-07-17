package Grid;

import Blocks.*;
import RealmWar.Player;
import Structures.Structures;
import Structures.TownHall;
import Units.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Grid {

    private int width;
    private int height;
    private Blocks[][] blocks;
    private List<Units> units = new ArrayList<>();

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        blocks = new Blocks[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                blocks[x][y] = new EmptyBlock(new Position(x, y));
            }
        }
    }

    public void addUnit(Units unit) {
        Position pos = unit.getPosition();
        if (isValidPosition(pos.getX(), pos.getY())) {
            units.add(unit);
        }
    }

    public Units getUnitAt(Position pos) {
        for (Units unit : units) {
            if (unit.getPosition().equals(pos)) {
                return unit;
            }
        }
        return null;
    }

    public void moveUnit(Units unit, Position newPos) {
        if (!isValidPosition(newPos.getX(), newPos.getY()))
            return;
        Units target = getUnitAt(newPos);
        if (target == null) {
            unit.setPosition(newPos);
            return;
        }
        if (!target.getOwner().equals(unit.getOwner())) {
            target.takeDamage(unit.getAttackPower());
            unit.takeDamage(target.getAttackPower());
            if (!target.isAlive()) {
                units.remove(target);
                target.getOwner().removeUnit(target);
                unit.setPosition(newPos);
            }
            if (!unit.isAlive()) {
                units.remove(unit);
                unit.getOwner().removeUnit(unit);
            }
        } else {
            Units mergedUnit = mergeUnits(unit, target);
            if (mergedUnit != null) {
                units.remove(unit);
                units.remove(target);
                mergedUnit.setPosition(newPos);
                units.add(mergedUnit);
                unit.getOwner().removeUnit(unit);
                target.getOwner().removeUnit(target);
                mergedUnit.getOwner().addUnit(mergedUnit);
            }
        }
    }

    private boolean isStronger(Units unit, Units target) {
        int unitIsHigher = unit.getHitPoints() + unit.getAttackPower() + unit.getLevel();
        int targetIsHigher = target.getHitPoints() + target.getAttackPower() + target.getLevel();
        return unitIsHigher > targetIsHigher;
    }

    private Units mergeUnits(Units unit, Units target) {
        if (!unit.getClass().equals(target.getClass()))
            return null;
        Units newUnit = null;
        if (unit instanceof SwordMan)
            newUnit = new SwordMan(unit.getOwner(), unit.getPosition());
        else if (unit instanceof SpearMan)
            newUnit = new SpearMan(unit.getOwner(), unit.getPosition());
        else if (unit instanceof Peasant)
            newUnit = new Peasant(unit.getOwner(), unit.getPosition());
        else if (unit instanceof Knight)
            newUnit = new Knight(unit.getOwner(), unit.getPosition());

        if (newUnit != null) {
            newUnit.setHitPoints(unit.getHitPoints() + target.getHitPoints());
            newUnit.setAttackPower(unit.getAttackPower() + target.getAttackPower());
            newUnit.setLevel(Math.max(unit.getLevel(), target.getLevel()) + 1);
        }
        return newUnit;
    }

    public void removeUnit(Units unit) {
        if (unit != null) {
            // حذف واحد از لیست واحدها
            units.remove(unit);
            // حذف واحد از بلوک مربوطه
            Blocks block = getBlock(unit.getPosition());
            if (block != null) {
                block.removeUnit();
            }
        }
    }

    public Blocks getBlock(int x, int y) {
        if (isValidPosition(x, y))
            return blocks[x][y];
        return null;
    }

    public List<Units> getUnitsOwnedBy(Player player) {
        return units.stream().filter(u -> u.getOwner().equals(player)).toList();
    }

    public List<Units> getAllUnits() {
        return units;
    }

    public Blocks getBlock(Position position) {
        return getBlock(position.getX(), position.getY());
    }

    public List<Blocks> getBlocksOwnedBy(Player player) {
        List<Blocks> blocksOwnedBy = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Blocks block = blocks[x][y];
                if (block.getOwner() != null && block.getOwner().equals(player)) {
                    blocksOwnedBy.add(block);
                }
            }
        }
        return blocksOwnedBy;
    }

    public int valueOfGoldPerTurn(Player player) {
        int gold = 0;
        for (Blocks block : getBlocksOwnedBy(player)) {
            gold += block.getGoldPerTurn();
        }
        return gold;
    }

    public int valueOfFoodPerTurn(Player player) {
        int food = 0;
        for (Blocks block : getBlocksOwnedBy(player)) {
            food += block.getFoodPerTurn();
        }
        return food;
    }

    public List<Position> getAdjacentPositions(Position pos) {
        List<Position> adjacents = new ArrayList<>();
        int x = pos.getX();
        int y = pos.getY();

        int[][] directions = {
                {-1, 0}, {1, 0}, // Right & Left
                {0, -1}, {0, 1}  // Top & Button
        };

        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];

            if (isValidPosition(newX, newY)) {
                adjacents.add(new Position(newX, newY));
            }
        }

        return adjacents;
    }

    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setStructure(Position startingPos, Structures structure) {

        Blocks block = getBlock(startingPos);
        if (block == null || !block.canBuildStructure()) {
            System.out.println("Can't build structure");
            return;
        }
        if (block instanceof ForestBlock) {
            ((ForestBlock) block).destroyForest();
            System.out.println("Forest destroyed at: " + startingPos);
        }
        block.setStructure(structure);
    }

    public Structures getStructure(Position position) {
        return getBlock(position).getStructure();
    }

    public void destroyStructure(Position position) {
        Blocks block = getBlock(position);
        if (block == null || block.getStructure() == null)
            return;
        Structures structure = block.getStructure();
        Player owner = structure.getOwner();
        block.setStructure(null);
        owner.removeStructure(structure);

        if (structure instanceof TownHall)
            owner.checkDefeat();
    }
}
