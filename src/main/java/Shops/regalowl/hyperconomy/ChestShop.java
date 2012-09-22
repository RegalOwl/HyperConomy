package regalowl.hyperconomy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ChestShop implements Listener{
	
	
	private HyperConomy hc;
	private Transaction tran;
	private Calculation calc;
	private ETransaction ench;
	private Account acc;
	private Shop s;
	
	private ArrayList<BlockFace> faces = new ArrayList<BlockFace>();
	
	
	ChestShop() {
		
		hc = HyperConomy.hc;
		tran = hc.getTransaction();
		calc = hc.getCalculation();
		ench = hc.getETransaction();
		acc = hc.getAccount();
		s = hc.getShop();
		
		faces.add(BlockFace.EAST);
		faces.add(BlockFace.WEST);
		faces.add(BlockFace.NORTH);
		faces.add(BlockFace.SOUTH);
		
		if (hc.getYaml().getConfig().getBoolean("config.use-chest-shops")) {
			hc.getServer().getPluginManager().registerEvents(this, hc);
		}
		
	}
	
	
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onSignChangeEvent(SignChangeEvent scevent) {
		if (hc.getYaml().getConfig().getBoolean("config.use-chest-shops")) {
				String line2 = ChatColor.stripColor(scevent.getLine(1)).trim();
		    	if (line2.equalsIgnoreCase("[Trade]") || line2.equalsIgnoreCase("[Buy]") || line2.equalsIgnoreCase("[Sell]")) {
		    		if (scevent.getPlayer().hasPermission("hyperconomy.chestshop")) {
		    			Block signblock = scevent.getBlock();
	        	    	org.bukkit.material.Sign msign = (org.bukkit.material.Sign)signblock.getState().getData();
	        	    	BlockFace attachedface = msign.getAttachedFace();
	        	    	Block attachedblock = signblock.getRelative(attachedface);
	        	    	Material am = attachedblock.getType();
	        	    	
		    			
		    			BlockState chestblock = signblock.getRelative(BlockFace.DOWN).getState();
		    			if (chestblock instanceof Chest) {
		    				Block cblock = chestblock.getBlock();
		    				BlockState pchest1 = cblock.getRelative(BlockFace.EAST).getState();
		    				BlockState pchest2 = cblock.getRelative(BlockFace.WEST).getState();
		    				BlockState pchest3 = cblock.getRelative(BlockFace.NORTH).getState();
		    				BlockState pchest4 = cblock.getRelative(BlockFace.SOUTH).getState();
		    				
		    				if (!(pchest1 instanceof Chest) && !(pchest2 instanceof Chest) && !(pchest3 instanceof Chest) && !(pchest4 instanceof Chest)) {
			    				s.setinShop(scevent.getPlayer());
			    				if (!hc.getYaml().getConfig().getBoolean("config.require-chest-shops-to-be-in-shop") || s.inShop() != -1) {    				
			    				Chest c = (Chest) chestblock;
			    				int count = 0;
			    				int emptyslots = 0;
			    				while (count < 27) {
			    					if (c.getInventory().getItem(count) == null) {
			    						emptyslots++;
			    					}
			    					count++;
			    				}
			    					if (emptyslots == 27) {
			    					
				    					if (am == Material.ICE || am == Material.LEAVES || am == Material.SAND || am == Material.GRAVEL || am == Material.SIGN || am == Material.SIGN_POST || am == Material.TNT) {
				    						
					    					scevent.setLine(0, "�4You can't");
					    					scevent.setLine(1, "�4attach your");
					    					scevent.setLine(2, "�4sign to that");
					    					scevent.setLine(3, "�4block!");
	
				    					} else {
					    					//probably add a check for lockette/deadbolt/lwc chests
				    						
				    						String line1 = scevent.getLine(0);
				    						if (line1.startsWith(hc.getYaml().getConfig().getString("config.currency-symbol"))) {
				    							try {
				    								String price = line1.substring(1, line1.length());
				    								Double.parseDouble(price);
				    								scevent.setLine(0, "�a" + line1);
				    							} catch (Exception e) {
				    								scevent.setLine(0, "");
				    							}
				    						}
					    					
					    					String fline = "";
					    					if (line2.equalsIgnoreCase("[Trade]")) {
					    						fline = "[Trade]";
					    					} else if (line2.equalsIgnoreCase("[Buy]")) {
					    						fline = "[Buy]";
					    					} else if (line2.equalsIgnoreCase("[Sell]")) {
					    						fline = "[Sell]";
					    					}
					    					String pname = scevent.getPlayer().getName();
					    					int nlength = pname.length();
					    					String line3 = "";
					    					String line4 = "";	
					    					if (nlength > 12) {
					    						line3 = pname.substring(0, 12);
					    						line4 = pname.substring(12, pname.length());
					    					} else {
					    						line3 = pname;
					    					}
					    					
					    					scevent.setLine(1, "�b" + fline);
							    			scevent.setLine(2, "�f" + line3);
							    			scevent.setLine(3, "�f" + line4);
				    					}

				    				} else {
				    					scevent.setLine(0, "�4You must");
				    					scevent.setLine(1, "�4use an");
				    					scevent.setLine(2, "�4empty");
				    					scevent.setLine(3, "�4chest.");
				    				}
			    				
				    			} else {
			    					scevent.setLine(0, "�4You must");
			    					scevent.setLine(1, "�4place your");
			    					scevent.setLine(2, "�4chest shop");
			    					scevent.setLine(3, "�4in a shop.");
				    			}
		    				} else {
		    					scevent.setLine(0, "�4You can't");
		    					scevent.setLine(1, "�4use a");
		    					scevent.setLine(2, "�4double");
		    					scevent.setLine(3, "�4chest.");
		    				}
		    				

		    				
		    			} else {
		    				scevent.setLine(1, "");
		    			}
		    		} else {
		    			scevent.setLine(1, "");	
		    		}
			    	if (scevent.getBlock() != null && scevent.getBlock().getType().equals(Material.SIGN_POST) || scevent.getBlock() != null && scevent.getBlock().getType().equals(Material.WALL_SIGN)) {
			    	    Sign s = (Sign) scevent.getBlock().getState();
			    	    s.update();
			    	}													    	    	
			    }

	    }
    }
	
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreakEvent(BlockBreakEvent bbevent) {
		if (hc.getYaml().getConfig().getBoolean("config.use-chest-shops")) {
			Block b = bbevent.getBlock();
					
	    	if (b != null && b.getType().equals(Material.WALL_SIGN)) {
	    	    Sign s = (Sign) b.getState();
				String line2 = s.getLine(1).trim();
		    	if (line2.equalsIgnoreCase("�b[Trade]") || line2.equalsIgnoreCase("�b[Buy]") || line2.equalsIgnoreCase("�b[Sell]")) {
					BlockState chestblock = Bukkit.getWorld(s.getBlock().getWorld().getName()).getBlockAt(s.getX(), s.getY() - 1, s.getZ()).getState();
			    	if (chestblock instanceof Chest) {
			    		if (!(ChatColor.stripColor(s.getLine(2)).trim() + ChatColor.stripColor(s.getLine(3)).trim()).equalsIgnoreCase(bbevent.getPlayer().getName()) && !bbevent.getPlayer().hasPermission("hyperconomy.admin")) {
				    		bbevent.setCancelled(true);
				    		s.update();
				    		return;
			    		} else {
			    			return;
			    		}
			    	}
		    	}
	    	} else if (b.getState() instanceof Chest) {
	    		Chest c = (Chest) b.getState();
				Block signblock = Bukkit.getWorld(c.getBlock().getWorld().getName()).getBlockAt(c.getX(), c.getY() + 1, c.getZ());
				if (signblock != null && signblock.getType().equals(Material.WALL_SIGN)) {
		    		Sign s = (Sign) signblock.getState();
					String line2 = s.getLine(1).trim();
			    	if (line2.equalsIgnoreCase("�b[Trade]") || line2.equalsIgnoreCase("�b[Buy]") || line2.equalsIgnoreCase("�b[Sell]")) {
			    		bbevent.setCancelled(true);
			    		return;
			    	}
		    	}
	    	} else {	
	    		int count = 0;
	    		while (count < 4) {		
	    			//Gets the blocks around the broken block.
	    			BlockFace cface = faces.get(count);
	            	Block relative = b.getRelative(cface);         	
	            	//If a block surrounding the broken block is a sign with they chestshop keyword it continues.
	            	if (relative.getType().equals(Material.WALL_SIGN)) {
	            		Sign s = (Sign) relative.getState();
	        			String line2 = s.getLine(1).trim();
	        	    	if (line2.equalsIgnoreCase("�b[Trade]") || line2.equalsIgnoreCase("�b[Buy]") || line2.equalsIgnoreCase("�b[Sell]")) {       	    		   			
	        	    		//Gets the material.Sign version of the sign surrounding the broken block
		        	    	org.bukkit.material.Sign sign = (org.bukkit.material.Sign)relative.getState().getData();
		        	    	BlockFace attachedface = sign.getFacing();
		        	    	if (attachedface == cface) {
		        	    		bbevent.setCancelled(true);
		        	    		return;
		        	    	}	
	        	    	}
	            	}	
	    			count++;
	    		}	     	
	    	}
		}  	
	}
	
	
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityExplodeEvent(EntityExplodeEvent eeevent) {
		if (hc.getYaml().getConfig().getBoolean("config.use-chest-shops")) {
			List<Block> blocks = eeevent.blockList();
			int count = 0;
			while (count < blocks.size()) {
				Block b= blocks.get(count);
		    	if (b != null && b.getType().equals(Material.WALL_SIGN)) {
		    	    Sign s = (Sign) b.getState();
					String line2 = s.getLine(1).trim();
			    	if (line2.equalsIgnoreCase("�b[Trade]") || line2.equalsIgnoreCase("�b[Buy]") || line2.equalsIgnoreCase("�b[Sell]")) {
						BlockState chestblock = Bukkit.getWorld(s.getBlock().getWorld().getName()).getBlockAt(s.getX(), s.getY() - 1, s.getZ()).getState();
				    	if (chestblock instanceof Chest) {
					    	eeevent.setCancelled(true);
					    	s.update();
					    	return;
				    	}
			    	}
		    	} else if (b.getState() instanceof Chest) {
		    		Chest c = (Chest) b.getState();
					Block signblock = Bukkit.getWorld(c.getBlock().getWorld().getName()).getBlockAt(c.getX(), c.getY() + 1, c.getZ());
					if (signblock != null && signblock.getType().equals(Material.WALL_SIGN)) {
			    		Sign s = (Sign) signblock.getState();
						String line2 = s.getLine(1).trim();
				    	if (line2.equalsIgnoreCase("�b[Trade]") || line2.equalsIgnoreCase("�b[Buy]") || line2.equalsIgnoreCase("�b[Sell]")) {
				    		eeevent.setCancelled(true);
				    		return;
				    	}
			    	}
		    	} else {	
		    		int count2 = 0;
		    		while (count2 < 4) {		
		    			//Gets the blocks around the broken block.
		    			BlockFace cface = faces.get(count2);
		            	Block relative = b.getRelative(cface);         	
		            	//If a block surrounding the broken block is a sign with they chestshop keyword it continues.
		            	if (relative.getType().equals(Material.WALL_SIGN)) {
		            		Sign s = (Sign) relative.getState();
		        			String line2 = s.getLine(1).trim();
		        	    	if (line2.equalsIgnoreCase("�b[Trade]") || line2.equalsIgnoreCase("�b[Buy]") || line2.equalsIgnoreCase("�b[Sell]")) {       	    		   			
		        	    		//Gets the material.Sign version of the sign surrounding the broken block
			        	    	org.bukkit.material.Sign sign = (org.bukkit.material.Sign)relative.getState().getData();
			        	    	BlockFace attachedface = sign.getFacing();
			        	    	if (attachedface == cface) {
			        	    		eeevent.setCancelled(true);
			        	    		return;
			        	    	}	
		        	    	}
		            	}	
		    			count2++;
		    		}	     	
		    	}
				count++;
			}
		}
	}
	
	
	
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPistonExtendEvent(BlockPistonExtendEvent bpeevent) {
		if (hc.getYaml().getConfig().getBoolean("config.use-chest-shops")) {
			List<Block> blocks = bpeevent.getBlocks();
			int count = 0;
			while (count < blocks.size()) {
				Block b= blocks.get(count);
		    	if (b != null && b.getType().equals(Material.WALL_SIGN)) {
		    	    Sign s = (Sign) b.getState();
					String line2 = s.getLine(1).trim();
			    	if (line2.equalsIgnoreCase("�b[Trade]") || line2.equalsIgnoreCase("�b[Buy]") || line2.equalsIgnoreCase("�b[Sell]")) {
						BlockState chestblock = Bukkit.getWorld(s.getBlock().getWorld().getName()).getBlockAt(s.getX(), s.getY() - 1, s.getZ()).getState();
				    	if (chestblock instanceof Chest) {
					    	bpeevent.setCancelled(true);
					    	s.update();
					    	return;
				    	}
			    	}
		    	} else if (b.getState() instanceof Chest) {
		    		Chest c = (Chest) b.getState();
					Block signblock = Bukkit.getWorld(c.getBlock().getWorld().getName()).getBlockAt(c.getX(), c.getY() + 1, c.getZ());
					if (signblock != null && signblock.getType().equals(Material.WALL_SIGN)) {
			    		Sign s = (Sign) signblock.getState();
						String line2 = s.getLine(1).trim();
				    	if (line2.equalsIgnoreCase("�b[Trade]") || line2.equalsIgnoreCase("�b[Buy]") || line2.equalsIgnoreCase("�b[Sell]")) {
				    		bpeevent.setCancelled(true);
				    		return;
				    	}
			    	}
		    	} else {	
		    		int count2 = 0;
		    		while (count2 < 4) {		
		    			//Gets the blocks around the broken block.
		    			BlockFace cface = faces.get(count2);
		            	Block relative = b.getRelative(cface);         	
		            	//If a block surrounding the broken block is a sign with they chestshop keyword it continues.
		            	if (relative.getType().equals(Material.WALL_SIGN)) {
		            		Sign s = (Sign) relative.getState();
		        			String line2 = s.getLine(1).trim();
		        	    	if (line2.equalsIgnoreCase("�b[Trade]") || line2.equalsIgnoreCase("�b[Buy]") || line2.equalsIgnoreCase("�b[Sell]")) {       	    		   			
		        	    		//Gets the material.Sign version of the sign surrounding the broken block
			        	    	org.bukkit.material.Sign sign = (org.bukkit.material.Sign)relative.getState().getData();
			        	    	BlockFace attachedface = sign.getFacing();
			        	    	if (attachedface == cface) {
			        	    		bpeevent.setCancelled(true);
			        	    		return;
			        	    	}	
		        	    	}
		            	}	
		    			count2++;
		    		}	     	
		    	}
				count++;
			}	
		}
	}
	
	
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPistonRetractEvent(BlockPistonRetractEvent bprevent) {
		if (hc.getYaml().getConfig().getBoolean("config.use-chest-shops")) {
			Location l = bprevent.getRetractLocation();
			Block b = l.getBlock();
			int count = 0;
			while (count < 4) {
				BlockFace cface = faces.get(count);
				Block relative = b.getRelative(cface);
				if (relative.getType().equals(Material.WALL_SIGN)) {
					Sign s = (Sign) relative.getState();
					String line2 = s.getLine(1).trim();
					if (line2.equalsIgnoreCase("�b[Trade]") || line2.equalsIgnoreCase("�b[Buy]") || line2.equalsIgnoreCase("�b[Sell]")) {
						org.bukkit.material.Sign sign = (org.bukkit.material.Sign) relative.getState().getData();
						BlockFace attachedface = sign.getFacing();
						if (attachedface == cface) {
							bprevent.setCancelled(true);
							return;
						}
					}
				}
				count++;
			}
		}
	}
	
	
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPlaceEvent(BlockPlaceEvent bpevent) {
		if (hc.getYaml().getConfig().getBoolean("config.use-chest-shops")) {
			Block b = bpevent.getBlock();
			if (b.getState() instanceof Chest) {
	    		int count = 0;
	    		while (count < 4) {		
	    			BlockFace cface = faces.get(count);
	            	Block relative = b.getRelative(cface);         	
	            	if (relative.getState() instanceof Chest) {
	            		Block signblock = relative.getRelative(BlockFace.UP);
						if (signblock != null && signblock.getType().equals(Material.WALL_SIGN)) {
				    		Sign s = (Sign) signblock.getState();
							String line2 = s.getLine(1).trim();
					    	if (line2.equalsIgnoreCase("�b[Trade]") || line2.equalsIgnoreCase("�b[Buy]") || line2.equalsIgnoreCase("�b[Sell]")) {
					    		bpevent.setCancelled(true);
					    		return;
					    	}
				    	}
	            	}	
	    			count++;
	    		}	 
			}	
		}  	
	}
	
	
	
	
	
	
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryClickEvent(InventoryClickEvent icevent) {
		if (hc.getYaml().getConfig().getBoolean("config.use-chest-shops")) {
			if (!hc.isLocked()) { 
				if (icevent.getInventory().getHolder() instanceof Chest) {
					Chest invchest = (Chest) icevent.getInventory().getHolder();
					int x = invchest.getX();
					int y = invchest.getY() + 1;
					int z = invchest.getZ();
					String world = invchest.getBlock().getWorld().getName();
					BlockState signblock = Bukkit.getWorld(world).getBlockAt(x, y, z).getState();
					if (signblock instanceof Sign) {
						Sign s = (Sign) signblock;
						String line2 = ChatColor.stripColor(s.getLine(1)).trim();
				    	if (line2.equalsIgnoreCase("[Trade]") || line2.equalsIgnoreCase("[Buy]") || line2.equalsIgnoreCase("[Sell]")) {
				    		
				    		int slot = icevent.getRawSlot();
				    		
				    		
							boolean buy = false;
							boolean sell = false;
							if (line2.equalsIgnoreCase("[Trade]")) {
								buy = true;
								sell = true;
							} else if (line2.equalsIgnoreCase("[Buy]")) {
								buy = true;
							} else if (line2.equalsIgnoreCase("[Sell]")) {
								sell = true;
							}
				    		
				    		String line34 = ChatColor.stripColor(s.getLine(2)).trim() + ChatColor.stripColor(s.getLine(3)).trim();
				    		String clicker = icevent.getWhoClicked().getName();
				    		//Handles everyone besides the owner of the chest. (make it ! when done testing)
				    		if (!clicker.equalsIgnoreCase(line34)) {
				    			
				    			if (icevent.getCurrentItem() == null) {
					    			icevent.setCancelled(true);
					    			return;
				    			}

				    			boolean setprice = false;
				    			double staticprice = 0.0;
				    			String line1 = ChatColor.stripColor(s.getLine(0)).trim();
	    						if (line1.startsWith(hc.getYaml().getConfig().getString("config.currency-symbol"))) {
	    							try {
	    								String price = line1.substring(1, line1.length());
	    								staticprice = calc.twoDecimals(Double.parseDouble(price));
	    								setprice = true;
	    							} catch (Exception e) {
	    								setprice = false;
	    							}
	    						}
				    			if (icevent.isShiftClick()) {
				    				
				    				Player p = Bukkit.getPlayer(icevent.getWhoClicked().getName());
					    			if (!ench.hasenchants(icevent.getCurrentItem())) {
					    				
						    			String key = icevent.getCurrentItem().getTypeId() + ":" + icevent.getCurrentItem().getDurability();
					    				String name = hc.getnameData(key);
					    				int id = icevent.getCurrentItem().getTypeId();
					    				int data =  icevent.getCurrentItem().getDurability();
					    				int camount = icevent.getCurrentItem().getAmount();
					    				
						    			if (slot < 27 && name != null) {
						    				
						    				if (buy) {
								    			if (setprice) {
								    				tran.buyChest(name, id, data, line34, p, camount, icevent.getView().getTopInventory(), calc.twoDecimals((camount * staticprice)));
								    			} else {
								    				tran.buyChest(name, id, data, line34, p, camount, icevent.getView().getTopInventory());
								    			}
						    				} else {
						    					p.sendMessage(ChatColor.BLUE + "You cannot purchase items from this chest.");
						    				}

						    			} else if (slot >= 27 && name != null){
						    				
						    				if (sell) {
						    					int itemamount = tran.countItems(id, data, icevent.getView().getTopInventory());
						    					
						    					if (itemamount > 0) {
								    				int space = tran.getInventoryAvailableSpace(id, data, icevent.getView().getTopInventory(), 27);
								    				if (space >= camount) {
							    						double bal = acc.getBalance(line34);
							    						double cost = calc.getTvalue(name, camount, hc.getSQLFunctions().getPlayerEconomy(line34));
							    						if (setprice) {
							    							cost = staticprice * camount;
							    						}
							    						
							    						if (bal >= cost) {
											    			if (setprice) {
											    				tran.sellChest(name, id, data, camount, line34, p, icevent.getView().getTopInventory(), calc.twoDecimals(cost));
											    			} else {
											    				tran.sellChest(name, id, data, camount, line34, p, icevent.getView().getTopInventory());
											    			}
							    						} else {
							    							p.sendMessage(ChatColor.BLUE + line34 + " doesn't have enough money for this transaction.");
							    						}
								    				} else {
								    					p.sendMessage(ChatColor.BLUE + "The chestshop doesn't have enough space.");
								    				}
						    					} else {
						    						p.sendMessage(ChatColor.BLUE + "This chest will not accept that item.");
						    					}
			
							    				
						    				} else {
						    					p.sendMessage(ChatColor.BLUE + "You cannot sell items to this chest.");
						    				}
						    				
						    				
						    			}
					    			}

					    			icevent.setCancelled(true);
					    			return;
					    			
					    			
					    		} else if (icevent.isLeftClick()) {
					    			Player p = Bukkit.getPlayer(icevent.getWhoClicked().getName());
					    			if (!ench.hasenchants(icevent.getCurrentItem())) {
						    			String key = icevent.getCurrentItem().getTypeId() + ":" + icevent.getCurrentItem().getDurability();
					    				String name = hc.getnameData(key);
					    				int id = icevent.getCurrentItem().getTypeId();
					    				int data =  icevent.getCurrentItem().getDurability();
					    				
						    			if (slot < 27 && name != null) {
						    				
						    				if (buy) {
						    					double price = calc.getTvalue(name, 1, hc.getSQLFunctions().getPlayerEconomy(line34));
						    					if (setprice) {
						    						price = staticprice;
						    					}
								    			p.sendMessage("�0-----------------------------------------------------");
								    			p.sendMessage(ChatColor.GREEN + "" + ChatColor.ITALIC + "1 " + ChatColor.AQUA + "" + ChatColor.ITALIC + name + ChatColor.BLUE + ChatColor.ITALIC + " can be purchased from " + line34 + " for: " + ChatColor.GREEN + ChatColor.ITALIC + hc.getYaml().getConfig().getString("config.currency-symbol") + price);
								    			p.sendMessage("�0-----------------------------------------------------");
						    				} else {
						    					p.sendMessage(ChatColor.BLUE + "You cannot buy items from this chest.");
						    				}
			
							    			
						    			} else if (slot >= 27 && name != null) {
						    				
						    				if (sell) {
						    					int itemamount = tran.countItems(id, data, icevent.getView().getTopInventory());
						    					
						    					if (itemamount > 0) {
						    						double price = calc.getTvalue(name, 1, hc.getSQLFunctions().getPlayerEconomy(line34));
							    					if (setprice) {
							    						price = staticprice;
							    					}
								    				p.sendMessage("�0-----------------------------------------------------");
								    				p.sendMessage(ChatColor.GREEN + "" + ChatColor.ITALIC + "1 "  + ChatColor.AQUA + ""  + ChatColor.ITALIC + name + ChatColor.BLUE + ChatColor.ITALIC + " can be sold to " + line34 + " for: " + ChatColor.GREEN + ChatColor.ITALIC + hc.getYaml().getConfig().getString("config.currency-symbol") + price);
								    				p.sendMessage("�0-----------------------------------------------------");	
						    					} else {
						    						p.sendMessage(ChatColor.BLUE + "This chest will not accept that item.");
						    					}
						    				} else {
						    					p.sendMessage(ChatColor.BLUE + "You cannot sell items to this chest.");
						    				}
			
						    			}
					    			} else {

					        			String key = icevent.getCurrentItem().getTypeId() + ":" + icevent.getCurrentItem().getDurability();
						    				String name = hc.getnameData(key);
						    				
							    			if (slot < 27 && name != null) {
							    				
							    				if (buy) {
				
							    						double price = 0;
								    					Iterator<Enchantment> ite = icevent.getCurrentItem().getEnchantments().keySet().iterator();
								        				while (ite.hasNext()) {;
								        					String rawstring = ite.next().toString();
								        					String enchname = rawstring.substring(rawstring.indexOf(",") + 2, rawstring.length() - 1);
								        					Enchantment en = null;
								        					en = Enchantment.getByName(enchname);
								        					int lvl = icevent.getCurrentItem().getEnchantmentLevel(en);
								        					String nam = hc.getenchantData(enchname);
								        					String fnam = nam + lvl;
								        					price = price + calc.getEnchantValue(fnam, p.getItemInHand().getType().toString(), hc.getSQLFunctions().getPlayerEconomy(line34));
									    					if (setprice) {
									    						price = staticprice;
									    					}
								        				}
								        				price = calc.twoDecimals(price);
								        			if (ench.isEnchantable(p.getItemInHand())) {
										    			p.sendMessage("�0-----------------------------------------------------");
										    			p.sendMessage(ChatColor.BLUE + "The selected item's enchantments can be purchased from " + line34 + " for: " + ChatColor.GREEN + ChatColor.ITALIC + hc.getYaml().getConfig().getString("config.currency-symbol") + price);
										    			p.sendMessage("�0-----------------------------------------------------");
							    					} else {
							    						p.sendMessage(ChatColor.BLUE + "That item cannot accept enchantments.");
							    					}
							    					

							    				} else {
							    					p.sendMessage(ChatColor.BLUE + "You cannot purchase enchantments from this chest.");
							    				}
				
							    				
							    			} else if (slot >= 27 && name != null) {
							    				
							    				p.sendMessage(ChatColor.BLUE + "You cannot sell enchantments here.");
							    				
							    			}
					    			}

					    			icevent.setCancelled(true);
					    			return;
					    		} else if (icevent.isRightClick()) {
					    			Player p = Bukkit.getPlayer(icevent.getWhoClicked().getName());
					    			if (!ench.hasenchants(icevent.getCurrentItem())) {
					        			String key = icevent.getCurrentItem().getTypeId() + ":" + icevent.getCurrentItem().getDurability();
					    				String name = hc.getnameData(key);
					    				int id = icevent.getCurrentItem().getTypeId();
					    				int data =  icevent.getCurrentItem().getDurability();
					    				
						    			if (slot < 27 && name != null) {
						    				
						    				if (buy) {
						    					if (setprice) {
						    						tran.buyChest(name, id, data, line34, p, 1, icevent.getView().getTopInventory(), staticprice);
						    					} else {
						    						tran.buyChest(name, id, data, line34, p, 1, icevent.getView().getTopInventory());
						    					}
								    			
						    				} else {
						    					p.sendMessage(ChatColor.BLUE + "You cannot buy items from this chest.");
						    				}
			
						    				
						    			} else if (slot >= 27 && name != null) {
						    				
						    				if (sell) {
						    					int itemamount = tran.countItems(id, data, icevent.getView().getTopInventory());
						    					
						    					if (itemamount > 0) {
							    					int space = tran.getInventoryAvailableSpace(id, data, icevent.getView().getTopInventory(), 27);
							    					if (space >= 1) {
							    						double bal = acc.getBalance(line34);
							    						double cost = calc.getTvalue(name, 1, hc.getSQLFunctions().getPlayerEconomy(line34));
							    						if (setprice) {
							    							cost = staticprice;
							    						}
							    						if (bal >= cost) {
							    							if (setprice) {
							    								tran.sellChest(name, id, data, 1, line34, p, icevent.getView().getTopInventory(), cost);
							    							} else {
							    								tran.sellChest(name, id, data, 1, line34, p, icevent.getView().getTopInventory());
							    							}
							    						} else {
							    							p.sendMessage(ChatColor.BLUE + line34 + " doesn't have enough money for this transaction.");
							    						}
							    					} else {
							    						p.sendMessage(ChatColor.BLUE + "The chest shop doesn't have enough space.");
							    					}
						    					} else {
						    						p.sendMessage(ChatColor.BLUE + "This chest will not accept that item.");
						    					}
						    				} else {
						    					p.sendMessage(ChatColor.BLUE + "You cannot sell items to this chest.");
						    				}
			
			
						    				
						    			}
					    			} else {				    				
					        			String key = icevent.getCurrentItem().getTypeId() + ":" + icevent.getCurrentItem().getDurability();
						    				String name = hc.getnameData(key);
						    				
							    			if (slot < 27 && name != null) {
							    				
							    				if (buy) {					    					
							    					Iterator<Enchantment> ite = icevent.getCurrentItem().getEnchantments().keySet().iterator();
							        				while (ite.hasNext()) {;
							        					String rawstring = ite.next().toString();
							        					String enchname = rawstring.substring(rawstring.indexOf(",") + 2, rawstring.length() - 1);
							        					Enchantment en = null;
							        					en = Enchantment.getByName(enchname);
							        					int lvl = icevent.getCurrentItem().getEnchantmentLevel(en);
							        					String nam = hc.getenchantData(enchname);
							        					String fnam = nam + lvl;
							        					if (setprice) {
							        						ench.buyChestEnchant(fnam, p, icevent.getCurrentItem(), line34, staticprice);
							        					} else {
							        						ench.buyChestEnchant(fnam, p, icevent.getCurrentItem(), line34);
							        					}
							        				}

							    				} else {
							    					p.sendMessage(ChatColor.BLUE + "You cannot buy items from this chest.");
							    				}
							    			} else if (slot >= 27 && name != null) {
							    				
							    				p.sendMessage(ChatColor.BLUE + "You cannot sell enchantments here.");
							    				
							    			}
					    			}
					
					    			
					    			
					    			icevent.setCancelled(true);
					    			return;
					    		}
				    		}		
				    	}					
					}
				}	
			} else {
				Bukkit.getPlayer(icevent.getWhoClicked().getName()).sendMessage(ChatColor.RED + "The global shop is currently locked!");
			}
		}		
	}

	
	
	
	
	
	
	

}
