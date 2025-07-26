package Grid;

import Blocks.*;
import RealmWar.Player;
import Structures.Structures;
import Structures.TownHall;
import Units.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Grid {

    private int width;
    private int height;
    private Blocks[][] blocks;
    private List<Units> units = new ArrayList<>();

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        blocks = new Blocks[width][height];
        randomizeBlocks();
    }

    public void setUnit(Units unit) {
        Position pos = unit.getPosition();
        if (!isValidPosition(pos.getX(), pos.getY())) {
            System.out.println("Invalid position");
            return;
        }

        Blocks block = getBlock(pos);
        if (block.getUnit() != null || block.getStructure() != null) {
            System.out.println("You can only set a unit or structure on each block");
            return;
        }
        //If forest block -> Destroy it
        if (block instanceof ForestBlock) {
            ((ForestBlock) block).destroyForest();
            System.out.println("Forest destroyed at: " + pos);
        }
        units.add(unit);
        block.setUnit(unit);
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
        Blocks newBlock1 = getBlock(newPos);
        if (newBlock1 instanceof ForestBlock && ((ForestBlock) newBlock1).hasForest()) {
            ((ForestBlock) newBlock1).destroyForest();
        }
        Position oldPos = unit.getPosition();
        Units target = getUnitAt(newPos);
        if (target == null) {
            Blocks newBlock = getBlock(newPos);
            Blocks oldBlock = getBlock(oldPos);
            if (oldBlock != null) {
                oldBlock.removeUnit();
            }
            unit.setPosition(newPos);
            if (newBlock != null) {
                newBlock.setUnit(unit);
            }
            return;
        }

        if (!target.getOwner().equals(unit.getOwner())) {
            target.takeDamage(unit.getAttackPower());
            unit.takeDamage(target.getAttackPower());
            if (!target.isAlive()) {
                units.remove(target);
                target.getOwner().removeUnit(target);
                Blocks targetBlock = getBlock(newPos);
                if (targetBlock != null) {
                    targetBlock.removeUnit();
                }
                Blocks oldBlock = getBlock(oldPos);
                if (oldBlock != null) {
                    oldBlock.removeUnit();
                }
                unit.setPosition(newPos);
                if (targetBlock != null) {
                    targetBlock.setUnit(unit);
                }
            }
            if (!unit.isAlive()) {
                units.remove(unit);
                unit.getOwner().removeUnit(unit);
                Blocks oldBlock = getBlock(oldPos);
                if (oldBlock != null) {
                    oldBlock.removeUnit();
                }
            }
        } else {
            Units mergedUnit = mergeUnits(unit, target);
            if (mergedUnit != null) {
                units.remove(unit);
                units.remove(target);
                unit.getOwner().removeUnit(unit);
                target.getOwner().removeUnit(target);
                mergedUnit.setPosition(newPos);
                units.add(mergedUnit);
                mergedUnit.getOwner().addUnit(mergedUnit);

                Blocks oldBlock = getBlock(oldPos);
                Blocks targetBlock = getBlock(newPos);
                if (oldBlock != null) {
                    oldBlock.removeUnit();
                }
                if (targetBlock != null) {
                    targetBlock.removeUnit();
                }
                if (targetBlock != null) {
                    targetBlock.setUnit(mergedUnit);
                }
            }
        }
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
            units.remove(unit);
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

    public List<Units> getAllUnits() {
        return units;
    }

    public Blocks getBlock(Position position) {
        return getBlock(position.getX(), position.getY());
    }

    public void randomizeBlocks() {
        List<Position> allPositions = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                allPositions.add(new Position(x, y));
            }
        }

        Collections.shuffle(allPositions);

        int totalBlocks = width * height;
        int forestCount = (int) (totalBlocks * 0.8);
        int voidCount = totalBlocks / 10;   // about 10% Void block

        for (int i = 0; i < allPositions.size(); i++) {
            Position pos = allPositions.get(i);
            if (i < voidCount) {
                blocks[pos.getX()][pos.getY()] = new VoidBlock(pos);
            } else if (i < voidCount + forestCount) {
                blocks[pos.getX()][pos.getY()] = new ForestBlock(pos);
            } else {
                blocks[pos.getX()][pos.getY()] = new EmptyBlock(pos);
            }
        }
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
        if (block.getStructure() != null || block.getUnit() != null) {
            System.out.println("You can only build structure or train unit on each block");
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
