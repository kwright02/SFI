package me.kw.sfis;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;


public class Core extends JavaPlugin implements Listener {

	public static ArrayList<Plugin> hooks;
	static SQLConnector connector;

	@Override
	public void onEnable(){
		getConfig().addDefault("sql-username", "null");
		getConfig().addDefault("sql-password", "null");
		getConfig().addDefault("sql-ip", "null");
		getConfig().addDefault("sql-database-name", "null");
		getConfig().addDefault("server-name", Bukkit.getServerName());
		getConfig().options().copyDefaults(true);
		saveConfig();
		Bukkit.getConsoleSender().sendMessage("§c==============================");
		Bukkit.getConsoleSender().sendMessage("§c=     SFI System Startup     =");
		Bukkit.getConsoleSender().sendMessage("§c==============================");
		Bukkit.getConsoleSender().sendMessage("§csfi§7$ §eChecking for hooks...");
		hooks = getHooks();
		connector = new SQLConnector(getConfig().getString("sql-ip"),getConfig().getString("sql-database-name"),getConfig().getString("sql-username"),getConfig().getString("sql-password"));
		connector.openConnection.run();
		if(hooks.contains(this)){
			hooks.remove(hooks.indexOf(this));
		}
		for(Plugin p: hooks){
			if(p.getDataFolder() == this.getDataFolder()){
				continue;
			}
			Bukkit.getConsoleSender().sendMessage("§csfi§7$ §aFound " + p.getName() + "! Loading it...");
		}
		if(hooks.size() > 0){
			Bukkit.getConsoleSender().sendMessage("§csfi§7$ §aAll found hooks were loaded!");
		} else{
			Bukkit.getConsoleSender().sendMessage("§csfi§7$ §eNo hooks found, consider purchasing some!");
		}
		Bukkit.getConsoleSender().sendMessage("§csfi§7$ §eAttempting to login to SQL Database...");
		if(!connector.checkDetails(true)){
			Bukkit.getConsoleSender().sendMessage("§csfi§7$ §aLogged into SQL Database!");
		}
		Bukkit.getConsoleSender().sendMessage("§csfi§7$ §eGrabbing server ip and port...");
		String ip = Bukkit.getServer().getIp();
		if(ip.length() == 0){
			ip = "localhost";
		}
		String port = ":" + Bukkit.getServer().getPort();
		Bukkit.getConsoleSender().sendMessage("§csfi§7$ §3IP: " + ip + port);
		if(!connector.checkDetails(false)){
			java.sql.DatabaseMetaData dbm;
			Bukkit.getConsoleSender().sendMessage("§csfi§7$ §eChecking to make sure necessary tables exist...");
			try {
				//			System.out.println("DEBUG --- " + (connector.connection == null));
				dbm = connector.connection.getMetaData();
				ResultSet tables = dbm.getTables(null, null, "ServerRegistry", null);
				if (tables.next()) {
					Bukkit.getConsoleSender().sendMessage("§csfi§7$ §aServerRegistry table exists, regestering this server!");
					if(SQLQuerier.safeQuery("SELECT * FROM `ServerRegistry` where `ip` ='" + ip + "' AND `port`=" + Bukkit.getServer().getPort() + ";").next()){
						SQLQuerier.safeUpdate("UPDATE `ServerRegistry` SET `running`=1 WHERE `ip`='" + ip + "' AND port=" + Bukkit.getServer().getPort() + ";");
					} else {
						SQLQuerier.safeUpdate("INSERT INTO `ServerRegistry` (`id`, `name`, `ip`, `port`, `running`) VALUES (0,'" + getConfig().getString("server-name") + "', '" + ip + "', " + Bukkit.getServer().getPort() + ", 1);");
					}
				} else {
					SQLQuerier.safeUpdate("CREATE TABLE ServerRegistry (id INT(12) NOT NULL AUTO_INCREMENT,name TEXT(100),ip TEXT(32),port INT(9), running INT(1),PRIMARY KEY (`id`));");
				}
				tables = dbm.getTables(null, null, "PlayerData", null);
				if (tables.next()) {
					Bukkit.getConsoleSender().sendMessage("§csfi§7$ §aPlayerData table exists!");
				} else {
					SQLQuerier.safeUpdate("CREATE TABLE PlayerData (id INT(8) NOT NULL AUTO_INCREMENT,name TEXT(100),uuid TEXT(100),permissions TEXT(10000),friends TEXT(100000),logged_on INT(1), PRIMARY KEY (`id`));");
				}
				tables = dbm.getTables(null, null, "RemotePannelUsers", null);
				if (tables.next()) {
					Bukkit.getConsoleSender().sendMessage("§csfi§7$ §aRemotePannelUsers table exists!");
				} else {
					SQLQuerier.safeUpdate("CREATE TABLE RemotePannelUsers (id INT(8) NOT NULL AUTO_INCREMENT,name TEXT(32), password TEXT(16), permission_level INT(7),logged_on INT(1), PRIMARY KEY (`id`));");
				}
				Bukkit.getConsoleSender().sendMessage("§csfi§7$ §aAll necessary tables exist!");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			Bukkit.getConsoleSender().sendMessage("§csfi§7$ §cIgnoring SQL Initiation Sequence due to no attachement (No database credentials provided)...");
		}
		Runnable fetchCommands = new Runnable(){
			@Override
			public void run() {
				try {
					String ip = Bukkit.getServer().getIp();
					if(ip.length() == 0){
						ip = "localhost";
					}
//					Bukkit.getConsoleSender().sendMessage("§csfi§7$ §9Checking for recieved commands!");
					ResultSet set = SQLQuerier.safeQuery("SELECT * FROM `ServerRegistry` WHERE `ip`='" + ip + "' AND `port`=" + Bukkit.getServer().getPort() + ";");
					set.next();
					int command = set.getInt("running");
//					Bukkit.getConsoleSender().sendMessage("§csfi§7$ §9Command=" + command);
					switch(command){
					case 2:
						Bukkit.getConsoleSender().sendMessage("§csfi§7$ §9Recieved stop command from desktop status menu!");
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
						SQLQuerier.safeUpdate("UPDATE `ServerRegistry` SET `running`=1 WHERE `ip`='" + ip +"' AND `port`=" + Bukkit.getServer().getPort() + ";");
						break;
					case 3:
						Bukkit.getConsoleSender().sendMessage("§csfi§7$ §9Recieved reload command from desktop status menu!");
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rl");
						SQLQuerier.safeUpdate("UPDATE `ServerRegistry` SET `running`=1 WHERE `ip`='" + ip +"' AND `port`=" + Bukkit.getServer().getPort() + ";");
						break;
					default:
						break;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

			}
		};
		Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, fetchCommands, 0, 20*5);
		this.getCommand("sfi").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
				if(sender instanceof ConsoleCommandSender) {
					if(args.length == 1) {
						if(args[0].equals("up")) {
							Bukkit.getConsoleSender().sendMessage("§csfi§7$ §eYou need to specify what hook to start!\n Hooks: " + getHooks().toString());
							return true;
						}
					} else if(args.length == 2) {
						if(args[0].equals("up")) {
							try {
								Bukkit.getConsoleSender().sendMessage("§csfi§7$ §eAttempting to load " + args[1] + "...");
								Bukkit.getPluginManager().loadPlugin(new File(Bukkit.getUpdateFolderFile().getParentFile().getPath() + "/" + args[1] + ".jar"));
								Bukkit.getPluginManager().getPlugin(args[1]).onEnable();
								Bukkit.getConsoleSender().sendMessage("§csfi§7$ §aLoaded " + args[1] + "! ");
								return true;
							} catch (UnknownDependencyException | InvalidPluginException | InvalidDescriptionException e) {
								e.printStackTrace();
								return true;
							}
						}
					}
				}
				return false;
			}
			
		});
	}

	@Override
	public void onDisable(){
		if(!connector.checkDetails(false)){
			String ip = Bukkit.getServer().getIp();
			if(ip.length() == 0){
				ip = "localhost";
			}
			SQLQuerier.safeUpdate("UPDATE `ServerRegistry` SET `running`=0 WHERE `ip`='" + ip +"' AND `port`=" + Bukkit.getServer().getPort());
		}
	}

	public ArrayList<Plugin> getHooks(){
		ArrayList<Plugin> hooks = new ArrayList<>();
		for(Plugin p: Bukkit.getPluginManager().getPlugins()){
			if(p.getName().contains("SFIS") && p != this) {
				hooks.add(p);
			}
		}
		return hooks;
	}

	public static SQLConnector getConnection(){
		return connector;
	}

}
