package regalowl.hyperconomy.util;

import java.util.ArrayList;
import java.util.HashMap;

import regalowl.databukkit.CommonFunctions;
import regalowl.databukkit.sql.QueryResult;
import regalowl.databukkit.sql.SQLRead;
import regalowl.databukkit.sql.SQLWrite;
import regalowl.hyperconomy.DataManager;
import regalowl.hyperconomy.HyperConomy;
import regalowl.hyperconomy.display.InfoSignHandler;
import regalowl.hyperconomy.hyperobject.EnchantmentClass;
import regalowl.hyperconomy.hyperobject.HyperObject;
import regalowl.hyperconomy.hyperobject.HyperObjectType;


/**
 * 
 * 
 * This class stores item price history in history.yml  (Value/Purchase price.)
 * 
 */
public class History {
	
	private HyperConomy hc;
	private DataManager em;
	private InfoSignHandler isign;
	private SQLWrite sw;
	private SQLRead sr;

	private long historylogtaskid;

	private int daysToSaveHistory;
	
	private long lastTime;
	private long timeCounter;
	private boolean useHistory;
	
	private final int millisecondsInHour = 3600000;
	
	public History() {
		hc = HyperConomy.hc;
		useHistory = hc.getConf().getBoolean("enable-feature.price-history-storage");
		if (!useHistory) {return;}
		em = hc.getDataManager();
		isign = hc.getInfoSignHandler();
		sw = hc.getSQLWrite();
		sr = hc.getSQLRead();
		daysToSaveHistory = hc.getConf().getInt("history.days-to-save");
		lastTime = System.currentTimeMillis();
		timeCounter = getTimeCounter();
		startTimer();
	}
	
	public boolean useHistory() {
		return useHistory;
	}
	

	public Long getTimeCounter() {
		Long value = 0L;
		QueryResult result = sr.select("SELECT VALUE FROM hyperconomy_settings WHERE SETTING = 'history_time_counter'");
		if (result.next()) {
			try {
				value = Long.parseLong(result.getString("VALUE"));
			} catch (Exception e) {
				value = 0L;
			}
		} else {
			addSetting("history_time_counter", "0");
		}
		result.close();
		return value;
	}

	public void addSetting(String setting, String value) {
		sw.addToQueue("INSERT INTO hyperconomy_settings (SETTING, VALUE, TIME) VALUES ('" + setting + "', '" + value + "', NOW() )");
	}

	public void updateSetting(String setting, String value) {
		sw.addToQueue("UPDATE hyperconomy_settings SET VALUE='" + value + "' WHERE SETTING = '" + setting + "'");
	}

	
	private void startTimer() {
		historylogtaskid = HyperConomy.mc.runRepeatingTask(new Runnable() {
			public void run() {
				long currentTime = System.currentTimeMillis();
				timeCounter += (currentTime - lastTime);
				lastTime = currentTime;
				if (timeCounter >= millisecondsInHour) {
					// if (timeCounter >= 600) {
					timeCounter = 0;
					writeHistoryThread();
					HyperConomy.mc.runTaskLater(new Runnable() {
						public void run() {
							if (isign != null) {
								isign.updateSigns();
							}
						}
					}, 1200L);
				}
				updateSetting("history_time_counter", timeCounter + "");
			}
		}, 600L, 600L);
	}
	

	
	private void writeHistoryThread() {
		ArrayList<HyperObject> objects = em.getHyperObjects();
		ArrayList<String> statements = new ArrayList<String>();
		for (HyperObject object : objects) {
			if (object.getType() == HyperObjectType.ENCHANTMENT) {
				statements.add(getWriteStatement(object.getName(), object.getEconomy(), object.getSellPrice(EnchantmentClass.DIAMOND)));
			} else if (object.getType() == HyperObjectType.ITEM) {
				statements.add(getWriteStatement(object.getName(), object.getEconomy(), object.getBuyPrice(1)));
			} else {
				statements.add(getWriteStatement(object.getName(), object.getEconomy(), object.getBuyPrice(1)));
			}
		}
		if (hc.getSQLManager().useMySQL()) {
			statements.add("DELETE FROM hyperconomy_history WHERE TIME < DATE_SUB(NOW(), INTERVAL " + daysToSaveHistory + " DAY)");
		} else {
			statements.add("DELETE FROM hyperconomy_history WHERE TIME < date('now','" + formatSQLiteTime(daysToSaveHistory * -1) + " day')");
		}
		sw.addToQueue(statements);
	}
	private String getWriteStatement(String object, String economy, double price) {
		return "INSERT INTO hyperconomy_history (OBJECT, ECONOMY, TIME, PRICE) VALUES ('" + object + "','" + economy + "', NOW() ,'" + price + "')";
	}
  	
  	
    
    public void stopHistoryLog() {
    	HyperConomy.mc.cancelTask(historylogtaskid);
    }

	public double getHistoricValue(String name, String economy, int count) {
		try {
			count -= 1;
			QueryResult result = sr.select("SELECT PRICE FROM hyperconomy_history WHERE OBJECT = '" + name + "' AND ECONOMY = '" + economy + "' ORDER BY TIME DESC");
			int c = 0;
			while (result.next()) {
				if (c == count) {
					return Double.parseDouble(result.getString("PRICE"));
				}
				c++;
			}
			result.close();
			return -1.0;
		} catch (Exception e) {
			hc.gDB().writeError(e, "getHistoricValue() passed arguments: name = '" + name + "', economy = '" + economy + "', count = '" + count + "'");
			return -1.0;
		}
	}
	
	/**
	 * This function must be called from an asynchronous thread!
	 * @param object
	 * @param timevalue
	 * @param economy
	 * @return The percentage change in theoretical price for the given object and timevalue in hours
	 */
	public synchronized String getPercentChange(HyperObject ho, int timevalue) {
		if (ho == null || sr == null) {
			hc.gDB().writeError("getPercentChange passed null HyperObject or SQLRead");
			return "?";
		}
		CommonFunctions cf = hc.getCommonFunctions();
		double percentChange = 0.0;
		double historicvalue = getHistoricValue(ho.getName(), ho.getEconomy(), timevalue);
		if (historicvalue == -1.0) {
			return "?";
		}
		if (historicvalue == 0.0) {
			return "?";
		}
		double currentvalue = 0.0;
		
		if (ho.getType() == HyperObjectType.ENCHANTMENT) {
			currentvalue = ho.getSellPrice(EnchantmentClass.DIAMOND);
		} else if (ho.getType() == HyperObjectType.ITEM) {
			currentvalue = ho.getSellPrice(1);
		} else {
			currentvalue = ho.getSellPrice(1);
		}

		percentChange = ((currentvalue - historicvalue) / historicvalue) * 100.0;
		percentChange = cf.round(percentChange, 3);
		return percentChange + "";
	}
	
	
	/**
	 * This function must be called from an asynchronous thread!
	 * @param object
	 * @param timevalue
	 * @param economy
	 * @return The percentage change in theoretical price for the given object and timevalue in hours
	 */
	public synchronized HashMap<HyperObject, String> getPercentChange(String economy, int timevalue) {
		if (sr == null) {
			return null;
		}

		HashMap<HyperObject, ArrayList<Double>> allValues = new HashMap<HyperObject, ArrayList<Double>>();
		QueryResult result = sr.select("SELECT OBJECT, PRICE FROM hyperconomy_history WHERE ECONOMY = '" + economy + "' ORDER BY TIME DESC");
		while (result.next()) {
			HyperObject ho = em.getEconomy(economy).getHyperObject(result.getString("OBJECT"));
			double price = result.getDouble("PRICE");
			if (!allValues.containsKey(ho)) {
				ArrayList<Double> values = new ArrayList<Double>();
				values.add(price);
				allValues.put(ho, values);
			} else {
				ArrayList<Double> values = allValues.get(ho);
				values.add(price);
				allValues.put(ho, values);
			}
		}
		result.close();
		
		ArrayList<HyperObject> hobjects =  em.getEconomy(economy).getHyperObjects();
		HashMap<HyperObject, String> relevantValues = new HashMap<HyperObject, String>();
		for (HyperObject ho:hobjects) {
			if (allValues.containsKey(ho)) {
				ArrayList<Double> historicValues = allValues.get(ho);
				if (historicValues.size() >= timevalue) {
					double historicValue = historicValues.get(timevalue - 1);
					double currentvalue = 0.0;
					if (ho.getType() == HyperObjectType.ENCHANTMENT) {
						currentvalue = ho.getSellPrice(EnchantmentClass.DIAMOND);
					} else if (ho.getType() == HyperObjectType.ITEM) {
						currentvalue = ho.getSellPrice(1);
					} else {
						currentvalue = ho.getSellPrice(1);
					}
					if (historicValue == 0.0) {
						relevantValues.put(ho, "?");
						continue;
					}
					double percentChange = ((currentvalue - historicValue) / historicValue) * 100.0;
					percentChange = hc.getCommonFunctions().round(percentChange, 3);
					String stringValue = percentChange + "";
					relevantValues.put(ho, stringValue);
				} else {
					relevantValues.put(ho, "?");
				}
			} else {
				relevantValues.put(ho, "?");
			}
		}
		return relevantValues;
	}

	public String formatSQLiteTime(int time) {
		if (time < 0) {
			return "-" + Math.abs(time);
		} else if (time > 0) {
			return "+" + time;
		} else {
			return "0";
		}
	}
  	
}
