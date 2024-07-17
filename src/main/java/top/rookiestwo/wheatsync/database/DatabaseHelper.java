package top.rookiestwo.wheatsync.database;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import top.rookiestwo.wheatsync.WheatSync;
import top.rookiestwo.wheatsync.database.requests.*;

import java.sql.*;
import java.util.Queue;
import java.util.UUID;

public class DatabaseHelper {

    private Connection connection;
    private static final String UPDATE_INVENTORY_SQL = "UPDATE sli_contents SET inventory = ? WHERE player_uuid = ? AND communication_id = ?";
    private static final String INSERT_SLI_SQL = "INSERT INTO sli_contents (player_uuid, communication_id, inventory) VALUES (?, ?, ?)";
    private static final String DELETE_SLI_SQL = "DELETE FROM sli_contents WHERE player_uuid = ? AND communication_id = ?";
    private static final String UPDATE_COMMUNICATION_ID_SQL = "UPDATE sli_contents SET communication_id = ? WHERE player_uuid = ? AND communication_id = ?";
    private static final String GET_SLI_SQL = "SELECT * FROM sli_contents WHERE player_uuid = ? AND communication_id = ?";

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

    public void processUpdateInventoryRequests(Queue<UpdateInventoryRequest> requests) throws SQLException {

        if (requests.isEmpty()) return;
        try (PreparedStatement pstmt = connection.prepareStatement(UPDATE_INVENTORY_SQL)) {
            WheatSync.LOGGER.info("Update Inventory Request");
            UpdateInventoryRequest request;
            while ((request = requests.poll()) != null) {
                pstmt.setString(1, request.newInventory);
                pstmt.setString(2, request.playerUUID.toString());
                pstmt.setInt(3, request.communicationID);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    public void processCreateSLIRequests(Queue<CreateSLIRequest> requests) throws SQLException {

        if (requests.isEmpty()) return;
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_SLI_SQL)) {
            WheatSync.LOGGER.info("Create SLI Request");
            CreateSLIRequest request;
            while ((request = requests.poll()) != null) {
                pstmt.setString(1, request.playerUUID.toString());
                pstmt.setInt(2, request.communicationID);
                pstmt.setString(3, request.inventory);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    public void processDeleteSLIRequests(Queue<DeleteSLIRequest> requests) throws SQLException {

        if (requests.isEmpty()) return;
        try (PreparedStatement pstmt = connection.prepareStatement(DELETE_SLI_SQL)) {
            WheatSync.LOGGER.info("Delete SLI Request");
            DeleteSLIRequest request;
            while ((request = requests.poll()) != null) {
                pstmt.setString(1, request.playerUUID.toString());
                pstmt.setInt(2, request.communicationID);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    public void processChangeCommunicationIDRequests(Queue<ChangeCommunicationIDRequest> requests) throws SQLException {

        if (requests.isEmpty()) return;
        try (PreparedStatement pstmt = connection.prepareStatement(UPDATE_COMMUNICATION_ID_SQL)) {
            WheatSync.LOGGER.info("Change Communication Request");
            ChangeCommunicationIDRequest request;
            while ((request = requests.poll()) != null) {
                pstmt.setInt(1, request.newCommunicationID);
                pstmt.setString(2, request.playerUUID.toString());
                pstmt.setInt(3, request.communicationID);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    public void processGetSLIRequests(Queue<GetSLIRequest> requests) throws SQLException {

        if (requests.isEmpty()) return;
        try (PreparedStatement pstmt = connection.prepareStatement(GET_SLI_SQL)) {
            //WheatSync.LOGGER.info("Get SLI Request");
            GetSLIRequest request;
            while ((request = requests.poll()) != null) {
                pstmt.setString(1, request.playerUUID.toString());
                pstmt.setInt(2, request.communicationID);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    WheatSync.sliCache.updateSLIInventoryCache(
                            UUID.fromString(rs.getString("player_uuid")),
                            rs.getInt("communication_id"),
                            rs.getString("inventory")
                    );
                }
            }
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void fetchAllFromTable(String tableName) {

        String query = "SELECT * FROM sli_contents";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {

            }
        } catch (SQLException e) {
            WheatSync.LOGGER.error("从表 " + tableName + " 抓取数据失败。详情：\n{}", e.getMessage());
        }
    }
}