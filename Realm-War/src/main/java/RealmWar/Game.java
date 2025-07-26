package RealmWar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import Utils.RuntimeTypeAdapterFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import Blocks.*;
import Grid.*;
import Structures.*;
import Units.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.concurrent.*;

public class Game {

    Scanner scanner = new Scanner(System.in);
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> turnTask;
    private ScheduledFuture<?> resourcesTask;
    private static final int turnDuration = 30;
    private List<Player> players;
    private int nowPlayerIndex;
    private Grid grid;
    private boolean isGameOver;
    private GameController gc;
    private long turnStartTime;
    private static final int TURN_DURATION = 30;
    private int remainingTurnTime = 30;

    public Game(List<Player> playerNames, int width, int height) {
        this.players = new ArrayList<>();
        for (Player p : playerNames) {
            this.players.add(new Player(p.getName(), p.getId()));
        }
        this.nowPlayerIndex = 0;
        this.grid = new Grid(width, height);
        this.isGameOver = false;
        this.gc = new GameController(players, grid, scanner);
        initGameBoard();
    }

    private void initGameBoard() {
        int playerCount = players.size();
        List<Position> startingPositions = generateStartingPositions(playerCount);

        for (int i = 0; i < playerCount; i++) {
            Player player = players.get(i);
            Position startingPos = startingPositions.get(i);

            //Own surrounding blocks
            for (Position p : grid.getAdjacentPositions(startingPos)) {
                Blocks adjacent = grid.getBlock(p);
                if (adjacent != null && adjacent.getOwner() == null) {
                    adjacent.setOwner(player);
                    player.addOwnedBlock(adjacent);
                }
            }
        }
    }

    private List<Position> generateStartingPositions(int count) {
        List<Position> positions = new ArrayList<>();
        int width = grid.getWidth();
        int height = grid.getHeight();

        if (count >= 1) positions.add(new Position(1, 1)); // top-left
        if (count >= 2) positions.add(new Position(width - 2, 1)); // top-right
        if (count >= 3) positions.add(new Position(1, height - 2)); // bottom-left
        if (count >= 4) positions.add(new Position(width - 2, height - 2)); // bottom-right
        return positions;
    }

    public Grid getGrid() {
        return grid;
    }

    public List<Player> getPlayers() {
        return players;
    }

    // --- Save game state to JSON file ---
    private Gson createGsonWithAdapters() {
        RuntimeTypeAdapterFactory<Structures> structureAdapter =
                RuntimeTypeAdapterFactory.of(Structures.class, "type")
                        .registerSubtype(TownHall.class, "TownHall")
                        .registerSubtype(Barrack.class, "Barrack")
                        .registerSubtype(Farm.class, "Farm")
                        .registerSubtype(Market.class, "Market")
                        .registerSubtype(Tower.class, "Tower");

        RuntimeTypeAdapterFactory<Units> unitsAdapter =
                RuntimeTypeAdapterFactory.of(Units.class, "type")
                        .registerSubtype(Knight.class, "Knight")
                        .registerSubtype(Peasant.class, "Peasant")
                        .registerSubtype(SpearMan.class, "SpearMan")
                        .registerSubtype(SwordMan.class, "SwordMan");

        RuntimeTypeAdapterFactory<Blocks> blocksAdapter =
                RuntimeTypeAdapterFactory.of(Blocks.class, "type")
                        .registerSubtype(EmptyBlock.class, "EmptyBlock")
                        .registerSubtype(ForestBlock.class, "ForestBlock")
                        .registerSubtype(VoidBlock.class, "VoidBlock");

        return new GsonBuilder()
                .registerTypeAdapterFactory(structureAdapter)
                .registerTypeAdapterFactory(unitsAdapter)
                .registerTypeAdapterFactory(blocksAdapter)
                .setPrettyPrinting()
                .create();
    }

    public void saveGame(String filePath) {
        Gson gson = createGsonWithAdapters();
        try (FileWriter writer = new FileWriter(filePath)) {
            JsonObject gameData = new JsonObject();

            for (Player player : players) {
                player.generateResources();
            }

            JsonElement playersJson = gson.toJsonTree(players);
            gameData.add("players", playersJson);

            JsonElement gridJson = gson.toJsonTree(grid);
            gameData.add("grid", gridJson);

            gameData.addProperty("currentPlayerIndex", gc.getCurrentPlayerIndex());

            gson.toJson(gameData, writer);
            System.out.println("Saved successfully at " + filePath);
        } catch (IOException e) {
            System.err.println("Error in saving Game: " + e.getMessage());
        }
    }

    // --- Load game state from JSON file ---
    public Game loadGame(String filePath) {
        Gson gson = createGsonWithAdapters();
        try (FileReader reader = new FileReader(filePath)) {
            JsonObject gameData = gson.fromJson(reader, JsonObject.class);

            //Deserialize players
            Player[] loadedPlayers = gson.fromJson(gameData.get("players"), Player[].class);
            List<Player> newPlayers = new ArrayList<>(Arrays.asList(loadedPlayers));

            //Deserialize grid
            Grid loadedGrid = gson.fromJson(gameData.get("grid"), Grid.class);

            //Create new game object
            Game newGame = new Game(newPlayers, loadedGrid.getWidth(), loadedGrid.getHeight());
            newGame.grid = loadedGrid;
            newGame.players = newPlayers;

            int currentPlayerIndex = gameData.get("currentPlayerIndex").getAsInt();
            newGame.gc.setCurrentPlayerIndex(currentPlayerIndex);

            fixOwners(newGame);

            return newGame;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load game." + e.getMessage());
            return null;
        }
    }

    public int getCurrentPlayerIndex() {
        return this.nowPlayerIndex;
    }

    private void fixOwners(Game game) {
        for (Player player : game.getPlayers()) {
            player.setOwnedBlocks(new HashSet<>());
            player.setStructures(new ArrayList<>());
            player.setUnits(new ArrayList<>());
        }

        for (int x = 0; x < game.getGrid().getWidth(); x++) {
            for (int y = 0; y < game.getGrid().getHeight(); y++) {
                Blocks block = game.getGrid().getBlock(x, y);
                Player owner = block.getOwner();
                if (owner != null) {
                    owner.addOwnedBlock(block);

                    Structures structure = block.getStructure();
                    if (structure != null) {
                        structure.setOwner(owner);
                        owner.getStructures().add(structure);
                    }

                    Units unit = block.getUnit();
                    if (unit != null) {
                        unit.setOwner(owner);
                        owner.getUnits().add(unit);
                    }
                }
            }
        }

        for (Units unit : game.getGrid().getAllUnits()) {
            for (Player player : game.getPlayers()) {
                if (player.getId() == unit.getOwnerId()) {
                    unit.setOwner(player);
                    player.getUnits().add(unit);
                    break;
                }
            }
        }
    }
}