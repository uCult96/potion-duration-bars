package com.potionbars;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class PotionBarsOverlay extends Overlay
{
	private static final Color BACKGROUND = new Color(20, 20, 20, 210);
	private static final Color FILL = new Color(75, 175, 105, 220);
	private static final Color OUTLINE = new Color(230, 230, 230, 150);

	private final PotionBarsConfig config;
	private final PotionBarsPlugin plugin;

	@Inject
	PotionBarsOverlay(PotionBarsConfig config, PotionBarsPlugin plugin)
	{
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setMovable(true);
		setSnappable(true);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		List<PotionBar> bars = plugin.getActiveBars();
		setMovable(!config.lockPosition());
		if (bars.isEmpty())
		{
			return null;
		}

		int width = Math.max(100, Math.min(500, config.barWidth()));
		int height = Math.max(14, Math.min(60, config.barHeight()));
		int gap = 4;
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		FontMetrics metrics = graphics.getFontMetrics();

		int y = 0;
		for (PotionBar bar : bars)
		{
			long remaining = bar.getRemainingMillis();
			float fraction = Math.max(0f, Math.min(1f, remaining / (bar.getTotalTicks() * 600f)));
			graphics.setColor(BACKGROUND);
			graphics.fillRoundRect(0, y, width, height, 6, 6);
			graphics.setColor(FILL);
			graphics.fillRoundRect(0, y, Math.round(width * fraction), height, 6, 6);
			graphics.setColor(OUTLINE);
			graphics.drawRoundRect(0, y, width - 1, height - 1, 6, 6);

			String label = bar.getName();
			if (config.showTime())
			{
				label += "  " + formatTime(remaining);
			}
			graphics.setColor(Color.WHITE);
			int textY = y + (height - metrics.getHeight()) / 2 + metrics.getAscent();
			graphics.drawString(label, 7, textY);
			y += height + gap;
		}
		return new Dimension(width, y - gap);
	}

	private static String formatTime(long millis)
	{
		long seconds = (millis + 999) / 1000;
		return String.format("%d:%02d", seconds / 60, seconds % 60);
	}
}
