package GUI;

import Blocks.*;
import Grid.*;
import RealmWar.*;
import Structures.*;
import Units.*;
import javax.swing.*;
import java.awt.*;
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
    private JButton attackButton;
    private JButton upgradeStructureButton;
    private Position selectedPosition;
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
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buildButton = new JButton("Build Structure");
        trainButton = new JButton("Train Unit");
        moveButton = new JButton("Move Unit");
        attackButton = new JButton("Attack");
        upgradeStructureButton = new JButton("Upgrade Structure");
        endTurnButton = new JButton("End Turn");

        Dimension buttonSize = new Dimension(160, 40);

        buildButton.setMaximumSize(buttonSize);
        trainButton.setMaximumSize(buttonSize);
        moveButton.setMaximumSize(buttonSize);
        attackButton.setMaximumSize(buttonSize);
        upgradeStructureButton.setMaximumSize(buttonSize);
        endTurnButton.setMaximumSize(buttonSize);

        buttonPanel.add(buildButton);
        buttonPanel.add(Box.createVerticalStrut(20));

        buttonPanel.add(trainButton);
        buttonPanel.add(Box.createVerticalStrut(20));

        buttonPanel.add(moveButton);
        buttonPanel.add(Box.createVerticalStrut(20));

        buttonPanel.add(attackButton);
        buttonPanel.add(Box.createVerticalStrut(20));

        buttonPanel.add(upgradeStructureButton);
        buttonPanel.add(Box.createVerticalStrut(20));

        buttonPanel.add(endTurnButton);

        buildButton.setBackground(new Color(139, 69, 19));
        buildButton.setForeground(Color.WHITE);

        trainButton.setBackground(new Color(135, 206, 235));
        trainButton.setForeground(Color.WHITE);

        moveButton.setBackground(new Color(0, 0, 128));
        moveButton.setForeground(Color.WHITE);

        attackButton.setBackground(new Color(128, 0, 0));
        attackButton.setForeground(Color.WHITE);

        upgradeStructureButton.setBackground(new Color(128, 0, 128));
        upgradeStructureButton.setForeground(Color.WHITE);

        endTurnButton.setBackground(new Color(255, 140, 0));
        endTurnButton.setForeground(Color.WHITE);

        buildButton.addActionListener(e -> showBuildDialog());
        trainButton.addActionListener(e -> showTrainDialog());
        moveButton.addActionListener(e -> showMoveDialog());
        attackButton.addActionListener(e -> showAttackDialog());
        upgradeStructureButton.addActionListener(e -> {
            if (selectedPosition == null) {
                JOptionPane.showMessageDialog(this,
                        "Please select a block first!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Blocks block = game.getGrid().getBlock(selectedPosition);
            Structures structure = block.getStructure();

            if (structure == null || !structure.getOwner().equals(gameController.getCurrentPlayer())) {
                JOptionPane.showMessageDialog(this,
                        "No owned structure at this position!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            upgradeStructure(structure);
        });
        endTurnButton.addActionListener(e -> endTurn());

        buttonPanel.add(buildButton);
        buttonPanel.add(trainButton);
        buttonPanel.add(moveButton);
        buttonPanel.add(attackButton);
        buttonPanel.add(upgradeStructureButton);
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
        Font buttonFont = new Font("Arial", Font.BOLD, 14);
        Color buttonColor = Color.DARK_GRAY;
        Color textColor = Color.WHITE;

        JButton quickSaveBtn = new JButton("Save Game");
        quickSaveBtn.setPreferredSize(buttonSize);
        quickSaveBtn.setFont(buttonFont);
        quickSaveBtn.setBackground(buttonColor);
        quickSaveBtn.setForeground(textColor);
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

        JButton returnBtn = new JButton("Main Menu");

        returnBtn.setPreferredSize(buttonSize);
        returnBtn.setFont(buttonFont);
        returnBtn.setBackground(buttonColor);
        returnBtn.setForeground(textColor);

        returnBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(GameGUI.this,
                    "Return to main menu? Progress will not be saved.",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                dispose(); // Close current GUI
                RealmWar.Main.main(null);
            }
        });

        if (controlPanel != null) {
            controlPanel.add(quickSaveBtn);
            controlPanel.add(returnBtn);
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

                final int finalX = x;
                final int finalY = y;
                cell.addActionListener(e -> handleCellClick(finalX, finalY));

                gameBoard.add(cell);
            }
        }
        gameBoard.revalidate();
        gameBoard.repaint();
    }

    /*private void styleCell(JButton cell, Blocks block) {
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
                cell.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            }
        } else {
            cell.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        }

        cell.setText("");
        cell.setIcon(null);

        // Add unit/structure info
        StringBuilder text = new StringBuilder("<html>");

        if (block.getStructure() != null) {
            text.append(block.getStructure().getClass().getSimpleName()).append("<br>");
        }

        if (block.getUnit() != null) {
            text.append(block.getUnit().getClass().getSimpleName())
                    .append(" Lv").append(block.getUnit().getLevel())
                    .append(" HP").append(block.getUnit().getHitPoints())
                    .append("<br>");
        }

        text.append("</html>");

        cell.setText(text.toString());
        cell.setHorizontalTextPosition(JButton.CENTER);
        cell.setVerticalTextPosition(JButton.CENTER);
    }*/

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
                cell.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            }
        } else {
            cell.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        }

        // Show structure and unit info
        StringBuilder text = new StringBuilder("<html>");

        if (block.getStructure() != null) {
            Structures structure = block.getStructure();
            text.append(structure.getClass().getSimpleName())
                    .append(" Lv").append(structure.getCurrentLevel())
                    .append(" HP: ").append(structure.getDurability())
                    .append("<br>");
        }

        if (block.getUnit() != null) {
            Units unit = block.getUnit();
            text.append(unit.getClass().getSimpleName())
                    .append(" Lv").append(unit.getLevel())
                    .append(" HP").append(unit.getHitPoints())
                    .append("<br>");
        }

        text.append("</html>");
        cell.setText(text.toString());
        cell.setHorizontalTextPosition(JButton.CENTER);
        cell.setVerticalTextPosition(JButton.CENTER);
    }

    public void updateCellsAfterMove(Position oldPos, Position newPos) {
        Blocks newBlock = game.getGrid().getBlock(newPos);
        if (newBlock instanceof ForestBlock && ((ForestBlock) newBlock).hasForest()) {
            ((ForestBlock) newBlock).destroyForest();
        }
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
//            JOptionPane.showMessageDialog(this,
//                    "This block is not yours!", "Error", JOptionPane.ERROR_MESSAGE);
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

        popupMenu.show(gameBoard, selectedPosition.getX() * 60, selectedPosition.getY() * 60);
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
            buildStructure(new Market(gameController.getCurrentPlayer()));
            dialog.dispose();
        });

        farmBtn.addActionListener(e -> {
            buildStructure(new Farm(gameController.getCurrentPlayer()));
            dialog.dispose();
        });

        barrackBtn.addActionListener(e -> {
            buildStructure(new Barrack(gameController.getCurrentPlayer()));
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
            currentPlayer.generateResources();
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

    private void showAttackDialog() {
        if (selectedPosition == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a block with your unit first!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Blocks block = game.getGrid().getBlock(selectedPosition);
        Units unit = block.getUnit();

        if (unit == null || !unit.getOwner().equals(gameController.getCurrentPlayer())) {
            JOptionPane.showMessageDialog(this,
                    "No valid unit selected for attack!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        showAttackDialog(unit);
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

                if (targetUnit != null && !targetUnit.getOwner().equals(attacker.getOwner())) {
                    if (!attacker.isInRange(targetPos)) {
                        JOptionPane.showMessageDialog(dialog,
                                "Target unit is out of range!",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    gameController.handleAttack(attacker, targetPos);
                } else if (targetStructure != null && targetStructure.getOwner() != null &&
                        !targetStructure.getOwner().equals(attacker.getOwner())) {
                    if (!attacker.isInRange(targetPos)) {
                        JOptionPane.showMessageDialog(dialog,
                                "Target Structure is out of range!",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    gameController.handleStructureAttack(attacker, targetStructure);
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "You can't attack you're own unit or structure!",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                updateGameBoard();
                updateGameInfo();
                dialog.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter valid coordinates!",
                        "Error", JOptionPane.ERROR_MESSAGE);
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

            //new GameGUI(game, gc);
            GameGUI gui = new GameGUI(game, gc);
            gui.updateGameBoard();
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
    }

    public void syncResourcesToPlayers() {
        Player currentPlayer = gameController.getCurrentPlayer();

        try {
            int gold = Integer.parseInt(goldLabel.getText().replaceAll("[^\\d]", ""));
            int food = Integer.parseInt(foodLabel.getText().replaceAll("[^\\d]", ""));

            currentPlayer.setGold(gold);
            currentPlayer.setFood(food);
        } catch (NumberFormatException ex) {
            System.err.println("Failed to parse gold or food: " + ex.getMessage());
        }
    }
}