package at.priv.joestr.uwtweaks;

import at.priv.joestr.uwtweaks.miscellaneous.ReplyBackState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class UwTweaksClient implements ClientModInitializer {
	private static final Logger log = LoggerFactory.getLogger("uwtweaks");

	private ReplyBackState state = ReplyBackState.getInstance();

	@Override
	public void onInitializeClient() {

		ClientPlayConnectionEvents.JOIN.register(new ClientPlayConnectionEvents.Join() {
			@Override
			public void onPlayReady(ClientPacketListener clientPacketListener, PacketSender packetSender, Minecraft minecraft) {
				var serverData = clientPacketListener.getServerData();

				if (serverData != null) {
					InetAddress ipAddress = null;
                    try {
						ipAddress = InetAddress.getByName(serverData.ip);
                    } catch (UnknownHostException | SecurityException e) {
                        throw new RuntimeException(e);
                    }

					/* UWMC does not have proper rDNS records (bad)
					String hostName = null;
                    try {
                        var hostInfo = InetAddress.getByAddress(ipAddress.getAddress());
						hostName = hostInfo.getCanonicalHostName();
                    } catch (UnknownHostException | SecurityException e) {
                        throw new RuntimeException(e);
                    } */

					/* Check against well-known ip addresses */
					InetAddress uwIp6 = null;
					InetAddress uwIp4 = null;

                    try {
                        uwIp6 = InetAddress.getByName("2a01:4f8:c0c:1c27::1");
                    } catch (UnknownHostException | SecurityException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        uwIp4 = InetAddress.getByName("135.125.238.63");
                    } catch (UnknownHostException | SecurityException e) {
                        throw new RuntimeException(e);
                    }

					if (ipAddress.getHostAddress().equals(uwIp4.getHostAddress()) || ipAddress.getHostAddress().equals(uwIp6.getHostAddress())) {
						state.setEnabled(true);
					}
				}
			}
		});

		ClientReceiveMessageEvents.GAME.register(new ClientReceiveMessageEvents.Game() {
			@Override
			public void onReceiveGameMessage(Component component, boolean b) {
				if (!state.isEnabled()) {
					return;
				}

				String message = component.getString();
				String preColon = message.split(": ")[0];

				if (!preColon.contains("flüstert")) {
					return;
				}

				if (state.isChatOpened() && !state.isReceivedAnotherWhisperWhileDraftingResponse()) {
					state.setReceivedAnotherWhisperWhileDraftingResponse(true);
					log.info("Received a whisper while writing response, redirecting reply to " + state.getLastWhisperPartner());
				} else {
					String[] playerSplit = preColon.split(" ");
					if (playerSplit.length == 3) {
						// Player has prefix rank
						state.setLastWhisperPartner(playerSplit[1]);
					} else {
						state.setLastWhisperPartner(playerSplit[0]);
					}
					log.info("Set last whisper partner to " + state.getLastWhisperPartner());
				}
			}
		});

		ClientSendMessageEvents.CHAT.register(new ClientSendMessageEvents.Chat() {
			@Override
			public void onSendChatMessage(String s) {
				if (!state.isEnabled()) {
					return;
				}

				state.setChatOpened(false);
				state.setReceivedAnotherWhisperWhileDraftingResponse(false);
			}
		});

		ClientSendMessageEvents.COMMAND.register(new ClientSendMessageEvents.Command() {
			@Override
			public void onSendCommandMessage(String s) {
				if (!state.isEnabled()) {
					return;
				}

				state.setChatOpened(false);
				state.setReceivedAnotherWhisperWhileDraftingResponse(false);
			}
		});

		ClientSendMessageEvents.MODIFY_COMMAND.register(new ClientSendMessageEvents.ModifyCommand() {
			@Override
			public String modifySendCommandMessage(String s) {
				if (!state.isEnabled()) {
					return s;
				}

				if ((s.startsWith("r ") || s.startsWith("reply ")) && state.isReceivedAnotherWhisperWhileDraftingResponse()) {
					String[] split = s.split(" ");
					String[] argsArray = Arrays.copyOfRange(split, 1, split.length);
					String args = String.join(" ", argsArray);
					return "tell " + state.getLastWhisperPartner() + " " + args;
				} else {
					return s;
				}
			}
		});

		ScreenEvents.AFTER_INIT.register(new ScreenEvents.AfterInit() {
			@Override
			public void afterInit(Minecraft minecraft, Screen screen, int i, int i1) {
				if (screen instanceof ChatScreen cs) {
					if (!state.isEnabled()) {
						return;
					}

					if (!state.isChatOpened()) {
						state.setChatOpened(true);
						state.setReceivedAnotherWhisperWhileDraftingResponse(false);
					}
				}
			}
		});
	}
}