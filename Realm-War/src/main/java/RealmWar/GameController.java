package RealmWar;

import Grid.*;
import Structures.TownHall;
import Units.*;
import Units.*;
import Blocks.*;

import java.util.*;

public class GameController {
    private List<Player> players;
    private Grid grid;
    private int currentPlayerIndex = 0;
    private Scanner scanner;

    public GameController(List<Player> players, Grid grid, Scanner scanner) {
        this.players = players;
        this.grid = grid;
        this.scanner = scanner;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int index) {
        this.currentPlayerIndex = index;
    }

    public void startGame() {
        while (!isGameOver()) {
            Player player = getCurrentPlayer();

            if (player.isDefeated()) {
                System.out.println("Oops! " + player.getName() + " is defeated! Skipping Turn...");
                nextTurn();
                continue;
            }

            System.out.println("\n==== " + player.getName() + "'s Turn ====");
            player.startTurn();

            boolean endTurn = false;
            while (!endTurn) {
                showPlayerInfo(player);
                System.out.println("1. Move Unit");
                System.out.println("2. Trian Unit");
                System.out.println("3. End Turn");
                System.out.println("Choose an action: ");
                int action = scanner.nextInt();
                scanner.nextLine();

                switch (action) {
                    case 1:
                        handleMoveUnit(player);
                        break;
                    case 2:
                        handleTrainUnit(player);
                        break;
                    case 3:
                        endTurn = true;
                        break;
                    default:
                        System.out.println("Invalid option");
                        break;
                }
            }
            removeDeadUnits();
            checkPlayerDefeat();
            nextTurn();
        }
        Player winner = getWinner();
        if (winner != null) {
            System.out.println("\n====Game Over!====\nWinner is: " + winner.getName());
        } else
            System.out.println("\n====Game Over! No Winner.====");
    }

    void handleMoveUnit(Player player) {
        List<Units> units = player.getUnits();
        if (units.isEmpty()) {
            System.out.println("You have no units to move!");
            return;
        }
        for (int i = 0; i < units.size(); i++) {
            Units unit = units.get(i);
            System.out.println((i + 1) + ". " + unit.getClass().getSimpleName() + " at " + unit.getPosition());
        }
        System.out.println("Choose a unit to move: ");
        int index = scanner.nextInt() - 1;
        scanner.nextLine();
        if (index < 0 || index >= units.size()) {
            System.out.println("Invalid unit index");
            return;
        }
        Units unit = units.get(index);
        System.out.print("Enter new X: ");
        int x = scanner.nextInt();
        System.out.print("Enter new Y: ");
        int y = scanner.nextInt();
        scanner.nextLine();

        Position newPos = new Position(x, y);
        if (!grid.isValidPosition(x, y)) {
            System.out.println("Invalid position");
            return;
        }
        Blocks myBlock = grid.getBlock(newPos);
        if (myBlock.getUnit() != null) {
            System.out.println("There is already a unit at this position");
            return;
        }
        grid.moveUnit(unit, newPos);
        System.out.println("Unit moved to " + newPos);
    }

    void handleTrainUnit(Player player) {
        System.out.println("Trainable units:");
        System.out.println("1. Peasant");
        System.out.println("2. SpearMan");
        System.out.println("3. SwordMan");
        System.out.println("4. Knight");
        System.out.print("Choose unit type: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter X position to Train: ");
        int x = scanner.nextInt();
        System.out.print("Enter Y position to Train: ");
        int y = scanner.nextInt();
        scanner.nextLine();
        Position newPos = new Position(x, y);

        if (!grid.isValidPosition(x, y)) {
            System.out.println("Invalid position");
            return;
        }
        Blocks block = grid.getBlock(newPos);
        if (block == null || !player.ownsBlock(block)) {
            System.out.println("Invalid position! You can only train units on your own territory.");
            return;
        }
        if (block.getUnit() != null) {
            System.out.println("There is already a unit at this position");
            return;
        }

        Units unit = null;
        switch (choice) {
            case 1:
                unit = new Peasant(player, newPos);
                break;
            case 2:
                unit = new SpearMan(player, newPos);
                break;
            case 3:
                unit = new SwordMan(player, newPos);
                break;
            case 4:
                unit = new Knight(player, newPos);
                break;
            default:
                System.out.println("Invalid choice");
                return;
        }

        if (player.addUnit(unit)) {
            grid.addUnit(unit);
            System.out.println(unit.getClass().getSimpleName() + " trained at " + newPos);
        } else {
            System.out.println("Not enough resources or unit space!");
        }
    }

    void showPlayerInfo(Player player) {
        System.out.println("---Player " + player.getName() + "'s Info---");
        System.out.println("Name: " + player.getName());
        System.out.println("Gold: " + player.getGold());
        System.out.println("Food: " + player.getFood());
        System.out.println("Units: " + player.getUnits().size());
        System.out.println("Structures: " + player.getStructures().size());
    }

    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    void removeDeadUnits() {
        for (Player player : players) {
            player.getUnits().removeIf(unit -> !unit.isAlive());
        }
    }

    void checkPlayerDefeat() {
        for (Player player : players) {
            boolean hasTownHall = player.getStructures().stream().anyMatch(
                    s -> s instanceof TownHall && s.isAlive());
            if (!hasTownHall) {
                player.setDefeated(true);
                System.out.println(player.getName() + " is defeated!");
            }
        }
    }

    boolean isGameOver() {
        return players.stream().filter(p -> !p.isDefeated()).count() <= 1;
    }

    public Player getWinner() {
        return players.stream().filter(p -> !p.isDefeated()).findFirst().orElse(null);
    }
}
