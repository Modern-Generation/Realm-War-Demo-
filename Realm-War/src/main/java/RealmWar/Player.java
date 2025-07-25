package RealmWar;

import Structures.Structures;
import Structures.TownHall;
import Units.Units;
import Blocks.*;

import java.util.*;

public class Player {
    private String name;
    private int gold;
    private int food;
    private int id;
    private int unitSpace;
    private int maxUnitSpace;
    private List<Units> units = new ArrayList<>();
    private List<Structures> structures = new ArrayList<>();
    private Set<Blocks> ownedBlocks = new HashSet<>();
    private boolean isDefeated = false;


    public Player(String name, int id) {
        this.name = name;
        this.id = id;
        this.gold = 10;
        this.food = 10;
        this.unitSpace = 0;
        this.maxUnitSpace = 0;
        this.units = new ArrayList<>();
        this.structures = new ArrayList<>();
        this.ownedBlocks = new HashSet<>();
    }

    public void setOwnedBlocks(Set<Blocks> ownedBlocks) {
        this.ownedBlocks = ownedBlocks != null ? ownedBlocks : new HashSet<>();
    }

    public void setStructures(List<Structures> structures) {
        this.structures = structures;
    }

    public void setUnits(List<Units> units) {
        this.units = units;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public List<Units> getUnits() {
        if (units == null) {
            units = new ArrayList<>();
        }
        return units;
    }

    public List<Structures> getStructures() {
        return structures;
    }

    public Set<Blocks> getOwnedBlocks() {
        if (ownedBlocks == null) ownedBlocks = new HashSet<>();
        return ownedBlocks;
    }

    public void startTurn() {
        maxUnitSpace = 0;
        generateResources();
        payMaintenance();
        updateUnitSpace();
        checkDefeat();
    }

    public void generateResources() {
        for (Blocks block : ownedBlocks) {
            if (block instanceof EmptyBlock)
                gold += 1;
            else if (block instanceof ForestBlock)
                food += 1;
        }
        for (Structures structure : structures) {
            gold += structure.getGoldPerTurn();
            food += structure.getFoodPerTurn();
            maxUnitSpace += structure.getUnitSpace();
        }
    }

    public void payMaintenance() {
        for (Structures structure : structures) {
            gold -= structure.getMaintenanceCost();
        }
        for (Units unit : units) {
            gold -= unit.getPayment();
            food -= unit.getRation();
        }
        if (gold < 0)
            gold = 0;
        if (food < 0)
            food = 0;
    }

    private void updateUnitSpace() {
        unitSpace = units.stream().mapToInt(Units::getUnitSpace).sum();
    }

    public boolean canBuildStructure(Structures structure) {
        return gold >= structure.getBuildingCost();
    }

    public void addStructure(Structures structure) {
        if (canBuildStructure(structure)) {
            structures.add(structure);
            gold -= structure.getBuildingCost();
        }
    }

    public void addOwnedBlock(Blocks block) {
        getOwnedBlocks().add(block);
    }

    public void removeStructure(Structures structure) {
        structures.remove(structure);
        if (structure instanceof TownHall)
            checkDefeat();
    }

    public boolean addUnit(Units unit) {
        if (gold >= unit.getPayment() && food >= unit.getRation()
                && unitSpace + unit.getUNITSpace() <= maxUnitSpace) {
            units.add(unit);
            gold -= unit.getPayment();
            food -= unit.getRation();
            unitSpace += unit.getUNITSpace();
            return true;
        }
        return false;
    }

    public void removeUnit(Units unit) {
        units.remove(unit);
    }

    public void collectResources() {
        for (Blocks block : ownedBlocks) {
            if (block instanceof EmptyBlock)
                gold += 1;
            else if (block instanceof ForestBlock)
                food += 1;
        }
        for (Structures structure : structures) {
            gold += structure.getGoldPerTurn();
            food += structure.getFoodPerTurn();
        }
        if (gold < 0) gold = 0;
        if (food < 0) food = 0;

    }

    public void spendResources(int goldCost, int foodCost) {
        if (canAfford(goldCost, foodCost)) {
            gold -= goldCost;
            food -= foodCost;
        }
    }

    public void checkDefeat() {
        boolean hasTownHall = structures.stream().anyMatch(structure -> structure instanceof TownHall);
        isDefeated = !hasTownHall;
    }

    public boolean isDefeated() {
        return isDefeated;
    }

    public void setDefeated(boolean defeated) {
        this.isDefeated = defeated;
    }

    public boolean canAfford(int goldCost, int foodCost) {
        return gold >= goldCost && food >= foodCost;
    }
}