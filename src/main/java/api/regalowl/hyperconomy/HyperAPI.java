package regalowl.hyperconomy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class HyperAPI implements GeneralAPI {

	public String listShops() {
		HyperConomy hc = HyperConomy.hc;
		SerializeArrayList sal = new SerializeArrayList();
		return sal.stringArrayToString(hc.getShopFactory().listShops());
	}

	public String listEconomies() {
		HyperConomy hc = HyperConomy.hc;
		SerializeArrayList sal = new SerializeArrayList();
		return sal.stringArrayToString(hc.getDataFunctions().getEconomyList());
	}

	public int getShopP1X(String shop) {
		HyperConomy hc = HyperConomy.hc;
		if (hc.getShopFactory().shopExists(shop)) {
			return hc.getShopFactory().getShop(shop).getP1x();
		} else {
			return 0;
		}
	}

	public int getShopP1Y(String shop) {
		HyperConomy hc = HyperConomy.hc;
		if (hc.getShopFactory().shopExists(shop)) {
			return hc.getShopFactory().getShop(shop).getP1y();
		} else {
			return 0;
		}
	}

	public int getShopP1Z(String shop) {
		HyperConomy hc = HyperConomy.hc;
		if (hc.getShopFactory().shopExists(shop)) {
			return hc.getShopFactory().getShop(shop).getP1z();
		} else {
			return 0;
		}
	}

	public int getShopP2X(String shop) {
		HyperConomy hc = HyperConomy.hc;
		if (hc.getShopFactory().shopExists(shop)) {
			return hc.getShopFactory().getShop(shop).getP2x();
		} else {
			return 0;
		}
	}

	public int getShopP2Y(String shop) {
		HyperConomy hc = HyperConomy.hc;
		if (hc.getShopFactory().shopExists(shop)) {
			return hc.getShopFactory().getShop(shop).getP2y();
		} else {
			return 0;
		}
	}

	public int getShopP2Z(String shop) {
		HyperConomy hc = HyperConomy.hc;
		if (hc.getShopFactory().shopExists(shop)) {
			return hc.getShopFactory().getShop(shop).getP2z();
		} else {
			return 0;
		}
	}

	public double getPlayerX(String player) {
		for (Player op:Bukkit.getOnlinePlayers()) {
			if (op.getName().equalsIgnoreCase(player)) {
				return op.getLocation().getX();
			}
		}
		HyperConomy hc = HyperConomy.hc;
		DataHandler dh = hc.getDataFunctions();
		if (dh.hasAccount(player)) {
			return dh.getHyperPlayer(player).getX();
		} else {
			return 0.0;
		}
	}

	public double getPlayerY(String player) {
		for (Player op:Bukkit.getOnlinePlayers()) {
			if (op.getName().equalsIgnoreCase(player)) {
				return op.getLocation().getY();
			}
		}
		HyperConomy hc = HyperConomy.hc;
		DataHandler dh = hc.getDataFunctions();
		if (dh.hasAccount(player)) {
			return dh.getHyperPlayer(player).getY();
		} else {
			return 0.0;
		}
	}

	public double getPlayerZ(String player) {
		for (Player op:Bukkit.getOnlinePlayers()) {
			if (op.getName().equalsIgnoreCase(player)) {
				return op.getLocation().getZ();
			}
		}
		HyperConomy hc = HyperConomy.hc;
		DataHandler dh = hc.getDataFunctions();
		if (dh.hasAccount(player)) {
			return dh.getHyperPlayer(player).getZ();
		} else {
			return 0.0;
		}
	}

	public String getPlayerShop(Player player) {
		HyperConomy hc = HyperConomy.hc;
		ShopFactory sf = hc.getShopFactory();
		ServerShop shop = sf.getShop(player);
		if (null == shop){
			return "";
		} else {
			return shop.getName();
		}
	}

	public boolean checkHash(String player, String SHA256Hash) {
		HyperConomy hc = HyperConomy.hc;
		DataHandler dh = hc.getDataFunctions();
		if (dh.hasAccount(player)) {
			if (dh.getHyperPlayer(player).getHash().equals(SHA256Hash)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	
	public String getSalt(String player) {
		HyperConomy hc = HyperConomy.hc;
		DataHandler dh = hc.getDataFunctions();
		if (dh.hasAccount(player)) {
			return dh.getHyperPlayer(player).getSalt();
		} else {
			return "";
		}
	}

	public double getAPIVersion() {
		return HyperConomy.hc.s().getApiVersion();
	}

	public String getShopEconomy(String shop) {
		HyperConomy hc = HyperConomy.hc;
		ShopFactory sf = hc.getShopFactory();
		if (sf.shopExists(shop)) {
			return sf.getShop(shop).getEconomy();
		} else {
			return "";
		}
	}

	public String getGlobalShopAccount() {
		return HyperConomy.hc.getYaml().getConfig().getString("config.global-shop-account");
	}
	
	public boolean isItemDisplay(Item item) {
		try {
			if (item == null) {
				return false;
			}
			ItemDisplayFactory idf = HyperConomy.hc.getItemDisplay();
			if (idf == null) {
				return false;
			} else {
				return idf.isDisplay(item);
			}
		} catch (Exception e) {
			new HyperError(e);
			return false;
		}
	}

}
