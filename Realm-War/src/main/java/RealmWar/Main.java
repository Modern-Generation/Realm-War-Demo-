package RealmWar;

import Blocks.Blocks;
import Grid.Grid;
import Grid.Position;
import Structures.*;
import Units.*;
import com.google.gson.internal.bind.util.ISO8601Utils;

import java.util.*;
import java.util.concurrent.*;

public class Main {
    private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<Player> players = new ArrayList<>();
        System.out.println("Enter the number of players: ");
        int n = sc.nextInt();
        sc.nextLine();
        for (int i = 1; i <= n; i++) {
            System.out.println("Enter name for player " + i + ": ");
            String name = sc.nextLine().trim();
            players.add(new Player(name, i));
        }
        Game game = new Game(players, Config.GRID_WIDTH, Config.GRID_HEIGHT);
        Grid grid = new Grid(Config.GRID_WIDTH, Config.GRID_HEIGHT);
        GameController gc = new GameController(players, grid, sc);
        scheduler.scheduleAtFixedRate(() -> {
            if (!gc.isGameOver()) {
                Player currentPlayer = gc.getCurrentPlayer();
                System.out.println(currentPlayer.getName() + "'s turn!");
                currentPlayer.startTurn();

                //Time out Warning
                scheduler.schedule(() -> {
                    System.out.println("\n⚠ 5 Seconds Remaining!");
                }, 25, TimeUnit.SECONDS);

                handleTurn(currentPlayer, game, gc);
                gc.removeDeadUnits();
                gc.checkPlayerDefeat();
                gc.nextTurn();
            } else {
                Player winner = gc.getWinner();
                System.out.println("\n===Game over!===");
                System.out.println("Winner is: " + (winner != null ? winner.getName() : "Nobody"));
                scheduler.shutdown();
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    private static void handleTurn(Player currentPlayer, Game game, GameController gc) {
        Scanner scanner = new Scanner(System.in);
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 30 * 1000L) {
            System.out.println("\nIt's " + currentPlayer.getName() + "'s turn");
            gc.showPlayerInfo(currentPlayer);
            System.out.println("1. Build Structure");
            System.out.println("2. Train Unit");
            System.out.println("3. Move Unit");
            System.out.println("4. Save Game");
            System.out.println("5. Load Game");
            System.out.println("6. ReStart Game");
            System.out.print("Choose an option: ");
            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1:
                    buildStructure(currentPlayer, scanner);
                    break;
                case 2:
                    gc.handleTrainUnit(currentPlayer);
                    break;
                case 3:
                    gc.handleMoveUnit(currentPlayer);
                case 4:
                    game.saveGame("savingGameFile.json");
                    System.out.println("Game saved!");
                    break;
                case 5:
                    game = game.loadGame("savingGameFile.json");
                    game.stopTurnTimer();
                    game.startTurnTimer();
                    System.out.println("Game loaded!");
                    break;
                case 6:
                    game.restartGame();
                    game.stopTurnTimer();
                    game.startTurnTimer();
                    System.out.println("Game restarted!");
                default:
                    System.out.println("Invalid or delayed option! Waiting for next turn...");
            }
            if (System.currentTimeMillis() - startTime >= 30 * 1000L)
                break;
        }
        System.out.println("Turn Ended for " + currentPlayer.getName() + "!");
    }

    private static void buildStructure(Player player, Scanner scanner) {
        System.out.println("Available structures to build:");
        System.out.println("1. Town Hall");
        System.out.println("2. Tower");
        System.out.print("Choose structure to build: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        Structures structure = null;
        switch (choice) {
            case 1:
                structure = new TownHall(player);
                break;
            case 2:
                structure = new Tower(player);
                break;
            default:
                System.out.println("Invalid choice.");
                return;
        }
        System.out.println("Position X to build:");
        int x = scanner.nextInt();
        System.out.println("Position Y to build:");
        int y = scanner.nextInt();
        scanner.nextLine();

        Blocks block = player.getOwnedBlocks().stream().filter(b ->
                b.getPosition().getX() == x && b.getPosition().getY() == y).findFirst().orElse(null);

        if (block == null || !block.canBuildStructure()) {
            System.out.println("Can't build structure here!");
            return;
        }

        if (player.addStructure(structure)) {
            block.setStructure(structure);
            System.out.println(structure.getClass().getSimpleName() + " built!");
        } else {
            System.out.println("Not enough resources to build.");
        }
    }

    private static void trainUnit(Player player, Grid grid, Scanner scanner) {
        System.out.println("Available units to train:");
        System.out.println("1. Peasant");
        System.out.println("2. SpearMan");
        System.out.println("3. SwordMan");
        System.out.println("4. Knight");
        System.out.print("Choose unit to train: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        System.out.println("First enter your coordinate:");
        System.out.print("X: ");
        int x = scanner.nextInt();
        System.out.println(" Y: ");
        int y = scanner.nextInt();
        scanner.nextLine();
        Position position = new Position(x, y);

        Units unit;
        switch (choice) {
            case 1:
                unit = new Peasant(player, position);
                break;
            case 2:
                unit = new SpearMan(player, position);
                break;
            case 3:
                unit = new SwordMan(player, position);
                break;
            case 4:
                unit = new Knight(player, position);
                break;
            default:
                System.out.println("Invalid choice.");
                return;
        }

        if (player.addUnit(unit)) {
            grid.addUnit(unit);
            System.out.println(unit.getClass().getSimpleName() + " trained!");
        } else {
            System.out.println("Not enough resources or unit space.");
        }
    }
}
