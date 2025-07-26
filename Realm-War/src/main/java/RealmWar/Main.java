package RealmWar;

import GUI.GameGUI;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;

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
        menuFrame.setSize(400, 350);
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setLocationRelativeTo(null);
        menuFrame.setResizable(false);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        Font font = null;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/Font/Audiowide-Regular.ttf")).deriveFont(18f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);
        } catch (Exception e) {
            e.printStackTrace();
            font = new Font("SansSerif", Font.BOLD, 18);
        }

        JButton newGameBtn = new JButton("New Game");
        JButton loadGameBtn = new JButton("Load Game");
        JButton exitBtn = new JButton("Exit");

        // Button styling
        newGameBtn.setBackground(Color.GREEN);
        loadGameBtn.setBackground(Color.BLUE);
        exitBtn.setBackground(Color.RED);

        newGameBtn.setForeground(Color.WHITE);
        loadGameBtn.setForeground(Color.WHITE);
        exitBtn.setForeground(Color.WHITE);

        newGameBtn.setFont(font.deriveFont(20f));
        loadGameBtn.setFont(font.deriveFont(20f));
        exitBtn.setFont(font.deriveFont(20f));

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

        Timer randomColorTimer = new Timer(1000, e -> {
            Random rand = new Random();
            Color randomColor = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
            panel.setBackground(randomColor);
        });
        randomColorTimer.start();
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
            gui.syncResourcesToPlayers();

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
}