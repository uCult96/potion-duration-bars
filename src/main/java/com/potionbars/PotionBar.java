package com.potionbars;

import java.time.Duration;
import java.time.Instant;

final class PotionBar
{
	private final String name;
	private int totalTicks;
	private Instant expiresAt;

	PotionBar(String name, int ticks)
	{
		this.name = name;
		this.totalTicks = Math.max(1, ticks);
		refresh(ticks);
	}

	void refresh(int ticks)
	{
		totalTicks = Math.max(totalTicks, ticks);
		expiresAt = Instant.now().plusMillis(Math.max(0, ticks) * 600L);
	}

	String getName()
	{
		return name;
	}

	int getTotalTicks()
	{
		return totalTicks;
	}

	long getRemainingMillis()
	{
		return Math.max(0, Duration.between(Instant.now(), expiresAt).toMillis());
	}

	int getRemainingTicks()
	{
		return (int) Math.ceil(getRemainingMillis() / 600.0);
	}
}
