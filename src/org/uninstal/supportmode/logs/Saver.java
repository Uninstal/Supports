package org.uninstal.supportmode.logs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.uninstal.supportmode.Main;

public class Saver {
	
	private String sp = System.lineSeparator();
	private String lines = 
			
			"Username: <user>" + sp +
			"Date: <start> -> <end>" + sp +
			"" + sp +
			"Chat:" + sp +
			"<chat>" + sp +
			"" + sp +
			"Commands:" + sp +
			"<commands>";

	public void save() throws IOException {
		
		Main plugin = Main.getInstance();
		List<Log> logs = plugin.getLogs();
		
		for(Log log : logs) {
			
			String way = plugin.getDataFolder() + File.separator;
			
			int i = 1;
			File file = new File(way + log.getName() + i + ".txt");
			
			while (!file.createNewFile()) {
				
				i += 1;
				file = new File(
						way + log.getName() + i + ".txt");
			}
			
			FileWriter writer = new FileWriter(file);
			
			String chat = sp + String.join(sp, log.chat());
			String commands = sp + String.join(sp, log.commands());
			
			writer.write(lines
					.replace("<user>", log.getName())
					.replace("<start>", log.getStart())
					.replace("<end>", log.getEnd())
					.replace("<chat>", chat)
					.replace("<commands>", commands));
			
			writer.close();
		}
	}
}
