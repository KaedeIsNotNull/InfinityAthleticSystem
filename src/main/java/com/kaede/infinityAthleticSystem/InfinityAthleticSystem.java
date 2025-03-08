package com.kaede.infinityAthleticSystem;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.UUID;

public final class InfinityAthleticSystem extends JavaPlugin {

    private Connection connection;

    @Override
    public void onEnable() {
        /**
         * プラグインのディレクトリを作成
         */
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        /**
         * Plugin startup logic
         */
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + getDataFolder() + "/InfinityAthleticSystem.db");
            InfinityAthleticTable();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        /**
         * Plugin shutdown logic
         */
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void InfinityAthleticTable() throws SQLException {
        // コマンドログテーブルを作成
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS athletic_location (" +
                    "uuid TEXT," +
                    "actionbar TEXT," +
                    "x REAL," +
                    "y REAL," +
                    "z REAL" +
                    ");");
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (label.toLowerCase()) {
            case "setathletic":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    try {
                        /**
                         * ランダムなUUIDを生成
                         */
                        UUID randomUUID = UUID.randomUUID();

                        /**
                         * 生成したUUIDをStringに変換
                         */
                        String uuid = randomUUID.toString();

                        String player_name = player.getName();
                        String actionbar = "[ステージ名：" + args[0] + "]" + " [作成：" + player_name + "]";

                        double x = player.getX();
                        BigDecimal bd = new BigDecimal(x);
                        bd = bd.setScale(1, RoundingMode.HALF_UP);  // 小数点第1位を四捨五入
                        x = bd.doubleValue();  // 四捨五入した値をxに直接代入

                        double y = player.getY();
                        bd = new BigDecimal(y);
                        bd = bd.setScale(1, RoundingMode.HALF_UP);  // 小数点第1位を四捨五入
                        y = bd.doubleValue();  // 四捨五入した値をyに直接代入

                        double z = player.getZ();
                        bd = new BigDecimal(z);
                        bd = bd.setScale(1, RoundingMode.HALF_UP);  // 小数点第1位を四捨五入
                        z = bd.doubleValue();  // 四捨五入した値をzに直接代入

                        Insert_athletic_location(uuid, actionbar, x, y, z);
                        sender.sendMessage(ChatColor.GOLD + "InfinityAthleticSystem＝＝＝登録情報＝＝＝＝＝＝＝＝＝＝＝＝＝＝");
                        sender.sendMessage(ChatColor.GOLD + "座標");
                        sender.sendMessage(ChatColor.GOLD + "X：" + x);
                        sender.sendMessage(ChatColor.GOLD + "Y：" + y);
                        sender.sendMessage(ChatColor.GOLD + "Z：" + z);
                        sender.sendMessage(ChatColor.GOLD + "actionbar：" + actionbar);

                        String uuid_hover_message = ChatColor.GOLD + "クリックでコピー：";
                        TextComponent text = new TextComponent(uuid_hover_message);
                        // UUID部分に色を付けて追加
                        TextComponent uuidText = new TextComponent(uuid);
                        uuidText.setColor(net.md_5.bungee.api.ChatColor.AQUA); // 任意の色に変更可能
                        uuidText.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid));
                        uuidText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("クリックでUUIDをコピー")));

                        text.addExtra(uuidText);

                        // ホバーテキスト付きのメッセージを送信
                        player.spigot().sendMessage(text);

                        sender.sendMessage(ChatColor.GOLD + "間違えて登録してしまった場合はdbを直接いじって消してください");
                        sender.sendMessage(ChatColor.GOLD + "＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝");
                    } catch (SQLException e) {
                        sender.sendMessage("エラーが発生しました。");
                        e.printStackTrace();
                    }
                }
                return true;
            case "randomathletictp":
                if (sender instanceof BlockCommandSender) {
                    try {
                        BlockCommandSender blockSender = (BlockCommandSender) sender;
                        Player player = getNearestPlayer(blockSender);
                        String world_name = "world";
                        random_select_athletic_location(player, world_name);
                    } catch (SQLException e) {
                        sender.sendMessage("エラーが発生しました。");
                        e.printStackTrace();
                    }
                }
        }
        return super.onCommand(sender, command, label, args);
    }

    private Player getNearestPlayer(BlockCommandSender blockSender) {
        Location blockLocation = blockSender.getBlock().getLocation();
        Player nearestPlayer = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            double distance = blockLocation.distance(player.getLocation());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestPlayer = player;
            }
        }

        return nearestPlayer;
    }


    private void Insert_athletic_location(String uuid, String actionbar, double x, double y, double z) throws SQLException {
        String sql = "INSERT INTO athletic_location (uuid, actionbar, x, y, z) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);
            statement.setString(2, actionbar);
            statement.setDouble(3, x);
            statement.setDouble(4, y);
            statement.setDouble(5, z);
            statement.executeUpdate();
        }
    }

    private void random_select_athletic_location(Player player, String world_name) throws SQLException {
        String query = "SELECT * FROM athletic_location ORDER BY RANDOM() LIMIT 1";  // Random() is a function in most databases, but it might differ based on the DBMS you’re using.
        try (Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(query)) {

            if (resultSet.next()) {
                String uuid = resultSet.getString("uuid");  // Replace "uuid_column_name" with the actual column name of the UUID
                // Retrieve other columns as needed
                String actionbar = resultSet.getString("actionbar");
                double x = resultSet.getDouble("x");
                double y = resultSet.getDouble("y");
                double z = resultSet.getDouble("z");

                // Do something with the retrieved values
                World world = Bukkit.getWorld(world_name);
                Location location = new Location(world, x ,y, z, 90, 0);

                player.teleport(location);

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionbar));

                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1);
            }
        }
    }
}
