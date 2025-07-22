package GUI;

import Blocks.*;
import Grid.*;
import RealmWar.*;
import Structures.*;
import Units.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import javax.swing.JOptionPane;


public class GameGUI extends JFrame {
    private final JPanel controlPanel;
    private Game game;
    private GameController gameController;
    private JPanel gameBoard;
    private JPanel infoPanel;
    private JLabel currentPlayerLabel;
    private JLabel goldLabel;
    private JLabel foodLabel;
    private JLabel unitSpaceLabel;
    private JButton endTurnButton;
    private JButton buildButton;
    private JButton trainButton;
    private JButton moveButton;
    private Position selectedPosition;
    private JDialog actionDialog;
    private GameController gc;
    private Timer guiTimer;


    public GameGUI(Game game, GameController gameController) {
        this.gc = gc;
        this.game = game;
        this.gameController = gameController;
        controlPanel = new JPanel();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Realm War Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(1000, 800);
        setLocationRelativeTo(null);

        // Create game board
        gameBoard = new JPanel(new GridLayout(game.getGrid().getHeight(), game.getGrid().getWidth()));
        initializeGameBoard();

        // Create info panel
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        currentPlayerLabel = new JLabel();
        goldLabel = new JLabel();
        foodLabel = new JLabel();
        unitSpaceLabel = new JLabel();

        infoPanel.add(currentPlayerLabel);
        infoPanel.add(goldLabel);
        infoPanel.add(foodLabel);
        infoPanel.add(unitSpaceLabel);

        // Create action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buildButton = new JButton("Build Structure");
        trainButton = new JButton("Train Unit");
        moveButton = new JButton("Move Unit");
        endTurnButton = new JButton("End Turn");

        buildButton.addActionListener(e -> showBuildDialog());
        trainButton.addActionListener(e -> showTrainDialog());
        moveButton.addActionListener(e -> showMoveDialog());
        endTurnButton.addActionListener(e -> endTurn());

        buttonPanel.add(buildButton);
        buttonPanel.add(trainButton);
        buttonPanel.add(moveButton);
        buttonPanel.add(endTurnButton);

        infoPanel.add(buttonPanel);

        add(gameBoard, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.EAST);
        this.add(controlPanel, BorderLayout.SOUTH);

        JLabel turnTimerLabel;

        //infoPanel
        turnTimerLabel = new JLabel("Time left: 30s");
        infoPanel.add(turnTimerLabel);

        // Remaining time timer
        guiTimer = new Timer(1000, e -> {
            int remainingTime = gameController.getRemainingTurnTime();
            turnTimerLabel.setText("Time left: " + remainingTime + "s");
        });

        guiTimer.start();
        updateGameInfo();
        setVisible(true);
        Timer infoUpdateTimer = new Timer(1000, e -> updateGameInfo());
        infoUpdateTimer.start();

        //GameGUI
        JButton quickSaveBtn = new JButton("Save Game");
        quickSaveBtn.addActionListener(e -> {
            try {
                new File("saves").mkdirs();
                String savePath = "saves/quicksave.json";
                game.saveGame(savePath);

                JOptionPane.showMessageDialog(GameGUI.this,
                        "Game saved successfully\nPath: " + savePath,
                        "Save Game",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(GameGUI.this,
                        "ERROR in saving game!: " + ex.getMessage(),
                        "ERROR",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        if (controlPanel != null) {
            controlPanel.add(quickSaveBtn);
        } else {
            System.err.println("ERROR: ControlPanel Not Valued");
        }
    }

    private void initializeGameBoard() {
        gameBoard.removeAll();
        Grid grid = game.getGrid();

        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                Blocks block = grid.getBlock(x, y);
                JButton cell = new JButton();
                cell.setPreferredSize(new Dimension(60, 60));
                styleCell(cell, block);

                if (block.getUnit() != null) {
                    cell.setText(block.getUnit().getClass().getSimpleName());
                } else if (block.getStructure() != null) {
                    cell.setText(block.getStructure().getClass().getSimpleName());
                } else {
                    cell.setText("");
                }

                final int finalX = x;
                final int finalY = y;
                cell.addActionListener(e -> handleCellClick(finalX, finalY));

                gameBoard.add(cell);
            }
        }
    }

    private void styleCell(JButton cell, Blocks block) {
        // Set background color based on block type
        if (block instanceof EmptyBlock) {
            cell.setBackground(Color.LIGHT_GRAY);
        } else if (block instanceof ForestBlock) {
            cell.setBackground(((ForestBlock) block).hasForest() ?
                    new Color(34, 139, 34) : new Color(139, 69, 19));
        } else if (block instanceof VoidBlock) {
            cell.setBackground(Color.BLACK);
        }

        // Highlight owned blocks
        if (block.isOwned()) {
            Player owner = block.getOwner();
            if (owner.equals(gameController.getCurrentPlayer())) {
                cell.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
            } else {
                cell.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
            }
        } else {
            cell.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        }

        // Add structure/unit info
        StringBuilder text = new StringBuilder();
        if (block.getStructure() != null) {
            text.append(block.getStructure().getClass().getSimpleName()).append("\n");
        }
        if (block.getUnit() != null) {
            text.append(block.getUnit().getClass().getSimpleName())
                    .append(" Lv").append(block.getUnit().getLevel())
                    .append(" HP").append(block.getUnit().getHitPoints());
        }
        cell.setText(text.toString());
    }

    public void updateCellsAfterMove(Position oldPos, Position newPos) {
        updateCell(oldPos);
        updateCell(newPos);
        gameBoard.repaint();
    }

    private void updateCell(Position pos) {
        int index = pos.getY() * game.getGrid().getWidth() + pos.getX();
        JButton cell = (JButton) gameBoard.getComponent(index);
        Blocks block = game.getGrid().getBlock(pos);
        styleCell(cell, block);
    }

    private void handleCellClick(int x, int y) {
        selectedPosition = new Position(x, y);
        Blocks block = game.getGrid().getBlock(selectedPosition);

        if (block.isOwned() && block.getOwner().equals(gameController.getCurrentPlayer())) {
            showBlockActions(block);
        } else {
            JOptionPane.showMessageDialog(this,
                    "This block is not yours!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showBlockActions(Blocks block) {
        JPopupMenu popupMenu = new JPopupMenu();

        if (block.getUnit() != null && block.getUnit().getOwner().equals(gameController.getCurrentPlayer())) {
            JMenuItem moveItem = new JMenuItem("Move Unit");
            moveItem.addActionListener(e -> showMoveDialog());
            popupMenu.add(moveItem);

            JMenuItem attackItem = new JMenuItem("Attack");
            attackItem.addActionListener(e -> showAttackDialog(block.getUnit()));
            popupMenu.add(attackItem);
        }

        if (block.canBuildStructure()) {
            JMenuItem buildItem = new JMenuItem("Build Structure");
            buildItem.addActionListener(e -> showBuildDialog());
            popupMenu.add(buildItem);
        }

        if (block.getStructure() != null && block.getStructure().getOwner().equals(gameController.getCurrentPlayer())) {
            JMenuItem upgradeItem = new JMenuItem("Upgrade Structure");
            upgradeItem.addActionListener(e -> upgradeStructure(block.getStructure()));
            popupMenu.add(upgradeItem);
        }

        popupMenu.show(gameBoard,
                selectedPosition.getX() * 60,
                selectedPosition.getY() * 60);
    }

    private void showBuildDialog() {
        if (selectedPosition == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a block first!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Build Structure", true);
        dialog.setLayout(new GridLayout(0, 1));

        JButton townHallBtn = new JButton("Town Hall (Cost: 10 Gold)");
        JButton towerBtn = new JButton("Tower (Cost: 5 Gold)");
        JButton marketBtn = new JButton("Market (Cost: 5 Gold)");
        JButton farmBtn = new JButton("Farm (Cost: 5 Gold)");
        JButton barrackBtn = new JButton("Barrack (Cost: 5 Gold)");

        townHallBtn.addActionListener(e -> {
            buildStructure(new TownHall(gameController.getCurrentPlayer()));
            dialog.dispose();
        });

        towerBtn.addActionListener(e -> {
            buildStructure(new Tower(gameController.getCurrentPlayer()));
            dialog.dispose();
        });

        marketBtn.addActionListener(e -> {
            buildStructure(new Market());
            dialog.dispose();
        });

        farmBtn.addActionListener(e -> {
            buildStructure(new Farm());
            dialog.dispose();
        });

        barrackBtn.addActionListener(e -> {
            buildStructure(new Barrack());
            dialog.dispose();
        });

        dialog.add(townHallBtn);
        dialog.add(towerBtn);
        dialog.add(marketBtn);
        dialog.add(farmBtn);
        dialog.add(barrackBtn);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void buildStructure(Structures structure) {
        Player currentPlayer = gameController.getCurrentPlayer();

        if (currentPlayer.canBuildStructure(structure)) {
            game.getGrid().setStructure(selectedPosition, structure);
            currentPlayer.addStructure(structure);
            currentPlayer.generateResources();
            updateGameBoard();
            updateGameInfo();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Not enough resources to build!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTrainDialog() {
        if (selectedPosition == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a block first!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Train Unit", true);
        dialog.setLayout(new GridLayout(0, 1));

        JButton peasantBtn = new JButton("Peasant (Cost: 1 Gold, 1 Food)");
        JButton spearManBtn = new JButton("SpearMan (Cost: 2 Gold, 1 Food)");
        JButton swordManBtn = new JButton("SwordMan (Cost: 3 Gold, 2 Food)");
        JButton knightBtn = new JButton("Knight (Cost: 5 Gold, 3 Food)");

        peasantBtn.addActionListener(e -> {
            trainUnit(new Peasant(gameController.getCurrentPlayer(), selectedPosition));
            dialog.dispose();
        });

        spearManBtn.addActionListener(e -> {
            trainUnit(new SpearMan(gameController.getCurrentPlayer(), selectedPosition));
            dialog.dispose();
        });

        swordManBtn.addActionListener(e -> {
            trainUnit(new SwordMan(gameController.getCurrentPlayer(), selectedPosition));
            dialog.dispose();
        });

        knightBtn.addActionListener(e -> {
            trainUnit(new Knight(gameController.getCurrentPlayer(), selectedPosition));
            dialog.dispose();
        });

        dialog.add(peasantBtn);
        dialog.add(spearManBtn);
        dialog.add(swordManBtn);
        dialog.add(knightBtn);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

    }

    private void trainUnit(Units unit) {
        Player currentPlayer = gameController.getCurrentPlayer();

        if (currentPlayer.addUnit(unit)) {
            game.getGrid().addUnit(unit);
            game.getGrid().setUnit(unit);
            currentPlayer.addUnit(unit);
            updateGameBoard();
            updateGameInfo();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Not enough resources or unit space!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showMoveDialog() {
        if (selectedPosition == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a block first!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Units unit = game.getGrid().getUnitAt(selectedPosition);
        if (unit == null || !gameController.getCurrentPlayer().equals(unit.getOwner())) {
            JOptionPane.showMessageDialog(this,
                    "No valid unit selected!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Move Unit", true);
        dialog.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(2, 2));
        JTextField xField = new JTextField();
        JTextField yField = new JTextField();

        inputPanel.add(new JLabel("Target X:"));
        inputPanel.add(xField);
        inputPanel.add(new JLabel("Target Y:"));
        inputPanel.add(yField);

        JButton moveBtn = new JButton("Move");
        moveBtn.addActionListener(e -> {
            try {
                int x = Integer.parseInt(xField.getText());
                int y = Integer.parseInt(yField.getText());
                Position target = new Position(x, y);
                if (game.getGrid().isValidPosition(x, y)) {
                    Position oldPos = unit.getPosition();
                    game.getGrid().moveUnit(unit, target);
                    updateCellsAfterMove(oldPos, target);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Invalid position!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter valid coordinates!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(moveBtn, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showAttackDialog(Units attacker) {
        JDialog dialog = new JDialog(this, "Attack", true);
        dialog.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(2, 2));
        JTextField xField = new JTextField();
        JTextField yField = new JTextField();

        inputPanel.add(new JLabel("Target X:"));
        inputPanel.add(xField);
        inputPanel.add(new JLabel("Target Y:"));
        inputPanel.add(yField);

        JButton attackBtn = new JButton("Attack");
        attackBtn.addActionListener(e -> {
            try {
                int x = Integer.parseInt(xField.getText());
                int y = Integer.parseInt(yField.getText());
                Position targetPos = new Position(x, y);

                if (!game.getGrid().isValidPosition(x, y)) {
                    JOptionPane.showMessageDialog(dialog,
                            "Invalid position!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Units targetUnit = game.getGrid().getUnitAt(targetPos);
                Structures targetStructure = game.getGrid().getStructure(targetPos);

                if ((targetUnit == null || targetUnit.getOwner().equals(attacker.getOwner()))
                        && (targetStructure == null || targetStructure.getOwner().equals(attacker.getOwner()))) {
                    JOptionPane.showMessageDialog(dialog,
                            "No valid enemy target at this position!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!attacker.isInRange(targetPos)) {
                    JOptionPane.showMessageDialog(dialog,
                            "Target is out of attack range!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                gameController.handleAttack(attacker, targetPos);

                updateGameBoard();
                updateGameInfo();
                dialog.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter valid coordinates!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(attackBtn, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    private void upgradeStructure(Structures structure) {
        Player currentPlayer = gameController.getCurrentPlayer();

        if (structure.getCurrentLevel() < structure.getMaxLevel()) {
            int cost = structure.levelUpCost();
            if (currentPlayer.getGold() >= cost) {
                structure.levelUp();
                currentPlayer.spendResources(cost, 0);
                updateGameBoard();
                updateGameInfo();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Not enough gold to upgrade!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Structure is already at max level!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void endTurn() {
        gameController.stopTimers();
        gameController.nextTurn();
        gameController.resetTurnTimer();
        updateGameInfo();
        selectedPosition = null;

        if (guiTimer != null) {
            guiTimer.restart();
        }
    }

    public void updateGameInfo() {
        Player currentPlayer = gameController.getCurrentPlayer();
        currentPlayerLabel.setText("Player: " + currentPlayer.getName());
        goldLabel.setText("Gold: " + currentPlayer.getGold());
        foodLabel.setText("Food: " + currentPlayer.getFood());
        unitSpaceLabel.setText("Unit Space: " + currentPlayer.getCurrentUsedUnitSpace() +
                "/" + currentPlayer.getMaxUnitSpace());

        // Highlight current player's blocks
        updateGameBoard();

    }

    public void updateGameBoard() {
        initializeGameBoard();
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            // Initialize game with 2 players for testing
            List<Player> players = List.of(
                    new Player("Player 1", 1),
                    new Player("Player 2", 2)
            );

            Game game = new Game(players, Config.GRID_WIDTH, Config.GRID_HEIGHT);
            GameController gc = new GameController(players, game.getGrid(), new Scanner(System.in));

            new GameGUI(game, gc);
        });
    }

    public void refresh() {
        Player currentPlayer = gameController.getCurrentPlayer();

        currentPlayerLabel.setText("Player: " + currentPlayer.getName());
        goldLabel.setText("Gold: " + currentPlayer.getGold());
        foodLabel.setText("Food: " + currentPlayer.getFood());

        infoPanel.repaint();
        infoPanel.revalidate();
    }

    public void showResourceGain(int gold, int food) {
        JLabel gainLabel = new JLabel("+" + gold + " Gold, +" + food + " Food");
        gainLabel.setForeground(Color.GREEN);
        gainLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // نمایش موقت پیام
        // JPanel messagePanel = new JPanel();
        //messagePanel.add(gainLabel);

        //  JOptionPane optionPane = new JOptionPane(messagePanel, JOptionPane.INFORMATION_MESSAGE);
        // JDialog dialog = optionPane.createDialog("Resources Gained");
        //  dialog.setModal(false);
        //  dialog.setVisible(true);

        //  new Timer(2000, e -> {
        //   dialog.dispose();
        //}).start();
    }

}