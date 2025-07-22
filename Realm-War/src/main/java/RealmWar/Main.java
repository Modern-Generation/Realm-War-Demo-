package RealmWar;

import GUI.GameGUI;
import Grid.*;
import Structures.*;
import Units.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.*;

public class Main {
    private static Game currentGame;
    private static GameController currentGameController;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            showMainMenu();
        });
    }

    private static void showMainMenu() {
        JFrame menuFrame = new JFrame("Realm War - Main Menu");
        menuFrame.setSize(400, 300);
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton newGameBtn = new JButton("New Game");
        JButton loadGameBtn = new JButton("Load Game");
        JButton exitBtn = new JButton("Exit");

        // Button styling
        Font btnFont = new Font("Tahoma", Font.BOLD, 16);
        newGameBtn.setFont(btnFont);
        loadGameBtn.setFont(btnFont);
        exitBtn.setFont(btnFont);

        newGameBtn.addActionListener(e -> {
            menuFrame.dispose();
            setupNewGame();
        });

        loadGameBtn.addActionListener(e -> {
            menuFrame.dispose();
            loadGame();
        });

        exitBtn.addActionListener(e -> {
            System.exit(0);
        });

        panel.add(newGameBtn);
        panel.add(loadGameBtn);
        panel.add(exitBtn);

        menuFrame.add(panel);
        menuFrame.setVisible(true);
    }

    private static void setupNewGame() {
        // Get number of players
        String[] options = {"2 Players", "3 Players", "4 Players"};
        int playerCount = JOptionPane.showOptionDialog(null,
                "Select number of players:",
                "Game Settings",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]) + 2;

        // Get player names
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= playerCount; i++) {
            String name = JOptionPane.showInputDialog(null,
                    "Enter name for Player " + i + ":",
                    "Player Names",
                    JOptionPane.PLAIN_MESSAGE);

            if (name == null || name.trim().isEmpty()) {
                name = "Player " + i;
            }
            players.add(new Player(name.trim(), i));
        }

        // Create new game
        currentGame = new Game(players, Config.GRID_WIDTH, Config.GRID_HEIGHT);
        currentGameController = new GameController(players, currentGame.getGrid(), new Scanner(System.in));

        // Show in-game menu options
        showInGameMenu();
    }

    private static void loadGame() {
        try {
            currentGame = new Game(new ArrayList<>(), Config.GRID_WIDTH, Config.GRID_HEIGHT);
            currentGame = currentGame.loadGame("saves/quicksave.json");

            currentGameController = new GameController(
                    currentGame.getPlayers(),
                    currentGame.getGrid(),
                    new Scanner(System.in));
            currentGameController.setCurrentPlayerIndex(currentGame.getCurrentPlayerIndex());

            JOptionPane.showMessageDialog(null,
                    "Game loaded successfully!",
                    "Load Game",
                    JOptionPane.INFORMATION_MESSAGE);

            showInGameMenu();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Error loading game: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            showMainMenu();
        }
    }

    private static void showInGameMenu() {
        GameGUI gui = new GameGUI(currentGame, currentGameController);
        currentGameController.setGui(gui);

        // Add game menu
        JMenuBar menuBar = new JMenuBar();

        // Game menu
        JMenu gameMenu = new JMenu("Game");
        JMenuItem saveItem = new JMenuItem("Save Game");
        JMenuItem mainMenuItem = new JMenuItem("Return to Main Menu");
        JMenuItem exitItem = new JMenuItem("Exit");

        saveItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Game");

            if (fileChooser.showSaveDialog(gui) == JFileChooser.APPROVE_OPTION) {
                currentGame.saveGame(fileChooser.getSelectedFile().getPath());
                JOptionPane.showMessageDialog(gui,
                        "Game saved successfully!",
                        "Save Game",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        mainMenuItem.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(gui,
                    "Return to main menu? Progress will not be saved.",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                gui.dispose();
                showMainMenu();
            }
        });

        exitItem.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(gui,
                    "Are you sure you want to exit the game?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        gameMenu.add(saveItem);
        gameMenu.addSeparator();
        gameMenu.add(mainMenuItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        menuBar.add(gameMenu);
        gui.setJMenuBar(menuBar);

        // Start game timer
        currentGameController.startTimers();
    }
}