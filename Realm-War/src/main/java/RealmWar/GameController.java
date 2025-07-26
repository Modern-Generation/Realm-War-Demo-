package RealmWar;

import GUI.GameGUI;
import Grid.*;
import Structures.Structures;
import Structures.TownHall;
import Units.*;
import Units.*;
import Blocks.*;
import javax.swing.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GameController {
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> turnTimerTask;
    private int remainingTurnTime = 30;
    private List<Player> players;
    private Grid grid;
    private int currentPlayerIndex = 0;
    private Scanner scanner;
    private GameGUI gui;

    private ScheduledFuture<?> resourceTimerTask;


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

    public void startTimers() {
        stopTimers();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        turnTimerTask = scheduler.scheduleAtFixedRate(() -> {
            remainingTurnTime--;
            System.out.println("Remaining Time: " + remainingTurnTime + " seconds");

            if (remainingTurnTime <= 0) {
                remainingTurnTime = 30;
                SwingUtilities.invokeLater(() -> {
                    nextTurn();
                    Player current = getCurrentPlayer();
                    current.startTurn();
                    System.out.println("Turn of Player: " + current.getName());

                    if (gui != null) {
                        gui.refresh();
                    }
                });
            }
        }, 1, 1, TimeUnit.SECONDS);

        //Gain resources timer(every 3 seconds)
        resourceTimerTask = scheduler.scheduleAtFixedRate(() -> {
            SwingUtilities.invokeLater(() -> {
                for (Player player : players) {
                    if (!player.isDefeated()) {
                        int goldBefore = player.getGold();
                        int foodBefore = player.getFood();
                        player.collectResources();
                        int goldGained = player.getGold() - goldBefore;
                        int foodGained = player.getFood() - foodBefore;


                        System.out.println(player.getName() +
                                " gained " + goldGained + " gold and " +
                                foodGained + " food");

                        if (gui != null) {
                            gui.refresh();
                            if (goldGained > 0 || foodGained > 0) {
                                gui.showResourceGain(goldGained, foodGained);
                            }
                        }
                    }
                }
            });
        }, 0, 3, TimeUnit.SECONDS);
    }

    public void stopTimers() {
        if (turnTimerTask != null) {
            turnTimerTask.cancel(true);
        }
        if (resourceTimerTask != null) {
            resourceTimerTask.cancel(true);
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    public void resetTurnTimer() {
        this.remainingTurnTime = 30;
        startTimers();
    }

    public void handleAttack(Units attacker, Position targetPos) {
        if (attacker == null || targetPos == null) {
            System.out.println("Invalid attacker or target position");
            return;
        }

        if (!grid.isValidPosition(targetPos.getX(), targetPos.getY())) {
            System.out.println("Invalid target position");
            return;
        }

        Units targetUnit = grid.getUnitAt(targetPos);
        Structures targetStructure = grid.getStructure(targetPos);

        if (targetUnit != null && !targetUnit.getOwner().equals(attacker.getOwner())) {
            if (!attacker.isInRange(targetPos)) {
                System.out.println("Target is out of range");
                return;
            }

            targetUnit.takeDamage(attacker.getAttackPower());
            attacker.takeDamage(targetUnit.getAttackPower());

            System.out.println(attacker.getClass().getSimpleName() + " attacked " +
                    targetUnit.getClass().getSimpleName());

            if (!targetUnit.isAlive()) {
                grid.removeUnit(targetUnit);
                targetUnit.getOwner().removeUnit(targetUnit);
                System.out.println("Target unit destroyed!");
            }

            if (!attacker.isAlive()) {
                grid.removeUnit(attacker);
                attacker.getOwner().removeUnit(attacker);
                System.out.println("Attacker unit died!");
            }

        } else if (targetStructure != null && !targetStructure.getOwner().equals(attacker.getOwner())) {
            if (!attacker.isInRange(targetPos)) {
                System.out.println("Target is out of range");
                return;
            }

            targetStructure.takeDamage(attacker.getAttackPower());
            System.out.println("Structure attacked!");

            if (!targetStructure.isAlive()) {
                grid.destroyStructure(targetPos);
                System.out.println("Structure destroyed!");
            }

        } else {
            System.out.println("No valid enemy target at this position");
        }

        if (gui != null) {
            gui.refresh();
        }
    }

    /*public void handleStructureAttack(Units attacker, Structures targetStructure) {
        if (attacker == null || targetStructure == null) {
            return;
        }

        targetStructure.takeDamage(attacker.getAttackPower());
        attacker.takeDamage(targetStructure.getDurability());

        System.out.println(attacker.getClass().getSimpleName() + " attacked " + targetStructure.getClass().getSimpleName());
        System.out.println("Structure HP left: " + targetStructure.getDurability());
        System.out.println("Attacker HP left: " + attacker.getHitPoints());

        if (!attacker.isAlive()) {
            grid.removeUnit(attacker);
            attacker.getOwner().removeUnit(attacker);
            System.out.println("Attacker unit destroyed!");
        }

        if (!targetStructure.isAlive()) {
            Position structurePos = targetStructure.getOwner().getOwnedBlocks().stream()
                    .filter(b -> b.getStructure() == targetStructure).findFirst()
                    .map(b -> b.getPosition()).orElse(null);
            if (structurePos != null) {
                grid.destroyStructure(structurePos);
            }

            targetStructure.getOwner().removeStructure(targetStructure);

            if (targetStructure instanceof TownHall) {
                targetStructure.getOwner().setDefeated(true);
                System.out.println("Town Hall destroyed! " + targetStructure.getOwner().getName() + " has been defeated!");

                long alivePlayers = players.stream().filter(p -> !p.isDefeated()).count();
                if (alivePlayers == 1) {
                    Player winner = players.stream().filter(p -> !p.isDefeated()).findFirst().get();
                    JOptionPane.showMessageDialog(null,
                            "Congratulations, " + winner.getName() + " has won the game!",
                            "Victory", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                }
            }
        }
    }*/

    public void handleStructureAttack(Units attacker, Structures targetStructure) {
        if (attacker == null || targetStructure == null) {
            JOptionPane.showMessageDialog(null,
                    "Invalid attacker or target structure!",
                    "Attack Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        targetStructure.takeDamage(attacker.getAttackPower());
        attacker.takeDamage(targetStructure.getDurability());

        StringBuilder message = new StringBuilder();
        message.append(attacker.getClass().getSimpleName())
                .append(" attacked ")
                .append(targetStructure.getClass().getSimpleName())
                .append("\n");

        message.append("Structure HP left: ").append(targetStructure.getDurability()).append("\n");
        message.append("Attacker HP left: ").append(attacker.getHitPoints());

        JOptionPane.showMessageDialog(null, message.toString(), "Attack Result", JOptionPane.INFORMATION_MESSAGE);

        if (!attacker.isAlive()) {
            grid.removeUnit(attacker);
            attacker.getOwner().removeUnit(attacker);
            JOptionPane.showMessageDialog(null,
                    "Attacker unit destroyed!",
                    "Unit Death",
                    JOptionPane.WARNING_MESSAGE);
        }

        if (!targetStructure.isAlive()) {
            Position structurePos = targetStructure.getOwner().getOwnedBlocks().stream()
                    .filter(b -> b.getStructure() == targetStructure)
                    .map(b -> b.getPosition())
                    .findFirst()
                    .orElse(null);

            if (structurePos != null) {
                grid.destroyStructure(structurePos);
            }

            targetStructure.getOwner().removeStructure(targetStructure);

            if (targetStructure instanceof TownHall) {
                targetStructure.getOwner().setDefeated(true);
                JOptionPane.showMessageDialog(null,
                        "Town Hall destroyed!\n" + targetStructure.getOwner().getName() + " has been defeated!",
                        "Defeat",
                        JOptionPane.WARNING_MESSAGE);

                long alivePlayers = players.stream().filter(p -> !p.isDefeated()).count();
                if (alivePlayers == 1) {
                    Player winner = players.stream().filter(p -> !p.isDefeated()).findFirst().get();
                    JOptionPane.showMessageDialog(null,
                            "Congratulations, " + winner.getName() + " has won the game!",
                            "Victory",
                            JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        "Structure destroyed!",
                        "Structure",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }

        if (gui != null) {
            gui.refresh();
            gui.updateGameBoard();
        }
    }

    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        System.out.println("Turn changed to: " + getCurrentPlayer().getName());
    }

    boolean isGameOver() {
        return players.stream().filter(p -> !p.isDefeated()).count() <= 1;
    }

    public void setGui(GameGUI gui) {
        this.gui = gui;
    }

    public int getRemainingTurnTime() {
        return remainingTurnTime;
    }
}