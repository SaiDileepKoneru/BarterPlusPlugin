package crashcringle.malmoserverplugin.data;


import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import crashcringle.malmoserverplugin.MalmoServerPlugin;
import crashcringle.malmoserverplugin.barterkings.trades.Trade;
import crashcringle.malmoserverplugin.barterkings.trades.TradeRequest;

public class Database {

    //final String SQL_INSERT_TRADE_REQUEST ="INSERT INTO trade_request(requester, requested, status, time_created, time_finished, game) VALUES (?, ?, ?, ?, ?, ?)";
   // final String SQL_INSERT_TRADE_EXCHANGE = "INSERT INTO trade(requestID, material, amount, offerred) VALUES (?, ?, ?, ?)";

    public Database() {
        try {
            initializeDatabase();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void initializeDatabase() throws SQLException, IOException {
        // first lets read our setup file.
        // This file contains statements to create our inital tables.
        // it is located in the resources.
        String setup;
        try (InputStream in = MalmoServerPlugin.inst().getResource("barterDb.sql")) {
            // Java 9+ way
            setup = new String(in.readAllBytes());
        } catch (IOException e) {
            MalmoServerPlugin.inst().getLogger().log(Level.SEVERE, "Could not read db setup file.", e);
            throw e;
        }
        // Mariadb can only handle a single query per statement. We need to split at ;.
        String[] queries = setup.split(";");
        // execute each query to the database.
        for (String query : queries) {
            try (Connection conn = MalmoServerPlugin.inst().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.execute();
                stmt.close();
            }
        }
        MalmoServerPlugin.inst().getLogger().info("§2Database setup complete.");
    }
    
    // public PlayerStats findPlayerStatsByUUID(String uuid) throws SQLException {

    //     PreparedStatement statement = MalmoServerPlugin.inst().getConnection().prepareStatement("SELECT * FROM player_stats WHERE uuid = ?");
    //     statement.setString(1, uuid);

    //     ResultSet resultSet = statement.executeQuery();

    //     PlayerStats playerStats;

    //     if(resultSet.next()){

    //         playerStats = new PlayerStats(resultSet.getString("uuid"), resultSet.getInt("deaths"), resultSet.getInt("kills"), resultSet.getLong("blocks_broken"), resultSet.getDouble("balance"), resultSet.getDate("last_login"), resultSet.getDate("last_logout"));

    //         statement.close();

    //         return playerStats;
    //     }

    //     statement.close();

    //     return null;
    // }

    public static void createTradeRequest(TradeRequest request) throws SQLException {

        PreparedStatement statement = MalmoServerPlugin.inst().getConnection()
                .prepareStatement("INSERT INTO trade_request(requester, requested, status, time_created, time_finished, game) VALUES (?, ?, ?, ?, ?, ?)");
        statement.setString(1, request.getRequester().getUniqueId().toString());
        statement.setString(2, request.getRequested().getUniqueId().toString());
        statement.setString(3, request.getRequestStatus().name());
        statement.setLong(4, request.getBeginTime().getTime());
        statement.setLong(5, request.getFinishTime().getTime());
        statement.setInt(6, request.getGameID());

        statement.executeUpdate();

        statement.close();

        createExchanges(request.getTrade(), request.getRequestID());
    }

    public static void createPlayer(Player player) throws SQLException {

//        PreparedStatement statement = MalmoServerPlugin.inst().getConnection()
//                .prepareStatement("INSERT INTO participants(player_uuid, username, firstName, lastName, age ) VALUES (?, ?, ?, ?, ?)");
//        statement.setString(1, player.getUniqueId().toString());
//        statement.setString(2, player.getName());
//        statement.setString(3, player.getDisplayName());
//        statement.setString(4, player.getCustomName());
//        statement.setInt(5, 20);
//
//        statement.executeUpdate();
//
//        statement.close();

    }
    public static void createExchanges(Trade trade, String requestID) throws SQLException {

//        PreparedStatement statement = MalmoServerPlugin.inst().getConnection()
//                .prepareStatement("INSERT INTO trade(requestID, material, amount, offerred) VALUES (?, ?, ?, ?)");
//        for (ItemStack item : trade.getRequestedItems()) {
//            statement.setString(1, requestID);
//            statement.setString(2, item.getType().name());
//            statement.setInt(3, item.getAmount());
//            statement.setBoolean(4, false);
//            statement.addBatch();
//        }
//        statement.executeBatch();
//        for (ItemStack item : trade.getOfferedItems()) {
//            statement.setString(1, requestID);
//            statement.setString(2, item.getType().name());
//            statement.setInt(3, item.getAmount());
//            statement.setBoolean(4, true);
//            statement.addBatch();
//        }
//        statement.executeBatch();
//
//        statement.close();

    }

    /**
     * Creates a new game in the database.
     * barter_game(num INT, duration, INT, winner, INT)
     * @param num
     * @throws SQLException
     */
    // public void createGame(BarterGame game) throws SQLException {

    //     PreparedStatement statement = MalmoServerPlugin.inst().getConnection()
    //             .prepareStatement("INSERT INTO barter_game(num, duration, winner) VALUES (?, ?, ?)");
    //     statement.setString(1, game.getID());
    //     statement.setInt(2, game.getDuration());
    //     statement.setString(3, game.getWinner().getUUID());

    //     statement.executeUpdate();

    //     statement.close();
    // }

    // public void updatePlayerStats(PlayerStats playerStats) throws SQLException {

    //     PreparedStatement statement = MalmoServerPlugin.inst().getConnection().prepareStatement("UPDATE player_stats SET deaths = ?, kills = ?, blocks_broken = ?, balance = ?, last_login = ?, last_logout = ? WHERE uuid = ?");
    //     statement.setInt(1, playerStats.getDeaths());
    //     statement.setInt(2, playerStats.getKills());
    //     statement.setLong(3, playerStats.getBlocksBroken());
    //     statement.setDouble(4, playerStats.getBalance());
    //     statement.setDate(5, new Date(playerStats.getLastLogin().getTime()));
    //     statement.setDate(6, new Date(playerStats.getLastLogout().getTime()));
    //     statement.setString(7, playerStats.getPlayerUUID());

    //     statement.executeUpdate();

    //     statement.close();

    // }

    // public void deletePlayerStats(PlayerStats playerStats) throws SQLException {

    //     PreparedStatement statement = MalmoServerPlugin.inst().getConnection().prepareStatement("DELETE FROM player_stats WHERE uuid = ?");
    //     statement.setString(1, playerStats.getPlayerUUID());

    //     statement.executeUpdate();

    //     statement.close();

    // }

}