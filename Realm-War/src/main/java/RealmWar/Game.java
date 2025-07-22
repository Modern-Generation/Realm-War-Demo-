package RealmWar;

import GUI.GameGUI;
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

    public GameController getGc() {
        return gc;
    }


    public int getRemainingTurnTime() {
        return remainingTurnTime;
    }

    /*public void startTurnTimer() {
        stopTurnTimer();
        scheduler = Executors.newScheduledThreadPool(2);

        // تایمر نوبت بازی
        turnTask = scheduler.scheduleAtFixedRate(() -> {
            remainingTurnTime--;
            if (remainingTurnTime <= 0) {
                nextPlayerTurn();
                remainingTurnTime = 30; // ریست تایمر برای بازیکن بعدی
            }
        }, 1, 1, TimeUnit.SECONDS);

        // تایمر جمع‌آوری منابع
        resourcesTask = scheduler.scheduleAtFixedRate(() -> {
            for (Player p : players) {
                if (!p.isDefeated()) {
                    int goldGain = grid.valueOfGoldPerTurn(p);
                    int foodGain = grid.valueOfFoodPerTurn(p);
                    p.addGold(goldGain);
                    p.addFood(foodGain);
                    System.out.println(p.getName() + " gained " + goldGain + " gold and " + foodGain + " food");

                    if (gc.getGui() != null) {
                        gc.getGui().showResourceGain(goldGain, foodGain);
                    }
                }
            }
        }, 0, 3, TimeUnit.SECONDS);
    }*/

   /* public int getRemainingTurnTime() {
        if (turnStartTime == 0) return TURN_DURATION;
        long elapsed = System.currentTimeMillis() - turnStartTime;
        return Math.max(0, TURN_DURATION - (int)(elapsed / 1000));
    }*/

//    public void stopTurnTimer() {
//        if (turnTask != null) {
//            turnTask.cancel(true);
//        }
//        if (resourcesTask != null) {
//            resourcesTask.cancel(true);
//        }
//        if (scheduler != null) {
//            scheduler.shutdownNow();
//        }
//    }

    private void initGameBoard() {
        int playerCount = players.size();
        List<Position> startingPositions = generateStartingPositions(playerCount);

        for (int i = 0; i < playerCount; i++) {
            Player player = players.get(i);
            Position startingPos = startingPositions.get(i);

            //Create & Place TownHall
            TownHall townHall = new TownHall(player);
            grid.setStructure(startingPos, townHall);
            player.addStructure(townHall);

            //Own the Block
            Blocks startingBlock = grid.getBlock(startingPos);
            startingBlock.setOwner(player);
            player.addOwnedBlock(startingBlock);

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

    public Player getCurrentPlayer() {
        if (isGameOver || players.isEmpty())
            return null;
        return gc.getCurrentPlayer();
    }

    public void nextPlayerTurn() {
        turnStartTime = System.currentTimeMillis();

        int attempts = 0;
        do {
            nowPlayerIndex = (nowPlayerIndex + 1) % players.size();
            attempts++;
            if (attempts > players.size()) {
                isGameOver = true;
                System.out.println("No more players left. Game over!");
                gc.stopTimers();
                return;
            }
        } while (players.get(nowPlayerIndex).isDefeated());

        Player currentPlayer = getCurrentPlayer();
        currentPlayer.startTurn();
        System.out.println("It's " + currentPlayer.getName() + "'s turn!");
        checkVictoryCondition();
    }

    private void checkVictoryCondition() {
        int activePlayers = 0;
        Player lastPlayer = null;
        for (Player player : players) {
            if (!player.isDefeated()) {
                activePlayers++;
                lastPlayer = player;
            }
        }
        if (activePlayers <= 1) {
            isGameOver = true;
            if (lastPlayer != null)
                System.out.println("Winner: " + lastPlayer.getName());
            else
                System.out.println("Game Over! No Winner!");
        }
    }

    public boolean isGameOver() {
        return gc.isGameOver();
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
                        .registerSubtype(TownHall.class).registerSubtype(Barrack.class)
                        .registerSubtype(Farm.class).registerSubtype(Market.class).registerSubtype(Tower.class);

        RuntimeTypeAdapterFactory<Units> unitsAdapter =
                RuntimeTypeAdapterFactory.of(Units.class, "type")
                        .registerSubtype(Knight.class).registerSubtype(Peasant.class)
                        .registerSubtype(SpearMan.class).registerSubtype(SwordMan.class);

        RuntimeTypeAdapterFactory<Blocks> blocksAdapter =
                RuntimeTypeAdapterFactory.of(Blocks.class, "type")
                        .registerSubtype(EmptyBlock.class).registerSubtype(ForestBlock.class).registerSubtype(VoidBlock.class);

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

            JsonElement playersJson = gson.toJsonTree(players);
            gameData.add("players", playersJson);

            JsonElement gridJson = gson.toJsonTree(grid);
            gameData.add("grid", gridJson);

            gameData.addProperty("currentPlayerIndex", gc.getCurrentPlayerIndex());

            gson.toJson(gameData, writer);
            System.out.println("Saved successfully at " + filePath);
        } catch (IOException e) {
            System.err.println("ERROR in saving Game: " + e.getMessage());
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

    // --- Restart the game to initial state ---
//    public void restartGame() {
//        List<Player> resetPlayers = new ArrayList<>();
//        for (Player player : players) {
//            resetPlayers.add(new Player(player.getName(), player.getId()));
//        }
//        this.players = resetPlayers;
//        this.grid = new Grid(grid.getWidth(), grid.getHeight());
//        this.nowPlayerIndex = 0;
//        this.isGameOver = false;
//        initGameBoard();
//        System.out.println("Game restarted.");
//    }

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
//                else {
//                    Units unit = block.getUnit();
//                    if (unit != null && unit.getOwner() != null) {
//                        block.setOwner(unit.getOwner());
//                        unit.getOwner().addOwnedBlock(block);
//                        unit.getOwner().getUnits().add(unit);
//                    }
//                    Structures structure = block.getStructure();
//                    if (structure != null && structure.getOwner() != null) {
//                        block.setOwner((structure.getOwner()));
//                        structure.getOwner().addOwnedBlock(block);
//                        structure.getOwner().getStructures().add(structure);
//                    }
//                }
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