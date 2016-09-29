package com.Sobki.korra.Link;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;

public class LinkListener implements Listener {
	
	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event) {
		
		if (event.isCancelled()) {
			return;
		}
		if (!event.getPlayer().isSneaking()) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(event.getPlayer());
			if (bPlayer != null && bPlayer.canBend(CoreAbility.getAbility("Link"))) {
				new Link(event.getPlayer());
			}
		}
	}

}