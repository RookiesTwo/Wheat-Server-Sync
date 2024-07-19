package top.rookiestwo.wheatsync.database;

import top.rookiestwo.wheatsync.WheatSync;
import top.rookiestwo.wheatsync.database.requests.UpdateInventoryRequest;

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
            ensureColumnExists(WheatSync.CONFIG.ServerName);
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

    public void ensureColumnExists(String columnName) {
        try (Statement stmt = connection.createStatement()) {
            // 检查列是否存在
            ResultSet resultSet = stmt.executeQuery(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '" +
                            WheatSync.CONFIG.MySQLDatabase +
                            "' AND TABLE_NAME = 'sli_contents' AND COLUMN_NAME = '" + columnName + "';"
            );
            if (!resultSet.next() || resultSet.getInt(1) == 0) {
                String alterTableSQL = "ALTER TABLE `sli_contents` ADD COLUMN `" + columnName + "` BOOLEAN DEFAULT FALSE;";
                stmt.executeUpdate(alterTableSQL);
                WheatSync.LOGGER.info("列 `" + columnName + "` 已成功添加到 `sli_contents` 表中。");
            }
        } catch (SQLException e) {
            WheatSync.LOGGER.error("检查或创建列失败。详情:\n{}", e.getMessage());
        }
    }

    //将本服务器存在的数据库内容器信息写入缓存
    public void loadSLIEntitiesFromDatabaseToCache() {
        String serverNameColumn = WheatSync.CONFIG.ServerName;
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT player_uuid, communication_id, inventory FROM sli_contents WHERE `" + serverNameColumn.toLowerCase() + "` = TRUE")) {
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                UUID playerUUID = UUID.fromString(resultSet.getString("player_uuid"));
                int communicationID = resultSet.getInt("communication_id");
                String inventory = resultSet.getString("inventory");
                boolean isOnOtherServer = ifOnOtherServer(resultSet);

                WheatSync.sliCache.addOrUpdateSLICache(playerUUID, communicationID, inventory, isOnOtherServer);
            }
        } catch (SQLException e) {
            WheatSync.LOGGER.error("Failed to load SLIEntities from database. Details:\n{}", e.getMessage());
        }
    }

    //在主线程中调用的小查询
    public boolean ifSLIExists(UUID playerUUID, int newID) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT COUNT(*) FROM sli_contents WHERE player_uuid = ? AND communication_id = ?")) {
            stmt.setString(1, playerUUID.toString());
            stmt.setInt(2, newID);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) > 0;
        } catch (SQLException e) {
            WheatSync.LOGGER.error("Failed to check if SLI exists. Details:\n{}", e.getMessage());
            return false;
        }
    }

    public void getSLIToCache(UUID playerUUID, int communicationID) {
        // 如果缓存中没有SLI状态，从数据库查询并检查是否在其他服务器上
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT inventory FROM sli_contents WHERE player_uuid = ? AND communication_id = ?")) {
            stmt.setString(1, playerUUID.toString());
            stmt.setInt(2, communicationID);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                String inventory = resultSet.getString("inventory");
                boolean isOnOtherServer = ifOnOtherServer(resultSet);
                // 更新缓存
                WheatSync.sliCache.addOrUpdateSLICache(playerUUID, communicationID, inventory, isOnOtherServer);
            }
        } catch (SQLException e) {
            WheatSync.LOGGER.error("Failed to get SLI to cache. Details:\n{}", e.getMessage());
        }
    }

    //给数据库添加一条的容器记录
    public void createSLIRecord(UUID playerUUID, int communicationID, String inventory) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO sli_contents (player_uuid, communication_id, inventory, `" + WheatSync.CONFIG.ServerName + "`) VALUES (?, ?, ?, TRUE)")) {
            stmt.setString(1, playerUUID.toString());
            stmt.setInt(2, communicationID);
            stmt.setString(3, inventory);
            stmt.executeUpdate();
        } catch (SQLException e) {
            WheatSync.LOGGER.error("Failed to create SLI record. Details:\n{}", e.getMessage());
        }
    }

    public void updateSLIServerStatus(UUID playerUUID, int communicationID, boolean status) {
        String serverNameColumn = WheatSync.CONFIG.ServerName;
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE sli_contents SET `" + serverNameColumn + "` = ? WHERE player_uuid = ? AND communication_id = ?")) {
            stmt.setBoolean(1, status);
            stmt.setString(2, playerUUID.toString());
            stmt.setInt(3, communicationID);

            stmt.executeUpdate();
        } catch (SQLException e) {
            WheatSync.LOGGER.error("Failed to update SLI server status. Details:\n{}", e.getMessage());
        }
    }

    public void deleteSLIRecord(UUID playerUUID, int communicationID) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM sli_contents WHERE player_uuid = ? AND communication_id = ?")) {
            stmt.setString(1, playerUUID.toString());
            stmt.setInt(2, communicationID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            WheatSync.LOGGER.error("Failed to delete SLI record. Details:\n{}", e.getMessage());
        }
    }

    public void processUpdateInventoryQueue() {
        if (SLICache.updateInventoryRequestQueue.isEmpty()) return;
        UpdateInventoryRequest request = null;
        try (PreparedStatement pstmt = connection.prepareStatement("UPDATE sli_contents SET inventory = ? WHERE player_uuid = ? AND communication_id = ?")) {
            WheatSync.LOGGER.info("Update Inventory Request");
            while ((request = SLICache.updateInventoryRequestQueue.poll()) != null) {
                pstmt.setString(1, request.newInventory);
                pstmt.setString(2, request.playerUUID.toString());
                pstmt.setInt(3, request.communicationID);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //是否在在其他服务器存在
    private boolean ifOnOtherServer(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 4; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            if (!columnName.equalsIgnoreCase(WheatSync.CONFIG.ServerName) && resultSet.getBoolean(columnName)) {
                return true;
            }
        }
        return false;
    }

}