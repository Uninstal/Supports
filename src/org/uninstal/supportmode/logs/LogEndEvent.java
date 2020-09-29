package org.uninstal.supportmode.logs;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LogEndEvent extends Event {

	private static HandlerList handlers = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private Log log;
	
	public LogEndEvent(Log log) {
		this.log = log;
	}
	
	public Log getLog() {
		return log;
	}
}
