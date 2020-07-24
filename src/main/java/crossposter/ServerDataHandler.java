package crossposter;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import crossposter.CrossPoster.EventHandler;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;

/**
 * @author Luke Tonon
 */
public final class ServerDataHandler {
	private static final Gson GSON = new Gson();
	private static final String DIRECTORY = "/root/cross-poster/data";
	private static final Map<String, ServerData> SERVER_DATA = Collections.synchronizedMap(new HashMap<String, ServerData>());
	
	public static void initialize() {
		File[] files = new File(DIRECTORY).listFiles();
		for (File file : files) {
			try {
				SERVER_DATA.put(file.getName(), GSON.fromJson(new String(Files.readAllBytes(Paths.get(file.getPath()))), ServerData.class));
			} catch (JsonSyntaxException | IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Cross Poster Info:" + "\n" + "Loaded " + files.length + " Server Data Files");
	}
	
	public static ServerData getServerData(String serverId) {
		synchronized (SERVER_DATA) {
			return SERVER_DATA.get(serverId);
		}
	}
	
	public static void writeChannel(String serverId, ChannelData channelData) {
		synchronized (SERVER_DATA) {
			ServerData currentData = SERVER_DATA.get(serverId);
			currentData.channelData.set(ChannelData.getChannelIndex(currentData.channelData, channelData.channelId), channelData);
			writeData(serverId, new ServerData(currentData.channelData, EventHandler.getServerPrefix(serverId), currentData.webhookURL));
		}
	}

	public static void writeChannel(String serverId, ChannelData channelData, TextChannel channel) {
		synchronized (SERVER_DATA) {
			ServerData currentData = SERVER_DATA.get(serverId);
			List<ChannelData> newChannelData = currentData != null ? currentData.channelData : new ArrayList<>();
		
			int index = ChannelData.getChannelIndex(newChannelData, channelData.channelId);
			if (index != -1)
				newChannelData.set(index, channelData);
			else
				newChannelData.add(channelData);
		
			try {
				writeData(serverId, new ServerData(newChannelData, EventHandler.getServerPrefix(serverId), ServerData.getOrCreateWebhookURL(channel, currentData)));
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void unwriteChannel(String serverId, ChannelData data) {
		synchronized (SERVER_DATA) {
			ServerData currentData = SERVER_DATA.get(serverId);
			currentData.channelData.remove(data);
			writeData(serverId, currentData);
		}
	}
	
	public static void writeWebhook(String serverId, Webhook webhook) {
		synchronized (SERVER_DATA) {
			ServerData currentData = SERVER_DATA.get(serverId);
			writeData(serverId, new ServerData(currentData.channelData, currentData.prefix, webhook.getUrl()));
		}
	}
	
	public static void writePrefix(String serverId, String prefix) {
		synchronized (SERVER_DATA) {
			ServerData serverData = SERVER_DATA.get(serverId);
			if (serverData == null) {
				serverData = new ServerData(new ArrayList<>(), prefix, null);
			} else {
				serverData = new ServerData(serverData.channelData, prefix, serverData.webhookURL);
			}
			writeData(serverId, serverData);
		}
	}
	
	public static void writeData(String serverId, ServerData data) {
		try {
			Writer writer = Files.newBufferedWriter(Paths.get(DIRECTORY + "/" + serverId));
			GSON.toJson(data, writer);
			writer.close();
			SERVER_DATA.put(serverId, data);
		} catch (IOException | JsonIOException e) {
			e.printStackTrace();
		}
	}
	
	public static class ServerData {
		public final List<ChannelData> channelData;
		public final String prefix;
		@Nullable
		public final String webhookURL;
		
		/**
		 * Class for server data
		 * @param channelData - A list of all the data stored for each channel(if any) {@link ChannelData}
		 * @param prefix - The server's command prefix for this bot
		 * @param webhookURL - The server's webhook url {@link Webhook#getUrl()}
		 */
		private ServerData(List<ChannelData> channelData, String prefix, @Nullable String webhookURL) {
			this.channelData = channelData;
			this.prefix = prefix;
			this.webhookURL = webhookURL;
		}
		
		@Nullable
		public ChannelData getChannelDataById(long channelId) {
			for (ChannelData channels : this.channelData) {
				if (channels.channelId == channelId) {
					return channels;
				}
			}
			return null;
		}
		
		public static String getOrCreateWebhookURL(TextChannel channel, @Nullable ServerData serverData) throws InterruptedException, ExecutionException {
			if (serverData != null && serverData.webhookURL == null) {
				return channel.createWebhook("Cross-Poster").submit().get().getUrl();
			}
			
			for (Webhook webhook : channel.getGuild().retrieveWebhooks().submit().get()) {
				String url = webhook.getUrl();
				if (serverData != null && url.equals(serverData.webhookURL)) {
					return url;
				}
			}
			return channel.createWebhook("Cross-Poster").submit().get().getUrl();
		}
	}
	
	public static class ChannelData {
		public final long channelId;
		public final long crosspostChannelId;
		public final boolean requiresAttachment;
		
		public ChannelData(long channelId, long crosspostChannelId) {
			this(channelId, crosspostChannelId, true);
		}
		
		/**
		 * Class for data about an individual channel
		 * @param channelId - The channel's id {@link TextChannel#getIdLong()}
		 * @param crosspostChannelId - The id of the channel the channel crossposts to {@link TextChannel#getIdLong()}
		 * @param requiresAttachment - If the channel should require an attachment to crosspost a message
		 */
		public ChannelData(long channelId, long crosspostChannelId, boolean requiresAttachment) {
			this.channelId = channelId;
			this.crosspostChannelId = crosspostChannelId;
			this.requiresAttachment = requiresAttachment;
		}
		
		@Nullable
		public static ChannelData getCrosspostChannel(List<ChannelData> channelData, long channelId) {
			for (ChannelData data : channelData) {
				if (data.channelId == channelId) return data;
			}
			return null;
		}
		
		public static int getChannelIndex(List<ChannelData> data, long channelId) {
			for (int i = 0; i < data.size(); i++) {
				ChannelData channel = data.get(i);
				if (channel.channelId == channelId) {
					return i;
				}
			}
			return -1;
		}
	}
}