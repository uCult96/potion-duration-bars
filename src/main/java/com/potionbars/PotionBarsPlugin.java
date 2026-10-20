package com.potionbars;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.ChatMessageType;
import net.runelite.api.GameState;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Potion Duration Bars",
	description = "Show active potion durations as movable countdown bars",
	tags = {"potion", "timer", "bar", "overlay", "buff"}
)
public class PotionBarsPlugin extends Plugin
{
	private static final int PRAYER_ENHANCE_TIMER = -1;
	private final Map<Integer, PotionBar> activeBars = new HashMap<>();

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PotionBarsOverlay overlay;

	@Inject
	private PotionBarsConfig config;

	@Provides
	PotionBarsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PotionBarsConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		refreshFromClient();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		activeBars.clear();
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		refreshFromClient();
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		String message = event.getMessage();
		if (message.startsWith("You drink some of your") && message.contains("prayer enhance"))
		{
			activeBars.put(PRAYER_ENHANCE_TIMER, new PotionBar("Prayer enhance", 483));
		}
		else if (message.contains("Your prayer enhance effect has worn off"))
		{
			activeBars.remove(PRAYER_ENHANCE_TIMER);
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		refreshFromClient();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			activeBars.clear();
		}
		else
		{
			refreshFromClient();
		}
	}

	List<PotionBar> getActiveBars()
	{
		List<PotionBar> bars = new ArrayList<>(activeBars.values());
		bars.sort(Comparator.comparing(PotionBar::getName));
		return bars;
	}

	private void refreshFromClient()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		track(VarbitID.DIVINECOMBAT_POTION_TIME, "Divine combat", false);
		track(VarbitID.DIVINERANGE_POTION_TIME, "Divine ranging", false);
		track(VarbitID.DIVINEMAGIC_POTION_TIME, "Divine magic", false);
		track(VarbitID.DIVINEBASTION_POTION_TIME, "Divine bastion", false);
		track(VarbitID.DIVINEBATTLEMAGE_POTION_TIME, "Divine battlemage", false);
		removeCoveredDivineEffects();
		trackScaled(VarbitID.STAMINA_DURATION, "Stamina", 10,
			client.getVarbitValue(VarbitID.STAMINA_ACTIVE) != 1);
		trackScaled(VarbitID.ANTIFIRE_POTION, "Antifire", 30, false);
		trackScaled(VarbitID.SUPER_ANTIFIRE_POTION, "Super antifire", 20, false);
		int overload = Math.max(client.getVarbitValue(VarbitID.NZONE_OVERLOAD_POTION_EFFECTS),
			Math.max(client.getVarbitValue(VarbitID.RAIDS_OVERLOAD_TIMER),
				client.getVarbitValue(VarbitID.DEADMAN_OVERLOAD_POTION_EFFECTS)));
		track(-2, "Overload", overload * 25, false);
		trackScaled(VarbitID.GOADING_POTION_TIMER, "Goading potion", 6, false);
		trackScaled(VarbitID.PRAYER_REGENERATION_POTION_TIMER, "Prayer regeneration", 12, false);
		trackScaled(VarbitID.SURGE_POTION_TIMER, "Surge potion", 10, false);
		track(VarbitID.MOONLIGHT_POTION_TIME, "Moonlight potion", false);
	}

	private void removeCoveredDivineEffects()
	{
		int combat = client.getVarbitValue(VarbitID.DIVINECOMBAT_POTION_TIME);
		int bastion = client.getVarbitValue(VarbitID.DIVINEBASTION_POTION_TIME);
		int battlemage = client.getVarbitValue(VarbitID.DIVINEBATTLEMAGE_POTION_TIME);
		if (combat > 0)
		{
			removeIfValueEquals(VarbitID.DIVINEATTACK_POTION_TIME, combat);
			removeIfValueEquals(VarbitID.DIVINESTRENGTH_POTION_TIME, combat);
			removeIfValueEquals(VarbitID.DIVINEDEFENCE_POTION_TIME, combat);
		}
		if (bastion > 0)
		{
			removeIfValueEquals(VarbitID.DIVINERANGE_POTION_TIME, bastion);
			removeIfValueEquals(VarbitID.DIVINEDEFENCE_POTION_TIME, bastion);
		}
		if (battlemage > 0)
		{
			removeIfValueEquals(VarbitID.DIVINEMAGIC_POTION_TIME, battlemage);
			removeIfValueEquals(VarbitID.DIVINEDEFENCE_POTION_TIME, battlemage);
		}
	}

	private void removeIfValueEquals(int varbit, int value)
	{
		if (client.getVarbitValue(varbit) == value)
		{
			activeBars.remove(varbit);
		}
	}

	private void track(int varbitId, String name, boolean inactive)
	{
		track(varbitId, name, inactive ? 0 : client.getVarbitValue(varbitId), false);
	}

	private void trackScaled(int varbitId, String name, int scale, boolean inactive)
	{
		track(varbitId, name, inactive ? 0 : client.getVarbitValue(varbitId) * scale, false);
	}

	private void track(int timerId, String name, int ticks, boolean inactive)
	{
		if (inactive)
		{
			ticks = 0;
		}
		if (ticks <= 0)
		{
			activeBars.remove(timerId);
			return;
		}

		PotionBar bar = activeBars.get(timerId);
		if (bar == null)
		{
			activeBars.put(timerId, new PotionBar(name, ticks));
		}
		else
		{
			bar.refresh(ticks);
		}
	}
}
