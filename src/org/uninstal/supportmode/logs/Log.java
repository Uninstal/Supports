package org.uninstal.supportmode.logs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.uninstal.supportmode.Main;

public class Log {

	private UUID uuid;
	private String name;
	
	private String format;
	private String start;
	private String end;
	
	private List<String> chat = new ArrayList<>();
	private List<String> commands = new ArrayList<>();

	public Log(UUID uuid) {
		
		this.uuid = uuid;
		this.name = Bukkit.getPlayer(uuid).getName();
		
		Date date = new Date();
		
		this.format = "[dd.MM.yyyy, HH:mm]";
		this.start = new SimpleDateFormat(format).format(date);
	}
	
	public List<String> chat(){
		return chat;
	}
	
	public List<String> commands() {
		return commands;
	}
	
	public void set(int mode, String log) {
		
		switch (mode) {
		
		case 0:
			
			chat.add(log);
			break;
			
        case 1:
			
			commands.add(log);
			break;

		default:
			break;
		}
	}
	
	public UUID getUniqueId() {
		return uuid;
	}
	
	public String getName() {
		return name;
	}
	
	public String getStart() {
		return start;
	}
	
	public String getEnd() {
		return end;
	}
	
	public boolean isActive() {
		return end == null || end.isEmpty();
	}
	
	public void end(boolean stop) {
		
		if(!stop) {
			
			Date date = new Date();
			this.end = new SimpleDateFormat(format).format(date);
			
			LogEndEvent event = new LogEndEvent(this);
			Bukkit.getPluginManager().callEvent(event);
		}
		
		else {
			
			Date date = new Date();
			this.end = new SimpleDateFormat(format).format(date);
			
			Main main = Main.getInstance();
			Player player = Bukkit.getPlayer(uuid);
			
			List<PermissionAttachment> attachments = main.getAttachments(getUniqueId());
			attachments.forEach(att -> player.removeAttachment(att));
			
			main.removeActiveLogData(getUniqueId());
			main.removeAttachmentData(getUniqueId());
			main.addLog(this);
		}
	}
}
