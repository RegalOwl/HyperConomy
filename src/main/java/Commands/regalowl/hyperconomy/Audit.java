package regalowl.hyperconomy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Audit {
	
	Audit(String args[], CommandSender sender) {
		HyperConomy hc = HyperConomy.hc;
		LanguageFile L = hc.getLanguageFile();
		Account acc = hc.getAccount();
		try {
			String account = args[0];
			if (!acc.checkAccount(account)) {
				sender.sendMessage(L.get("ACCOUNT_NOT_FOUND"));
				return;
			}
			sender.sendMessage(L.get("LINE_BREAK"));
			double cbalance = acc.getBalance(account);
			sender.sendMessage("Current Balance: " + cbalance);
			double logbalance = getHyperLogTotal(account, "sale") - getHyperLogTotal(account, "purchase");
			sender.sendMessage("Theoretical Balance condsidering sales/purchases: " + logbalance);
			double auditbalance = getAuditLogTotal(account);
			sender.sendMessage("Theoretical Balance condsidering everything: " + auditbalance);
			sender.sendMessage(L.get("LINE_BREAK"));

		} catch (Exception e) {
			sender.sendMessage(L.get("AUDIT_INVALID"));
		}
	}
	
	
	
	public Double getHyperLogTotal(String account, String type) {
		HyperConomy hc = HyperConomy.hc;
		SQLRead sr = hc.getSQLRead();
		String query = "";
		if (type.equalsIgnoreCase("sale")) {
			query = "SELECT SUM(MONEY) AS total FROM hyperconomy_log WHERE CUSTOMER = '" + account + "' AND ACTION = 'sale'";
		} else if (type.equalsIgnoreCase("purchase")) {
			query = "SELECT SUM(MONEY) AS total FROM hyperconomy_log WHERE CUSTOMER = '" + account + "' AND ACTION = 'purchase'";
		}
		QueryResult result = sr.getDatabaseConnection().read(query);
		double amount = 0.0;
		if (result.next()) {
			amount = result.getDouble("total");
		}
		result.close();
		return amount;
	}
	
	
	public Double getAuditLogTotal(String account) {
		HyperConomy hc = HyperConomy.hc;
		SQLRead sr = hc.getSQLRead();
		QueryResult result = sr.getDatabaseConnection().read("SELECT * FROM hyperconomy_audit_log WHERE ACCOUNT = '" + account + "' ORDER BY TIME ASC");
		double tBalance = 0.0;
		//double lastSetBalance = -1;
		while (result.next()) {
			String action = result.getString("ACTION");
			double amount = result.getDouble("AMOUNT");
			if (action.equalsIgnoreCase("deposit")) {
				tBalance += amount;
			} else if (action.equalsIgnoreCase("withdrawal")) {
				tBalance -= amount;
			} else if (action.equalsIgnoreCase("setbalance")) {
				tBalance = amount;
			} else if (action.equalsIgnoreCase("initialization")) {
				tBalance = amount;
			}
		}
		result.close();
		return tBalance;
	}
	
}