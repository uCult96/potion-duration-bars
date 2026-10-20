package com.potionbars;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PotionBarsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PotionBarsPlugin.class);
		RuneLite.main(args);
	}
}
