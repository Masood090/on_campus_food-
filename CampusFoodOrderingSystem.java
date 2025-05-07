import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class CampusFoodOrderingSystem {
    // Main method to launch the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginFrame();
        });
    }
}

// Database connection class
class DatabaseConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/campus_food_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // SQL creation script for reference
    public static void createDatabaseTables() {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();

            // Create Users table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Users (" +
                    "ID INT AUTO_INCREMENT PRIMARY KEY, " +
                    "Name VARCHAR(100) NOT NULL, " +
                    "Email VARCHAR(100) UNIQUE NOT NULL, " +
                    "Phone VARCHAR(20), " +
                    "Password VARCHAR(100) NOT NULL)");

            // Create Canteens table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Canteens (" +
                    "ID INT AUTO_INCREMENT PRIMARY KEY, " +
                    "Name VARCHAR(100) NOT NULL, " +
                    "Location VARCHAR(255) NOT NULL)");

            // Create Menu_Items table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Menu_Items (" +
                    "ID INT AUTO_INCREMENT PRIMARY KEY, " +
                    "Name VARCHAR(100) NOT NULL, " +
                    "Description TEXT, " +
                    "Price DECIMAL(10,2) NOT NULL)");

            // Create Orders table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Orders (" +
                    "ID INT AUTO_INCREMENT PRIMARY KEY, " +
                    "User_id INT NOT NULL, " +
                    "Status VARCHAR(50) DEFAULT 'Pending', " +
                    "Order_Date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (User_id) REFERENCES Users(ID))");

            // Create Order_Items table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Order_Items (" +
                    "Order_id INT NOT NULL, " +
                    "MenuItem_id INT NOT NULL, " +
                    "Quantity INT NOT NULL, " +
                    "PRIMARY KEY (Order_id, MenuItem_id), " +
                    "FOREIGN KEY (Order_id) REFERENCES Orders(ID), " +
                    "FOREIGN KEY (MenuItem_id) REFERENCES Menu_Items(ID))");

            System.out.println("Database tables created successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error creating database tables: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// Login Frame
class LoginFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("Campus Food Ordering System - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel headerLabel = new JLabel("Campus Food Ordering System", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // Login Panel
        JPanel loginPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        loginPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        loginPanel.add(emailField);

        loginPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        loginPanel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> attemptLogin());

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> openRegisterFrame());

        loginPanel.add(loginButton);
        loginPanel.add(registerButton);

        mainPanel.add(loginPanel, BorderLayout.CENTER);

        // Database setup button
        JButton setupDbButton = new JButton("Setup Database");
        setupDbButton.addActionListener(e -> DatabaseConnection.createDatabaseTables());
        mainPanel.add(setupDbButton, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private void attemptLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both email and password",
                    "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT ID, Name FROM Users WHERE Email = ? AND Password = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, email);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("ID");
                String name = rs.getString("Name");
                JOptionPane.showMessageDialog(this, "Login successful. Welcome, " + name + "!");

                new MainFrame(userId, name);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid email or password",
                        "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRegisterFrame() {
        new RegisterFrame();
        dispose();
    }
}

// Register Frame
class RegisterFrame extends JFrame {
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    public RegisterFrame() {
        setTitle("Campus Food Ordering System - Register");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel headerLabel = new JLabel("New User Registration", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));

        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("Phone:"));
        phoneField = new JTextField();
        formPanel.add(phoneField);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        formPanel.add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField();
        formPanel.add(confirmPasswordField);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> attemptRegister());

        JButton backButton = new JButton("Back to Login");
        backButton.addActionListener(e -> {
            new LoginFrame();
            dispose();
        });

        formPanel.add(registerButton);
        formPanel.add(backButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        add(mainPanel);
        setVisible(true);
    }

    private void attemptRegister() {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, Email and Password are required fields",
                    "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match",
                    "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO Users (Name, Email, Phone, Password) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setString(4, password);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Registration successful! You can now login.");
                new LoginFrame();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("for key 'Users.Email'")) {
                JOptionPane.showMessageDialog(this, "Email already exists! Please use a different email.",
                        "Registration Error", JOptionPane.ERROR_MESSAGE);
            } else {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

// Main Application Frame
class MainFrame extends JFrame {
    private int userId;
    private String userName;
    private JTabbedPane tabbedPane;

    public MainFrame(int userId, String userName) {
        this.userId = userId;
        this.userName = userName;

        setTitle("Campus Food Ordering System - Welcome " + userName);
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create tabbed pane for different sections
        tabbedPane = new JTabbedPane();

        // Add different panels for each functionality
        tabbedPane.addTab("Menu Items", new MenuItemsPanel());
        tabbedPane.addTab("Place Order", new PlaceOrderPanel(userId));
        tabbedPane.addTab("My Orders", new MyOrdersPanel(userId));
        tabbedPane.addTab("Canteens", new CanteensPanel());

        add(tabbedPane);

        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu userMenu = new JMenu("User");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> {
            new LoginFrame();
            dispose();
        });
        userMenu.add(logoutItem);
        menuBar.add(userMenu);
        setJMenuBar(menuBar);

        setVisible(true);
    }
}

// Menu Items Panel
class MenuItemsPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField nameField, descField, priceField, idField;

    public MenuItemsPanel() {
        setLayout(new BorderLayout());

        // Create table
        String[] columns = {"ID", "Name", "Description", "Price"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
                    nameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                    descField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                    priceField.setText(tableModel.getValueAt(selectedRow, 3).toString());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Menu Item Details"));

        formPanel.add(new JLabel("ID:"));
        idField = new JTextField();
        idField.setEditable(false);
        formPanel.add(idField);

        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Description:"));
        descField = new JTextField();
        formPanel.add(descField);

        formPanel.add(new JLabel("Price:"));
        priceField = new JTextField();
        formPanel.add(priceField);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> addMenuItem());

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> updateMenuItem());

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteMenuItem());

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearFields());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        formPanel.add(buttonPanel);

        add(formPanel, BorderLayout.SOUTH);

        // Load data
        loadMenuItems();
    }

    private void loadMenuItems() {
        tableModel.setRowCount(0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM Menu_Items ORDER BY ID";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("ID"));
                row.add(rs.getString("Name"));
                row.add(rs.getString("Description"));
                row.add(rs.getDouble("Price"));
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading menu items: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addMenuItem() {
        String name = nameField.getText();
        String desc = descField.getText();
        String priceStr = priceField.getText();

        if (name.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Price are required fields",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "INSERT INTO Menu_Items (Name, Description, Price) VALUES (?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, name);
                pstmt.setString(2, desc);
                pstmt.setDouble(3, price);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Menu item added successfully!");
                    clearFields();
                    loadMenuItems();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add menu item",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price format. Please enter a valid number.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMenuItem() {
        String idStr = idField.getText();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a menu item to update",
                    "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = nameField.getText();
        String desc = descField.getText();
        String priceStr = priceField.getText();

        if (name.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Price are required fields",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            double price = Double.parseDouble(priceStr);

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE Menu_Items SET Name=?, Description=?, Price=? WHERE ID=?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, name);
                pstmt.setString(2, desc);
                pstmt.setDouble(3, price);
                pstmt.setInt(4, id);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Menu item updated successfully!");
                    clearFields();
                    loadMenuItems();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update menu item",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid ID or price format",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteMenuItem() {
        String idStr = idField.getText();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a menu item to delete",
                    "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this menu item?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int id = Integer.parseInt(idStr);

            try (Connection conn = DatabaseConnection.getConnection()) {
                // First check if this menu item is used in any orders
                String checkQuery = "SELECT COUNT(*) FROM Order_Items WHERE MenuItem_id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setInt(1, id);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);

                if (count > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Cannot delete this menu item as it is referenced in " + count + " orders.",
                            "Delete Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // If not used in any orders, proceed with deletion
                String query = "DELETE FROM Menu_Items WHERE ID = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, id);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Menu item deleted successfully!");
                    clearFields();
                    loadMenuItems();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete menu item",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid ID format",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        descField.setText("");
        priceField.setText("");
        table.clearSelection();
    }
}

// Place Order Panel
class PlaceOrderPanel extends JPanel {
    private int userId;
    private JTable menuTable;
    private JTable cartTable;
    private DefaultTableModel menuTableModel;
    private DefaultTableModel cartTableModel;
    private JSpinner quantitySpinner;
    private JComboBox<String> canteenComboBox;
    private JLabel totalLabel;
    private double total = 0.0;

    public PlaceOrderPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());

        // Canteen selection
        JPanel canteenPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        canteenPanel.add(new JLabel("Select Canteen:"));
        canteenComboBox = new JComboBox<>();
        loadCanteens();
        canteenPanel.add(canteenComboBox);

        JButton refreshButton = new JButton("Refresh Menu");
        refreshButton.addActionListener(e -> loadMenuItems());
        canteenPanel.add(refreshButton);

        topPanel.add(canteenPanel, BorderLayout.NORTH);

        // Menu table
        String[] menuColumns = {"ID", "Name", "Description", "Price"};
        menuTableModel = new DefaultTableModel(menuColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        menuTable = new JTable(menuTableModel);
        JScrollPane menuScrollPane = new JScrollPane(menuTable);
        menuScrollPane.setBorder(BorderFactory.createTitledBorder("Available Menu Items"));

        // Cart table
        String[] cartColumns = {"ID", "Name", "Price", "Quantity", "Subtotal"};
        cartTableModel = new DefaultTableModel(cartColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        cartTable = new JTable(cartTableModel);
        JScrollPane cartScrollPane = new JScrollPane(cartTable);
        cartScrollPane.setBorder(BorderFactory.createTitledBorder("Your Order"));

        // Split pane for menu and cart
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, menuScrollPane, cartScrollPane);
        splitPane.setDividerLocation(250);

        topPanel.add(splitPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.CENTER);

        // Bottom panel for actions
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Quantity and add panel
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quantityPanel.add(new JLabel("Quantity:"));

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 100, 1);
        quantitySpinner = new JSpinner(spinnerModel);
        quantityPanel.add(quantitySpinner);

        JButton addToCartButton = new JButton("Add to Cart");
        addToCartButton.addActionListener(e -> addToCart());
        quantityPanel.add(addToCartButton);

        JButton removeFromCartButton = new JButton("Remove from Cart");
        removeFromCartButton.addActionListener(e -> removeFromCart());
        quantityPanel.add(removeFromCartButton);

        bottomPanel.add(quantityPanel, BorderLayout.WEST);

        // Order panel
        JPanel orderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        orderPanel.add(totalLabel);

        JButton placeOrderButton = new JButton("Place Order");
        placeOrderButton.addActionListener(e -> placeOrder());
        orderPanel.add(placeOrderButton);

        JButton clearCartButton = new JButton("Clear Cart");
        clearCartButton.addActionListener(e -> clearCart());
        orderPanel.add(clearCartButton);

        bottomPanel.add(orderPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        // Load data
        loadMenuItems();
    }

    private void loadCanteens() {
        canteenComboBox.removeAllItems();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT ID, Name FROM Canteens ORDER BY Name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String display = rs.getString("Name") + " (ID: " + rs.getInt("ID") + ")";
                canteenComboBox.addItem(display);
            }

            // Add an "All Canteens" option
            canteenComboBox.addItem("All Canteens");
            canteenComboBox.setSelectedItem("All Canteens");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading canteens: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMenuItems() {
        menuTableModel.setRowCount(0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM Menu_Items ORDER BY ID";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("ID"));
                row.add(rs.getString("Name"));
                row.add(rs.getString("Description"));
                row.add(rs.getDouble("Price"));
                menuTableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading menu items: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addToCart() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a menu item to add",
                    "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int menuItemId = (int) menuTableModel.getValueAt(selectedRow, 0);
        String name = (String) menuTableModel.getValueAt(selectedRow, 1);
        double price = (double) menuTableModel.getValueAt(selectedRow, 3);
        int quantity = (int) quantitySpinner.getValue();
        double subtotal = price * quantity;

        // Check if item already exists in cart
        boolean found = false;
        for (int i = 0; i < cartTableModel.getRowCount(); i++) {
            if ((int) cartTableModel.getValueAt(i, 0) == menuItemId) {
                int currentQty = (int) cartTableModel.getValueAt(i, 3);
                int newQty = currentQty + quantity;
                double newSubtotal = price * newQty;

                cartTableModel.setValueAt(newQty, i, 3);
                cartTableModel.setValueAt(newSubtotal, i, 4);
                found = true;
                break;
            }
        }

        if (!found) {
            Vector<Object> row = new Vector<>();
            row.add(menuItemId);
            row.add(name);
            row.add(price);
            row.add(quantity);
            row.add(subtotal);
            cartTableModel.addRow(row);
        }

        updateTotal();
    }

    private void removeFromCart() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a cart item to remove",
                    "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        cartTableModel.removeRow(selectedRow);
        updateTotal();
    }

    private void updateTotal() {
        total = 0.0;
        for (int i = 0; i < cartTableModel.getRowCount(); i++) {
            total += (double) cartTableModel.getValueAt(i, 4);
        }
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    private void clearCart() {
        cartTableModel.setRowCount(0);
        updateTotal();
    }

    private void placeOrder() {
        if (cartTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Your cart is empty",
                    "Order Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedCanteen = (String) canteenComboBox.getSelectedItem();
        if (selectedCanteen.equals("All Canteens")) {
            JOptionPane.showMessageDialog(this, "Please select a specific canteen",
                    "Order Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to place this order?",
                "Confirm Order", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Insert order
                String orderQuery = "INSERT INTO Orders (User_id, Status) VALUES (?, 'Pending')";
                PreparedStatement orderStmt = conn.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS);
                orderStmt.setInt(1, userId);
                orderStmt.executeUpdate();

                ResultSet rs = orderStmt.getGeneratedKeys();
                int orderId = -1;
                if (rs.next()) {
                    orderId = rs.getInt(1);
                }

                // Insert order items
                String itemsQuery = "INSERT INTO Order_Items (Order_id, MenuItem_id, Quantity) VALUES (?, ?, ?)";
                PreparedStatement itemsStmt = conn.prepareStatement(itemsQuery);

                for (int i = 0; i < cartTableModel.getRowCount(); i++) {
                    itemsStmt.setInt(1, orderId);
                    itemsStmt.setInt(2, (int) cartTableModel.getValueAt(i, 0));
                    itemsStmt.setInt(3, (int) cartTableModel.getValueAt(i, 3));
                    itemsStmt.addBatch();
                }

                itemsStmt.executeBatch();
                conn.commit();

                JOptionPane.showMessageDialog(this, "Order placed successfully!");
                clearCart();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error placing order: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// My Orders Panel
class MyOrdersPanel extends JPanel {
    private int userId;
    private JTable ordersTable;
    private DefaultTableModel ordersTableModel;
    private JTable orderItemsTable;
    private DefaultTableModel orderItemsTableModel;
    private JButton refreshButton;

    public MyOrdersPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());

        // Orders table
        String[] ordersColumns = {"ID", "Status", "Order Date", "Total"};
        ordersTableModel = new DefaultTableModel(ordersColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        ordersTable = new JTable(ordersTableModel);
        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = ordersTable.getSelectedRow();
                if (selectedRow != -1) {
                    int orderId = (int) ordersTableModel.getValueAt(selectedRow, 0);
                    loadOrderItems(orderId);
                }
            }
        });

        JScrollPane ordersScrollPane = new JScrollPane(ordersTable);
        ordersScrollPane.setBorder(BorderFactory.createTitledBorder("My Orders"));

        // Order items table
        String[] itemsColumns = {"Menu Item", "Quantity", "Price", "Subtotal"};
        orderItemsTableModel = new DefaultTableModel(itemsColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        orderItemsTable = new JTable(orderItemsTableModel);
        JScrollPane itemsScrollPane = new JScrollPane(orderItemsTable);
        itemsScrollPane.setBorder(BorderFactory.createTitledBorder("Order Items"));

        // Split pane for orders and items
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, ordersScrollPane, itemsScrollPane);
        splitPane.setDividerLocation(200);

        add(splitPane, BorderLayout.CENTER);

        // Refresh button
        refreshButton = new JButton("Refresh Orders");
        refreshButton.addActionListener(e -> {
            loadOrders();
            orderItemsTableModel.setRowCount(0); // Clear order items
        });
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(refreshButton);
        add(topPanel, BorderLayout.NORTH);

        // Load data
        loadOrders();
    }

    private void loadOrders() {
        ordersTableModel.setRowCount(0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT o.ID, o.Status, o.Order_Date, " +
                    "SUM(oi.Quantity * mi.Price) as Total " +
                    "FROM Orders o " +
                    "JOIN Order_Items oi ON o.ID = oi.Order_id " +
                    "JOIN Menu_Items mi ON oi.MenuItem_id = mi.ID " +
                    "WHERE o.User_id = ? " +
                    "GROUP BY o.ID, o.Status, o.Order_Date " +
                    "ORDER BY o.Order_Date DESC";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("ID"));
                row.add(rs.getString("Status"));
                row.add(rs.getTimestamp("Order_Date"));
                row.add(rs.getDouble("Total"));
                ordersTableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadOrderItems(int orderId) {
        orderItemsTableModel.setRowCount(0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT mi.Name, oi.Quantity, mi.Price, " +
                    "(oi.Quantity * mi.Price) as Subtotal " +
                    "FROM Order_Items oi " +
                    "JOIN Menu_Items mi ON oi.MenuItem_id = mi.ID " +
                    "WHERE oi.Order_id = ?";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("Name"));
                row.add(rs.getInt("Quantity"));
                row.add(rs.getDouble("Price"));
                row.add(rs.getDouble("Subtotal"));
                orderItemsTableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading order items: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// Canteens Panel
class CanteensPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField nameField, locationField, idField;

    public CanteensPanel() {
        setLayout(new BorderLayout());

        // Create table
        String[] columns = {"ID", "Name", "Location"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
                    nameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                    locationField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Canteen Details"));

        formPanel.add(new JLabel("ID:"));
        idField = new JTextField();
        idField.setEditable(false);
        formPanel.add(idField);

        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Location:"));
        locationField = new JTextField();
        formPanel.add(locationField);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> addCanteen());

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> updateCanteen());

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteCanteen());

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearFields());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        formPanel.add(buttonPanel);

        add(formPanel, BorderLayout.SOUTH);

        // Load data
        loadCanteens();
    }

    private void loadCanteens() {
        tableModel.setRowCount(0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM Canteens ORDER BY ID";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("ID"));
                row.add(rs.getString("Name"));
                row.add(rs.getString("Location"));
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading canteens: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addCanteen() {
        String name = nameField.getText();
        String location = locationField.getText();

        if (name.isEmpty() || location.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Location are required fields",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO Canteens (Name, Location) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, name);
            pstmt.setString(2, location);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Canteen added successfully!");
                clearFields();
                loadCanteens();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add canteen",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCanteen() {
        String idStr = idField.getText();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a canteen to update",
                    "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = nameField.getText();
        String location = locationField.getText();

        if (name.isEmpty() || location.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Location are required fields",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idStr);

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE Canteens SET Name=?, Location=? WHERE ID=?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, name);
                pstmt.setString(2, location);
                pstmt.setInt(3, id);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Canteen updated successfully!");
                    clearFields();
                    loadCanteens();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update canteen",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid ID format",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCanteen() {
        String idStr = idField.getText();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a canteen to delete",
                    "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this canteen?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int id = Integer.parseInt(idStr);

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "DELETE FROM Canteens WHERE ID = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, id);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Canteen deleted successfully!");
                    clearFields();
                    loadCanteens();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete canteen",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid ID format",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        locationField.setText("");
        table.clearSelection();
    }
}