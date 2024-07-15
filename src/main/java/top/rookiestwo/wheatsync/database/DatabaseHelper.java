package top.rookiestwo.wheatsync.database;

import top.rookiestwo.wheatsync.WheatSync;

import java.sql.*;
import java.util.UUID;

public class DatabaseHelper {

    private Connection connection;

    public DatabaseHelper() {
        try {
            WheatSync.LOGGER.info("Connecting to the database...");
            String url = "jdbc:mysql://" + WheatSync.CONFIG.MySQLAddress + ":" + WheatSync.CONFIG.MySQLPort + "/" + WheatSync.CONFIG.MySQLDatabase;
            this.connection = DriverManager.getConnection(url, WheatSync.CONFIG.MySQLAccount, WheatSync.CONFIG.Password);
            ensureTableExists();
        } catch (SQLException e) {
            WheatSync.LOGGER.error("Failed to connect to the database. Details:\n{}", e.getMessage());
        }
    }

    private void ensureTableExists() {
        try (Statement stmt = connection.createStatement()) {
            ResultSet resultSet = stmt.executeQuery(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" +
                            WheatSync.CONFIG.MySQLDatabase +
                            "' AND TABLE_NAME = 'sli_contents';"
            );
            if (!resultSet.next() || resultSet.getInt(1) == 0) {
                // Table does not exist, create it
                String createTableSQL =
                        "CREATE TABLE `sli_contents` (" +
                                "`player_uuid` VARCHAR(36) NOT NULL," +
                                "`communication_id` SMALLINT UNSIGNED NOT NULL," +
                                "`inventory` TEXT," +
                                "PRIMARY KEY (`player_uuid`, `communication_id`)," +
                                "CONSTRAINT `chk_communication_id_range` CHECK (`communication_id` BETWEEN 0 AND 65535)" +
                                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
                stmt.executeUpdate(createTableSQL);
                WheatSync.LOGGER.info("Table `sli_contents` created successfully.");
            }
        } catch (SQLException e) {
            WheatSync.LOGGER.error("Failed to create table exists. Details:\n{}", e.getMessage());
        }
    }

    public void insertNewSLI(UUID playerUuid, int communicationId, String inventory) {
        String sql = "INSERT INTO sli_contents (player_uuid, communication_id, inventory) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString()); // Convert UUID to String
            pstmt.setInt(2, communicationId);
            pstmt.setString(3, inventory);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            WheatSync.LOGGER.error("Failed to insert new SLI. Details:\n{}", e.getMessage());
        }
    }
}