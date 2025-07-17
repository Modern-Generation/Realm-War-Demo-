package RealmWar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Blocks.*;
import Grid.*;
import Structures.*;
import Units.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Scanner;
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

    public void startTurnTimer() {
        scheduler = Executors.newScheduledThreadPool(2);
        turnTask = scheduler.scheduleAtFixedRate(() -> {
                    nextPlayerTurn();
                },
                0, turnDuration, TimeUnit.SECONDS);
        resourcesTask = scheduler.scheduleAtFixedRate(() -> {
            for (Player p : players) {
                if (!p.isDefeated()) {
                    p.collectResources();
                    System.out.println(p.getName() + " has collected resources");
                }
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    public void stopTurnTimer() {
        if (turnTask != null) {
            turnTask.cancel(true);
        }
        if (resourcesTask != null) {
            resourcesTask.cancel(true);
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

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
        int attempts = 0;
        do {
            nowPlayerIndex = (nowPlayerIndex + 1) % players.size();
            attempts++;
            if (attempts > players.size()) {
                isGameOver = true;
                System.out.println("No more players left. Game over!");
                stopTurnTimer();
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
    public void saveGame(String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject gameData = new JsonObject();

            //Serialize players
            JsonElement playersJson = gson.toJsonTree(players);
            gameData.add("players", playersJson);

            //Serialize grid
            JsonElement gridJson = gson.toJsonTree(grid);
            gameData.add("grid", gridJson);

            //Serialize current turn index
            gameData.addProperty("CurrentPlayer", gc.getCurrentPlayerIndex());

            gson.toJson(this, writer);
            System.out.println("Game saved successfully to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to save game." + e.getMessage());
        }
    }

    // --- Load game state from JSON file ---
    public Game loadGame(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
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

            int currentPlayerIndex = gameData.get("CurrentPlayer").getAsInt();
            newGame.gc.setCurrentPlayerIndex(currentPlayerIndex);
            return newGame;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load game." + e.getMessage());
            return null;
        }
    }

    // --- Restart the game to initial state ---
    public void restartGame() {
        List<Player> resetPlayers = new ArrayList<>();
        for (Player player : players) {
            resetPlayers.add(new Player(player.getName(), player.getId()));
        }
        this.players = resetPlayers;
        this.grid = new Grid(grid.getWidth(), grid.getHeight());
        this.nowPlayerIndex = 0;
        this.isGameOver = false;
        initGameBoard();
        System.out.println("Game restarted.");
    }
}
