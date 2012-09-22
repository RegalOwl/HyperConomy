package regalowl.hyperconomy;

import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

public class Evalue {
	Evalue(String args[], Player player, CommandSender sender, String playerecon) {
		HyperConomy hc = HyperConomy.hc;
		Calculation calc = hc.getCalculation();
		Account acc = hc.getAccount();
		SQLFunctions sf = hc.getSQLFunctions();
		ETransaction ench = hc.getETransaction();
		try {
			if (args.length == 2) {
				String nam = args[0];
				String teststring = hc.testeString(nam);
				if (teststring != null) {
					String type = args[1];
					if (type.equalsIgnoreCase("s")) {
						String[] classtype = new String[8];
						classtype[0] = "leather";
						classtype[1] = "wood";
						classtype[2] = "iron";
						classtype[3] = "chainmail";
						classtype[4] = "stone";
						classtype[5] = "gold";
						classtype[6] = "diamond";
						classtype[7] = "bow";
						int n = 0;
						sender.sendMessage(ChatColor.BLACK + "-----------------------------------------------------");
						while (n < 8) {
							double value = calc.getEnchantValue(nam, classtype[n], playerecon);
							double salestax = calc.getSalesTax(player, value);
							value = calc.twoDecimals(value - salestax);
							sender.sendMessage(ChatColor.AQUA + "" + nam + ChatColor.BLUE + " on a " + ChatColor.AQUA + "" + classtype[n] + ChatColor.BLUE + " item can be sold for: " + ChatColor.GREEN + hc.getYaml().getConfig().getString("config.currency-symbol") + value);
							n++;
						}
						sender.sendMessage(ChatColor.BLACK + "-----------------------------------------------------");
					} else if (type.equalsIgnoreCase("b")) {
						String[] classtype = new String[8];
						classtype[0] = "leather";
						classtype[1] = "wood";
						classtype[2] = "iron";
						classtype[3] = "chainmail";
						classtype[4] = "stone";
						classtype[5] = "gold";
						classtype[6] = "diamond";
						classtype[7] = "bow";
						int n = 0;
						sender.sendMessage(ChatColor.BLACK + "-----------------------------------------------------");
						while (n < 8) {
							double cost = calc.getEnchantCost(nam, classtype[n], playerecon);
							cost = cost + calc.getEnchantTax(nam, playerecon, cost);
							sender.sendMessage(ChatColor.AQUA + "" + nam + ChatColor.BLUE + " on a " + ChatColor.AQUA + "" + classtype[n] + ChatColor.BLUE + " item can be bought for: " + ChatColor.GREEN + hc.getYaml().getConfig().getString("config.currency-symbol") + cost);
							n++;
						}
						sender.sendMessage(ChatColor.BLACK + "-----------------------------------------------------");
					} else if (type.equalsIgnoreCase("a")) {
						sender.sendMessage(ChatColor.BLACK + "-----------------------------------------------------");
						sender.sendMessage(ChatColor.BLUE + "The global shop has " + ChatColor.GREEN + "" + sf.getStock(nam, playerecon) + ChatColor.AQUA + " " + nam + ChatColor.BLUE + " available.");
						sender.sendMessage(ChatColor.BLACK + "-----------------------------------------------------");
					} else {
						sender.sendMessage(ChatColor.DARK_RED + "Invalid Parameters. Use /evalue (name) ('b'/'s'/'a')");
					}
				} else {
					sender.sendMessage(ChatColor.BLUE + "Sorry, that enchantment is not in the enchantment database.");
				}
			} else if (args.length == 0 && player != null) {
				if (ench.hasenchants(player.getItemInHand())) {
					player.getItemInHand().getEnchantments().keySet().toArray();
					Iterator<Enchantment> ite = player.getItemInHand().getEnchantments().keySet().iterator();
					player.sendMessage(ChatColor.BLACK + "-----------------------------------------------------");
					double duramult = ench.getDuramult(player);
					while (ite.hasNext()) {
						String rawstring = ite.next().toString();
						String enchname = rawstring.substring(rawstring.indexOf(",") + 2, rawstring.length() - 1);
						Enchantment en = null;
						en = Enchantment.getByName(enchname);
						int lvl = player.getItemInHand().getEnchantmentLevel(en);
						String nam = hc.getenchantData(enchname);
						String fnam = nam + lvl;
						String mater = player.getItemInHand().getType().name();
						double value = calc.getEnchantValue(fnam, mater, playerecon) * duramult;
						double cost = calc.getEnchantCost(fnam, mater, playerecon);
						cost = cost + calc.getEnchantTax(fnam, playerecon, cost);
						value = calc.twoDecimals(value);
						cost = calc.twoDecimals(cost);
						double salestax = 0;
						if (hc.getYaml().getConfig().getBoolean("config.dynamic-tax.use-dynamic-tax")) {
							double moneycap = hc.getYaml().getConfig().getDouble("config.dynamic-tax.money-cap");
							double cbal = acc.getBalance(player.getName());
							if (cbal >= moneycap) {
								salestax = value * (hc.getYaml().getConfig().getDouble("config.dynamic-tax.max-tax-percent") / 100);
							} else {
								salestax = value * (cbal / moneycap);
							}
						} else {
							double salestaxpercent = hc.getYaml().getConfig().getDouble("config.sales-tax-percent");
							salestax = (salestaxpercent / 100) * value;
						}
						value = calc.twoDecimals(value - salestax);
						player.sendMessage(ChatColor.AQUA + "" + fnam + ChatColor.BLUE + " can be sold for: " + ChatColor.GREEN + hc.getYaml().getConfig().getString("config.currency-symbol") + value);
						player.sendMessage(ChatColor.AQUA + "" + fnam + ChatColor.BLUE + " can be purchased for: " + ChatColor.GREEN + hc.getYaml().getConfig().getString("config.currency-symbol") + cost);
						player.sendMessage(ChatColor.BLUE + "The global shop currently has" + ChatColor.GREEN + " " + sf.getStock(fnam, playerecon) + ChatColor.AQUA + " " + fnam + ChatColor.BLUE + " available.");
					}
					player.sendMessage(ChatColor.BLACK + "-----------------------------------------------------");
				} else {
					sender.sendMessage(ChatColor.DARK_RED + "The item you're holding has no enchantments!");
				}
			} else {
				sender.sendMessage(ChatColor.DARK_RED + "Invalid Parameters. Use /evalue (name) ('b'/'s'/'a')");
			}
		} catch (Exception e) {
			sender.sendMessage(ChatColor.DARK_RED + "Invalid Parameters. Use /evalue (name) ('b'/'s'/'a')");
		}
	}
}
