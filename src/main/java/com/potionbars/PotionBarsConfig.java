package com.potionbars;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("potiondurationbars")
public interface PotionBarsConfig extends Config
{
	@ConfigItem(
		keyName = "lockPosition",
		name = "Lock bar position",
		description = "Prevent the potion bars from being moved with Alt-drag"
	)
	default boolean lockPosition()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showTime",
		name = "Show remaining time",
		description = "Show a countdown on each active potion bar"
	)
	default boolean showTime()
	{
		return true;
	}

	@ConfigItem(
		keyName = "barWidth",
		name = "Bar width",
		description = "Width of each potion bar in pixels",
		position = 2
	)
	default int barWidth()
	{
		return 190;
	}

	@ConfigItem(
		keyName = "barHeight",
		name = "Bar height",
		description = "Height of each potion bar in pixels",
		position = 3
	)
	default int barHeight()
	{
		return 20;
	}
}
