package org.uninstal.supportmode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.uninstal.supportmode.logs.Log;
import org.uninstal.supportmode.logs.LogEndEvent;
import org.uninstal.supportmode.logs.Saver;

public class Main extends JavaPlugin implements Listener {
	
	private Saver saver;
	
	private List<String> permissions = new ArrayList<>();
	private List<Log> logs = new ArrayList<>();
	
	private Map<UUID, Log> activeLogs = new HashMap<>();
	private Map<UUID, List<PermissionAttachment>> attachments = new HashMap<>();
	private Map<UUID, Long> cooldowns = new HashMap<>();
	
	private String dateFormat = "[HH:mm] ";
	private static Main instance;

	@Override
	public void onEnable() {
		
		instance = this;
		saver = new Saver();
		
		Files files = new Files(this);
		YamlConfiguration config = files.registerNewFile("config");
		
		permissions = config.getStringList("permissions");
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	public static Main getInstance() {
		return instance;
	}
	
	public List<PermissionAttachment> getAttachments(UUID uuid) {
		return attachments.get(uuid);
	}
	
	public void removeAttachmentData(UUID uuid) {
		this.attachments.remove(uuid);
	}
	
	public void removeActiveLogData(UUID uuid) {
		this.activeLogs.remove(uuid);
	}
	
	public void addLog(Log log) {
		this.logs.add(log);
	}
	
	public List<Log> getLogs() {
		return logs;
	}
	
	@Override
	public void onDisable() {
		
		for(Log log : activeLogs.values())
			log.end(true);
		
		try {
			
			saver.save();
		} catch (IOException e) {
			return;
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if(command.getName().equalsIgnoreCase("mod") ||
				command.getName().equalsIgnoreCase("moder")) {
			
			if(sender.hasPermission("supportmode.change")) {
				
				if(args.length == 0) {
					
					sender.sendMessage("/mod <on | off>");
					return false;
				}
				
				else {
					
					Player player = (Player) sender;
					UUID uuid = player.getUniqueId();
					
					if(args[0].equalsIgnoreCase("on")) {
						
						if(activeLogs.containsKey(uuid)) {
							
							sender.sendMessage("§cВы уже находитесь в режиме модератора!");
							return false;
						}
						
						long millis = System.currentTimeMillis();
						long cooldown = cooldowns.containsKey(uuid) ? cooldowns.get(uuid) : 0;
						
						if(millis < cooldown) {
							
							sender.sendMessage("§cУ вас стоит задержка в 30 минут");
							return false;
						}
						
						Log log = new Log(uuid);
						activeLogs.put(uuid, log);
						
						List<PermissionAttachment> att = new ArrayList<>();
						
						for(String perm : permissions) {
							
							PermissionAttachment attachment = player.addAttachment(this);
							attachment.setPermission(perm, true);
							
							att.add(attachment);
						}
						
						attachments.put(uuid, att);
						
						cooldowns.put(uuid, (long) (System.currentTimeMillis() + 30 * 60 * 1000));
						sender.sendMessage("§aВы вошли в режим модератора");
						return false;
					}
					
					else if(args[0].equalsIgnoreCase("off")) {
						
	                    if(!activeLogs.containsKey(uuid)) {
							
							sender.sendMessage("§cВы не находитесь в режиме модератора!");
							return false;
						}
	                    
	                    Log log = activeLogs.get(uuid);
						log.end(false);
						
						sender.sendMessage("§aВы вышли из режима модератора");
						return false;
					}
				}
			}
			
			else {
				
				sender.sendMessage("§cУ вас нет прав!");
				return false;
			}
		}
		
		return false;
	}
	
	@EventHandler
	public void damage(EntityDamageByEntityEvent e) {
		
		if(e.getDamager() != null && e.getDamager().getType() == EntityType.PLAYER) {
			
			Player player = (Player) e.getDamager();
			UUID uuid = player.getUniqueId();
			
			if(activeLogs.containsKey(uuid))
				e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void drop(PlayerDropItemEvent e) {
		
		Player player = e.getPlayer();
		UUID uuid = player.getUniqueId();
		
		if(activeLogs.containsKey(uuid))
			e.setCancelled(true);
	}
	
	@EventHandler
	public void chat(AsyncPlayerChatEvent e) {
		
		Player player = e.getPlayer();
		UUID uuid = player.getUniqueId();
		
		if(activeLogs.containsKey(uuid)) {
			
			String message = e.getMessage();
			String date = new SimpleDateFormat(dateFormat).format(new Date());
			
			message = date + player.getName() + ": " + message;
			activeLogs.get(uuid).set(0, message);
			
			return;
		}
	}
	
	@EventHandler
	public void command(PlayerCommandPreprocessEvent e) {
		
		Player player = e.getPlayer();
		UUID uuid = player.getUniqueId();
		
		if(activeLogs.containsKey(uuid)) {
			
			String message = e.getMessage();
			String date = new SimpleDateFormat(dateFormat).format(new Date());
			
			message = date + player.getName() + " use " + message;
			activeLogs.get(uuid).set(1, message);
			
			return;
		}
	}
	
	@EventHandler
	public void quit(PlayerQuitEvent e) {
		
		Player player = e.getPlayer();
		UUID uuid = player.getUniqueId();
		
		if(activeLogs.containsKey(uuid))
			activeLogs.get(uuid).end(false);
	}
	
	@EventHandler
	public void logEnd(LogEndEvent e) throws IOException {
		
		Log log = e.getLog();
		
		UUID uuid = log.getUniqueId();
		Player player = Bukkit.getPlayer(uuid);
		
		if(player != null) {
			
			attachments.get(uuid).forEach(perm -> player.removeAttachment(perm));
			attachments.remove(uuid);
		}
		
		logs.add(log);
		activeLogs.remove(uuid);
		
		saver.save();
		logs.clear();
	}
}
