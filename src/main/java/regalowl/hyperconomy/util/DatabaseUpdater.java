package regalowl.hyperconomy.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.configuration.file.FileConfiguration;

import regalowl.databukkit.QueryResult;
import regalowl.databukkit.SQLWrite;
import regalowl.databukkit.YamlHandler;
import regalowl.hyperconomy.HyperConomy;
import regalowl.hyperconomy.HyperEconomy;
import regalowl.hyperconomy.display.SignType;
import regalowl.hyperconomy.hyperobject.EnchantmentClass;

public class DatabaseUpdater {

	private HyperConomy hc;
	private ArrayList<String> tables = new ArrayList<String>();
	private ArrayList<Double> updateAfterLoad = new ArrayList<Double>();
	public final double version = 1.28;
	
	public DatabaseUpdater() {
		hc = HyperConomy.hc;
		tables.add("settings");
		tables.add("objects");
		tables.add("players");
		tables.add("log");
		tables.add("history");
		tables.add("audit_log");
		tables.add("shop_objects");
		tables.add("frame_shops");
		tables.add("banks");
		tables.add("shops");
		tables.add("info_signs");
		tables.add("item_displays");
		tables.add("economies");
	}
	
	
	public ArrayList<String> getTablesList() {
		return tables;
	}
	
	public double getVersion() {
		return version;
	}
	
	
	public void createTables(SQLWrite sw, boolean copydatabase) {
		if (copydatabase) {
			for (String table:tables) {
				sw.convertExecuteSynchronously("DROP TABLE IF EXISTS hyperconomy_"+table);
			}
		}
		sw.convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_settings (SETTING VARCHAR(255) NOT NULL, VALUE TEXT, TIME DATETIME NOT NULL, PRIMARY KEY (SETTING))");
		sw.convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_objects (NAME VARCHAR(255) NOT NULL, ECONOMY VARCHAR(255) NOT NULL, DISPLAY_NAME VARCHAR(255), ALIASES VARCHAR(1000), TYPE TINYTEXT, MATERIAL TINYTEXT, DATA INTEGER, DURABILITY INTEGER, VALUE DOUBLE, STATIC TINYTEXT, STATICPRICE DOUBLE, STOCK DOUBLE, MEDIAN DOUBLE, INITIATION TINYTEXT, STARTPRICE DOUBLE, CEILING DOUBLE, FLOOR DOUBLE, MAXSTOCK DOUBLE NOT NULL DEFAULT '1000000', PRIMARY KEY (NAME, ECONOMY))");
		sw.convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_players (PLAYER VARCHAR(255) NOT NULL PRIMARY KEY, ECONOMY TINYTEXT, BALANCE DOUBLE NOT NULL DEFAULT '0', X DOUBLE NOT NULL DEFAULT '0', Y DOUBLE NOT NULL DEFAULT '0', Z DOUBLE NOT NULL DEFAULT '0', WORLD TINYTEXT NOT NULL, HASH VARCHAR(255) NOT NULL DEFAULT '', SALT VARCHAR(255) NOT NULL DEFAULT '')");
		sw.convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_log (ID INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, TIME DATETIME, CUSTOMER TINYTEXT, ACTION TINYTEXT, OBJECT TINYTEXT, AMOUNT DOUBLE, MONEY DOUBLE, TAX DOUBLE, STORE TINYTEXT, TYPE TINYTEXT)");
		sw.convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_history (ID INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, OBJECT TINYTEXT, ECONOMY TINYTEXT, TIME DATETIME, PRICE DOUBLE)");
		sw.convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_audit_log (ID INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, TIME DATETIME NOT NULL, ACCOUNT TINYTEXT NOT NULL, ACTION TINYTEXT NOT NULL, AMOUNT DOUBLE NOT NULL, ECONOMY TINYTEXT NOT NULL)");
		sw.convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_shop_objects (SHOP VARCHAR(255) NOT NULL, HYPEROBJECT VARCHAR(255) NOT NULL, QUANTITY DOUBLE NOT NULL, SELL_PRICE DOUBLE NOT NULL, BUY_PRICE DOUBLE NOT NULL, MAX_STOCK INTEGER NOT NULL DEFAULT '1000000', STATUS VARCHAR(255) NOT NULL, PRIMARY KEY(SHOP, HYPEROBJECT))");
		sw.convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_frame_shops (ID INTEGER NOT NULL PRIMARY KEY, HYPEROBJECT VARCHAR(255) NOT NULL, ECONOMY TINYTEXT, SHOP VARCHAR(255), TRADE_AMOUNT INTEGER NOT NULL, X DOUBLE NOT NULL DEFAULT '0', Y DOUBLE NOT NULL DEFAULT '0', Z DOUBLE NOT NULL DEFAULT '0', WORLD TINYTEXT NOT NULL)");
		sw.convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_banks (NAME VARCHAR(255) NOT NULL PRIMARY KEY, BALANCE DOUBLE NOT NULL DEFAULT '0', OWNERS VARCHAR(255), MEMBERS VARCHAR(255))");
		sw.convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_shops (NAME VARCHAR(255) NOT NULL PRIMARY KEY, TYPE VARCHAR(255) NOT NULL, ECONOMY VARCHAR(255) NOT NULL, OWNER VARCHAR(255) NOT NULL, WORLD VARCHAR(255) NOT NULL, MESSAGE TEXT NOT NULL, BANNED_OBJECTS TEXT NOT NULL, ALLOWED_PLAYERS TEXT NOT NULL, P1X DOUBLE NOT NULL, P1Y DOUBLE NOT NULL, P1Z DOUBLE NOT NULL, P2X DOUBLE NOT NULL, P2Y DOUBLE NOT NULL, P2Z DOUBLE NOT NULL)");
		sw.convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_info_signs (WORLD VARCHAR(255) NOT NULL, X INTEGER NOT NULL, Y INTEGER NOT NULL, Z INTEGER NOT NULL, HYPEROBJECT VARCHAR(255) NOT NULL, TYPE VARCHAR(255) NOT NULL, MULTIPLIER INTEGER NOT NULL, ECONOMY VARCHAR(255) NOT NULL, ECLASS VARCHAR(255) NOT NULL, PRIMARY KEY(WORLD, X, Y, Z))");
		sw.convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_item_displays (WORLD VARCHAR(255) NOT NULL, X DOUBLE NOT NULL, Y DOUBLE NOT NULL, Z DOUBLE NOT NULL, HYPEROBJECT VARCHAR(255) NOT NULL, PRIMARY KEY(WORLD, X, Y, Z))");
		sw.convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_economies (NAME VARCHAR(255) NOT NULL PRIMARY KEY, HYPERACCOUNT VARCHAR(255) NOT NULL)");
		if (!copydatabase) {
			sw.convertExecuteSynchronously("DELETE FROM hyperconomy_settings");
			sw.convertExecuteSynchronously("INSERT INTO hyperconomy_settings (SETTING, VALUE, TIME) VALUES ('version', '"+hc.getDataManager().getDatabaseUpdater().getVersion()+"', NOW() )");
		}
	}
	
	
	
	
	
	
	public void updateTables(QueryResult qr) {
		hc = HyperConomy.hc;
		hc.getSQLRead().setErrorLogging(true);
		if (qr.next()) {
			double version = Double.parseDouble(qr.getString("VALUE"));
			if (version < 1.2) {
				//update adds hyperconomy_shop_objects table
				hc.getLogger().info("[HyperConomy]Updating HyperConomy database to version 1.2.");
				hc.getSQLWrite().convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_shop_objects (ID INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, SHOP VARCHAR(255) NOT NULL, HYPEROBJECT VARCHAR(255) NOT NULL, QUANTITY DOUBLE NOT NULL, PRICE DOUBLE NOT NULL, STATUS VARCHAR(255) NOT NULL)");
				hc.getSQLWrite().executeSynchronously("UPDATE hyperconomy_settings SET VALUE = '1.2' WHERE SETTING = 'version'");
			}
			if (version < 1.21) {
				//update removes unnecessary fields from hyperconomy_objects (id, category)
				hc.getLogger().info("[HyperConomy]Updating HyperConomy database to version 1.21.");
				hc.getSQLWrite().convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_objects_temp (NAME VARCHAR(255) NOT NULL, ECONOMY VARCHAR(255) NOT NULL, TYPE TINYTEXT, MATERIAL TINYTEXT, DATA INTEGER, DURABILITY INTEGER, VALUE DOUBLE, STATIC TINYTEXT, STATICPRICE DOUBLE, STOCK DOUBLE, MEDIAN DOUBLE, INITIATION TINYTEXT, STARTPRICE DOUBLE, CEILING DOUBLE, FLOOR DOUBLE, MAXSTOCK DOUBLE NOT NULL DEFAULT '1000000', PRIMARY KEY (NAME, ECONOMY))");
				hc.getSQLWrite().executeSynchronously("INSERT INTO hyperconomy_objects_temp (NAME, ECONOMY, TYPE, MATERIAL, DATA, DURABILITY, VALUE, STATIC, STATICPRICE, STOCK, MEDIAN, INITIATION, STARTPRICE, CEILING, FLOOR, MAXSTOCK) SELECT NAME, ECONOMY, TYPE, MATERIAL, DATA, DURABILITY, VALUE, STATIC, STATICPRICE, STOCK, MEDIAN, INITIATION, STARTPRICE, CEILING, FLOOR, MAXSTOCK FROM hyperconomy_objects");
				hc.getSQLWrite().executeSynchronously("DROP TABLE hyperconomy_objects");
				hc.getSQLWrite().convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_objects (NAME VARCHAR(255) NOT NULL, ECONOMY VARCHAR(255) NOT NULL, TYPE TINYTEXT, MATERIAL TINYTEXT, DATA INTEGER, DURABILITY INTEGER, VALUE DOUBLE, STATIC TINYTEXT, STATICPRICE DOUBLE, STOCK DOUBLE, MEDIAN DOUBLE, INITIATION TINYTEXT, STARTPRICE DOUBLE, CEILING DOUBLE, FLOOR DOUBLE, MAXSTOCK DOUBLE NOT NULL DEFAULT '1000000', PRIMARY KEY (NAME, ECONOMY))");
				hc.getSQLWrite().executeSynchronously("INSERT INTO hyperconomy_objects (NAME, ECONOMY, TYPE, MATERIAL, DATA, DURABILITY, VALUE, STATIC, STATICPRICE, STOCK, MEDIAN, INITIATION, STARTPRICE, CEILING, FLOOR, MAXSTOCK) SELECT NAME, ECONOMY, TYPE, MATERIAL, DATA, DURABILITY, VALUE, STATIC, STATICPRICE, STOCK, MEDIAN, INITIATION, STARTPRICE, CEILING, FLOOR, MAXSTOCK FROM hyperconomy_objects_temp");
				hc.getSQLWrite().executeSynchronously("DROP TABLE hyperconomy_objects_temp");
				hc.getSQLWrite().executeSynchronously("UPDATE hyperconomy_settings SET VALUE = '1.21' WHERE SETTING = 'version'");
			}
			if (version < 1.22) {
				//update adds frame shop table
				hc.getLogger().info("[HyperConomy]Updating HyperConomy database to version 1.22.");
				hc.getSQLWrite().convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_frame_shops (ID INTEGER NOT NULL PRIMARY KEY, HYPEROBJECT VARCHAR(255) NOT NULL, ECONOMY TINYTEXT, SHOP VARCHAR(255), X DOUBLE NOT NULL DEFAULT '0', Y DOUBLE NOT NULL DEFAULT '0', Z DOUBLE NOT NULL DEFAULT '0', WORLD TINYTEXT NOT NULL)");
				hc.getSQLWrite().executeSynchronously("UPDATE hyperconomy_settings SET VALUE = '1.22' WHERE SETTING = 'version'");
			}
			if (version < 1.23) {
				//update adds new fields ALIASES and DISPLAY_NAME to hyperconomy_objects, backs up composites.yml and objects.yml and replaces them with the new ones
				//and then calls an after load update to update the names, aliases, and display names in the database
				hc.getLogger().info("[HyperConomy]Updating HyperConomy database to version 1.23.");
				hc.getSQLWrite().convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_objects_temp (NAME VARCHAR(255) NOT NULL, ECONOMY VARCHAR(255) NOT NULL, DISPLAY_NAME VARCHAR(255), ALIASES VARCHAR(1000), TYPE TINYTEXT, MATERIAL TINYTEXT, DATA INTEGER, DURABILITY INTEGER, VALUE DOUBLE, STATIC TINYTEXT, STATICPRICE DOUBLE, STOCK DOUBLE, MEDIAN DOUBLE, INITIATION TINYTEXT, STARTPRICE DOUBLE, CEILING DOUBLE, FLOOR DOUBLE, MAXSTOCK DOUBLE NOT NULL DEFAULT '1000000', PRIMARY KEY (NAME, ECONOMY))");
				hc.getSQLWrite().executeSynchronously("INSERT INTO hyperconomy_objects_temp (NAME, ECONOMY, TYPE, MATERIAL, DATA, DURABILITY, VALUE, STATIC, STATICPRICE, STOCK, MEDIAN, INITIATION, STARTPRICE, CEILING, FLOOR, MAXSTOCK) SELECT NAME, ECONOMY, TYPE, MATERIAL, DATA, DURABILITY, VALUE, STATIC, STATICPRICE, STOCK, MEDIAN, INITIATION, STARTPRICE, CEILING, FLOOR, MAXSTOCK FROM hyperconomy_objects");
				hc.getSQLWrite().executeSynchronously("DROP TABLE hyperconomy_objects");
				hc.getSQLWrite().convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_objects (NAME VARCHAR(255) NOT NULL, ECONOMY VARCHAR(255) NOT NULL, DISPLAY_NAME VARCHAR(255), ALIASES VARCHAR(1000), TYPE TINYTEXT, MATERIAL TINYTEXT, DATA INTEGER, DURABILITY INTEGER, VALUE DOUBLE, STATIC TINYTEXT, STATICPRICE DOUBLE, STOCK DOUBLE, MEDIAN DOUBLE, INITIATION TINYTEXT, STARTPRICE DOUBLE, CEILING DOUBLE, FLOOR DOUBLE, MAXSTOCK DOUBLE NOT NULL DEFAULT '1000000', PRIMARY KEY (NAME, ECONOMY))");
				hc.getSQLWrite().executeSynchronously("INSERT INTO hyperconomy_objects (NAME, ECONOMY, DISPLAY_NAME, ALIASES, TYPE, MATERIAL, DATA, DURABILITY, VALUE, STATIC, STATICPRICE, STOCK, MEDIAN, INITIATION, STARTPRICE, CEILING, FLOOR, MAXSTOCK) SELECT NAME, ECONOMY, DISPLAY_NAME, ALIASES, TYPE, MATERIAL, DATA, DURABILITY, VALUE, STATIC, STATICPRICE, STOCK, MEDIAN, INITIATION, STARTPRICE, CEILING, FLOOR, MAXSTOCK FROM hyperconomy_objects_temp");
				hc.getSQLWrite().executeSynchronously("DROP TABLE hyperconomy_objects_temp");
				new Backup();
				YamlHandler yh = hc.getYamlHandler();
				yh.unRegisterFileConfiguration("composites");
				yh.unRegisterFileConfiguration("objects");
				yh.deleteConfigFile("composites");
				yh.deleteConfigFile("objects");
				yh.copyFromJar("composites");
				yh.copyFromJar("objects");
				yh.registerFileConfiguration("composites");
				yh.registerFileConfiguration("objects");
				hc.getDataManager().getDatabaseUpdater().addUpdateAfterLoad(1.23);
				hc.getSQLWrite().executeSynchronously("UPDATE hyperconomy_settings SET VALUE = '1.23' WHERE SETTING = 'version'");
			}
			if (version < 1.24) {
				//update fixes frameshop table
				hc.getLogger().info("[HyperConomy]Updating HyperConomy database to version 1.24.");
				hc.getSQLWrite().executeSynchronously("DROP TABLE hyperconomy_frame_shops");
				hc.getSQLWrite().convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_frame_shops (ID INTEGER NOT NULL PRIMARY KEY, HYPEROBJECT VARCHAR(255) NOT NULL, ECONOMY TINYTEXT, SHOP VARCHAR(255), TRADE_AMOUNT INTEGER NOT NULL, X DOUBLE NOT NULL DEFAULT '0', Y DOUBLE NOT NULL DEFAULT '0', Z DOUBLE NOT NULL DEFAULT '0', WORLD TINYTEXT NOT NULL)");
				hc.getSQLWrite().executeSynchronously("UPDATE hyperconomy_settings SET VALUE = '1.24' WHERE SETTING = 'version'");
			}
			if (version < 1.25) {
				//update adds buy/sell prices to player shops and a max stock setting
				hc.getLogger().info("[HyperConomy]Updating HyperConomy database to version 1.25.");
				hc.getSQLWrite().convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_shop_objects_temp (ID INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, SHOP VARCHAR(255) NOT NULL, HYPEROBJECT VARCHAR(255) NOT NULL, QUANTITY DOUBLE NOT NULL, SELL_PRICE DOUBLE NOT NULL, BUY_PRICE DOUBLE NOT NULL, MAX_STOCK INTEGER NOT NULL DEFAULT '1000000', STATUS VARCHAR(255) NOT NULL)");
				hc.getSQLWrite().convertExecuteSynchronously("INSERT INTO hyperconomy_shop_objects_temp (SHOP,HYPEROBJECT,QUANTITY,SELL_PRICE,BUY_PRICE,STATUS) SELECT SHOP,HYPEROBJECT,QUANTITY,PRICE,PRICE,STATUS FROM hyperconomy_shop_objects");
				hc.getSQLWrite().executeSynchronously("DROP TABLE hyperconomy_shop_objects");
				hc.getSQLWrite().convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_shop_objects (ID INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, SHOP VARCHAR(255) NOT NULL, HYPEROBJECT VARCHAR(255) NOT NULL, QUANTITY DOUBLE NOT NULL, SELL_PRICE DOUBLE NOT NULL, BUY_PRICE DOUBLE NOT NULL, MAX_STOCK INTEGER NOT NULL DEFAULT '1000000', STATUS VARCHAR(255) NOT NULL)");
				hc.getSQLWrite().convertExecuteSynchronously("INSERT INTO hyperconomy_shop_objects (SHOP,HYPEROBJECT,QUANTITY,SELL_PRICE,BUY_PRICE,MAX_STOCK,STATUS) SELECT SHOP,HYPEROBJECT,QUANTITY,SELL_PRICE,BUY_PRICE,MAX_STOCK,STATUS FROM hyperconomy_shop_objects_temp");
				hc.getSQLWrite().executeSynchronously("DROP TABLE hyperconomy_shop_objects_temp");
				hc.getSQLWrite().executeSynchronously("UPDATE hyperconomy_settings SET VALUE = '1.25' WHERE SETTING = 'version'");
			}
			if (version < 1.26) {
				//adds banks
				hc.getLogger().info("[HyperConomy]Updating HyperConomy database to version 1.26.");
				hc.getSQLWrite().convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_banks (NAME VARCHAR(255) NOT NULL PRIMARY KEY, BALANCE DOUBLE NOT NULL DEFAULT '0', OWNERS VARCHAR(255), MEMBERS VARCHAR(255))");
				hc.getSQLWrite().executeSynchronously("UPDATE hyperconomy_settings SET VALUE = '1.26' WHERE SETTING = 'version'");
			}
			if (version < 1.27) {
				//moves shops, infosigns, economies, and item displays to the database
				hc.getLogger().info("[HyperConomy]Updating HyperConomy database to version 1.27.");
				hc.getSQLWrite().convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_shops (NAME VARCHAR(255) NOT NULL PRIMARY KEY, TYPE VARCHAR(255) NOT NULL, ECONOMY VARCHAR(255) NOT NULL, OWNER VARCHAR(255) NOT NULL, WORLD VARCHAR(255) NOT NULL, MESSAGE TEXT NOT NULL, BANNED_OBJECTS TEXT NOT NULL, ALLOWED_PLAYERS TEXT NOT NULL, P1X DOUBLE NOT NULL, P1Y DOUBLE NOT NULL, P1Z DOUBLE NOT NULL, P2X DOUBLE NOT NULL, P2Y DOUBLE NOT NULL, P2Z DOUBLE NOT NULL)");
				hc.getSQLWrite().convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_info_signs (WORLD VARCHAR(255) NOT NULL, X INTEGER NOT NULL, Y INTEGER NOT NULL, Z INTEGER NOT NULL, HYPEROBJECT VARCHAR(255) NOT NULL, TYPE VARCHAR(255) NOT NULL, MULTIPLIER INTEGER NOT NULL, ECONOMY VARCHAR(255) NOT NULL, ECLASS VARCHAR(255) NOT NULL, PRIMARY KEY(WORLD, X, Y, Z))");
				hc.getSQLWrite().convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_item_displays (WORLD VARCHAR(255) NOT NULL, X DOUBLE NOT NULL, Y DOUBLE NOT NULL, Z DOUBLE NOT NULL, HYPEROBJECT VARCHAR(255) NOT NULL, PRIMARY KEY(WORLD, X, Y, Z))");	
				hc.getSQLWrite().convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_economies (NAME VARCHAR(255) NOT NULL PRIMARY KEY, HYPERACCOUNT VARCHAR(255) NOT NULL)");
				hc.getDataManager().getDatabaseUpdater().addUpdateAfterLoad(1.27);
				hc.getSQLWrite().executeSynchronously("UPDATE hyperconomy_settings SET VALUE = '1.27' WHERE SETTING = 'version'");
			}
			if (version < 1.28) {
				//removes id increment field and adds SHOP, HYPEROBJECT primary key to shop objects table to guarantee no duplicates
				hc.getLogger().info("[HyperConomy]Updating HyperConomy database to version 1.28.");
				hc.getSQLWrite().convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_shop_objects_temp (SHOP VARCHAR(255) NOT NULL, HYPEROBJECT VARCHAR(255) NOT NULL, QUANTITY DOUBLE NOT NULL, SELL_PRICE DOUBLE NOT NULL, BUY_PRICE DOUBLE NOT NULL, MAX_STOCK INTEGER NOT NULL DEFAULT '1000000', STATUS VARCHAR(255) NOT NULL, PRIMARY KEY(SHOP, HYPEROBJECT))");
				hc.getSQLWrite().executeSynchronously("INSERT INTO hyperconomy_shop_objects_temp (SHOP, HYPEROBJECT, QUANTITY, SELL_PRICE, BUY_PRICE, MAX_STOCK, STATUS) SELECT SHOP, HYPEROBJECT, QUANTITY, SELL_PRICE, BUY_PRICE, MAX_STOCK, STATUS FROM hyperconomy_shop_objects");
				hc.getSQLWrite().executeSynchronously("DROP TABLE hyperconomy_shop_objects");
				hc.getSQLWrite().convertExecuteSynchronously("CREATE TABLE IF NOT EXISTS hyperconomy_shop_objects (SHOP VARCHAR(255) NOT NULL, HYPEROBJECT VARCHAR(255) NOT NULL, QUANTITY DOUBLE NOT NULL, SELL_PRICE DOUBLE NOT NULL, BUY_PRICE DOUBLE NOT NULL, MAX_STOCK INTEGER NOT NULL DEFAULT '1000000', STATUS VARCHAR(255) NOT NULL, PRIMARY KEY(SHOP, HYPEROBJECT))");
				hc.getSQLWrite().executeSynchronously("INSERT INTO hyperconomy_shop_objects (SHOP, HYPEROBJECT, QUANTITY, SELL_PRICE, BUY_PRICE, MAX_STOCK, STATUS) SELECT SHOP, HYPEROBJECT, QUANTITY, SELL_PRICE, BUY_PRICE, MAX_STOCK, STATUS FROM hyperconomy_shop_objects_temp");
				hc.getSQLWrite().executeSynchronously("DROP TABLE hyperconomy_shop_objects_temp");
				hc.getSQLWrite().executeSynchronously("UPDATE hyperconomy_settings SET VALUE = '1.28' WHERE SETTING = 'version'");
			}
		} else {
			createTables(hc.getSQLWrite(), false);
		}
		String query = "SELECT * FROM hyperconomy_objects WHERE economy = 'default'";
		hc.getSQLRead().syncRead(hc.getDataManager(), "load2", query, null);
	}
	
	
	
	public void addUpdateAfterLoad(double version) {
		updateAfterLoad.add(version);
	}
	
	
	
	public boolean updateAfterLoad() {
		boolean restart = false;
		for (Double d:updateAfterLoad) {
			if (d.doubleValue() == 1.23) {
				hc.getLogger().info("[HyperConomy]Updating object names for version 1.23.");
				for (HyperEconomy he : hc.getDataManager().getEconomies()) {
					he.updateNamesFromYml();
				}
				restart = true;
			} else if (d.doubleValue() == 1.27) {
				hc.getLogger().info("[HyperConomy]Importing YML shops for version 1.27.");
				hc.gYH().registerFileConfiguration("shops");
				FileConfiguration sh = hc.gYH().gFC("shops");
				LanguageFile L = hc.getLanguageFile();
				Iterator<String> it = sh.getKeys(false).iterator();
				while (it.hasNext()) {
					HashMap<String,String> values = new HashMap<String,String>();
					Object element = it.next();
					String name = element.toString(); 
					String owner = sh.getString(name + ".owner");
					if (owner == null || owner == "") {
						owner = hc.getDataManager().getGlobalShopAccount().getName();
					}
					String type = "player";
					if (owner.equalsIgnoreCase(hc.getDataManager().getGlobalShopAccount().getName())) {
						type = "server";
					}
					values.put("NAME", name);
					values.put("TYPE", type);
					String economy = sh.getString(name + ".economy");
					if (economy == null || economy == "") {
						economy = "default";
					}
					values.put("ECONOMY", economy);
					values.put("OWNER", owner);
					String world = sh.getString(name + ".world");
					if (world == null || world == "") {
						world = "world";
					}
					values.put("WORLD", world);

					String message1 = sh.getString(name + ".shopmessage1");
					if (message1 == null || message1 == "") {
						message1 = "&aWelcome to "+name+"";
					}
					message1 = message1.replace("%n", name);
					String message2 = sh.getString(name + ".shopmessage2");
					if (message2 == null || message2 == "") {
						message2 = "&9Type &b/hc &9for help.";
					}
					message2 = message2.replace("%n", name);
					String message = L.get("SHOP_LINE_BREAK")+"%n"+message1+"%n"+message2+"%n"+L.get("SHOP_LINE_BREAK");
					values.put("MESSAGE", message);
					values.put("P1X", sh.getString(name + ".p1.x"));
					values.put("P1Y", sh.getString(name + ".p1.y"));
					values.put("P1Z", sh.getString(name + ".p1.z"));
					values.put("P2X", sh.getString(name + ".p2.x"));
					values.put("P2Y", sh.getString(name + ".p2.y"));
					values.put("P2Z", sh.getString(name + ".p2.z"));
					String banned = sh.getString(name + ".unavailable");
					if (banned == null) {
						banned = "";
					}
					values.put("BANNED_OBJECTS", banned);
					String allowed = sh.getString(name + ".allowed");
					if (allowed == null) {
						allowed = "";
					}
					values.put("ALLOWED_PLAYERS", allowed);
					hc.getSQLWrite().performInsert("hyperconomy_shops", values);
				}
				
				hc.gYH().registerFileConfiguration("signs");
				FileConfiguration sns = hc.gYH().gFC("signs");
				hc.getLogger().info("[HyperConomy]Importing YML info signs for version 1.27.");
				Iterator<String> iterat = sns.getKeys(false).iterator();
				while (iterat.hasNext()) {
					String signKey = iterat.next().toString();
					String key = signKey;
					String world = signKey.substring(0, signKey.indexOf("|"));
					signKey = signKey.substring(signKey.indexOf("|") + 1, signKey.length());
					int x = Integer.parseInt(signKey.substring(0, signKey.indexOf("|")));
					signKey = signKey.substring(signKey.indexOf("|") + 1, signKey.length());
					int y = Integer.parseInt(signKey.substring(0, signKey.indexOf("|")));
					signKey = signKey.substring(signKey.indexOf("|") + 1, signKey.length());
					int z = Integer.parseInt(signKey);
					
					String name = sns.getString(key + ".itemname");
					SignType type = SignType.fromString(sns.getString(key + ".type"));
					String economy = sns.getString(key + ".economy");
					EnchantmentClass enchantClass = EnchantmentClass.fromString(sns.getString(key + ".enchantclass"));
					int multiplier = sns.getInt(key + ".multiplier");
					if (multiplier < 1) {
						multiplier = 1;
					}
					HashMap<String,String> values = new HashMap<String,String>();
					values.put("WORLD", world);
					values.put("X", x+"");
					values.put("Y", y+"");
					values.put("Z", z+"");
					values.put("HYPEROBJECT", name);
					values.put("TYPE", type.toString());
					values.put("MULTIPLIER", multiplier+"");
					values.put("ECONOMY", economy);
					values.put("ECLASS", enchantClass.toString());
					hc.getSQLWrite().performInsert("hyperconomy_info_signs", values);
				}
				
				
				
				hc.gYH().registerFileConfiguration("displays");
				FileConfiguration displays = hc.gYH().gFC("displays");
				hc.getLogger().info("[HyperConomy]Importing YML item displays for version 1.27.");
				iterat = displays.getKeys(false).iterator();
				while (iterat.hasNext()) {
					String key = iterat.next().toString();
					String name = displays.getString(key + ".name");
					String world = displays.getString(key + ".world");
					String x = displays.getString(key + ".x");
					String y = displays.getString(key + ".y");
					String z = displays.getString(key + ".z");
					HashMap<String,String> values = new HashMap<String,String>();
					values.put("WORLD", world);
					values.put("X", x);
					values.put("Y", y);
					values.put("Z", z);
					values.put("HYPEROBJECT", name);
					hc.getSQLWrite().performInsert("hyperconomy_item_displays", values);
				}
				
				
				ArrayList<String> econs = hc.getSQLRead().getStringList("hyperconomy_objects", "DISTINCT ECONOMY", null);
				for (String econ:econs) {
					HashMap<String,String> values = new HashMap<String,String>();
					values.put("NAME", econ);
					values.put("HYPERACCOUNT", "hyperconomy");
					hc.getSQLWrite().performInsert("hyperconomy_economies", values);
				}
				
				
				
				restart = true;
			} else if (d.doubleValue() == 1.28) {
				hc.getLogger().info("[HyperConomy]Updating for version 1.28.");
			}
		}
		return restart;
	}
	
}