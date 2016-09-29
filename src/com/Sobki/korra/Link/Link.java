package com.Sobki.korra.Link;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AvatarAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class Link extends AvatarAbility implements AddonAbility {
	
	static String permission = "bending.ability.Link";
	static String path = "ExtraAbilities.Sobki.Link.";
	static int range;
	static double hpPerSecond;
	static long duration;
	static long cooldown;
	static float soundVolume;
	
	private Player player;
	private Player target;
	private Location origin;
	private boolean active;
	private long activateTime;
	private double angle;
	private int iterator;

	public Link(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			remove();
			return;
		}
		
		this.player = player;
		this.origin = player.getLocation();
		
		Block targetBlock = getTargetBlock(player, range);
		List<Location> locs = getLocationsBetweenTwoPoints(origin, targetBlock.getLocation(), 0.5F);
		for (Location location : locs) {
			
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 1)) {
				if (entity instanceof Player && entity.getEntityId() != player.getEntityId()) {
					
					target = (Player) entity;
					activateTime = System.currentTimeMillis();
					active = true;
					angle = 0;
					iterator = 0;
					start();
				}
			}
		}
	}

	@Override
	public long getCooldown() {
		
		return cooldown;
	}

	@Override
	public Location getLocation() {
		
		return origin;
	}

	@Override
	public String getName() {
		
		return "Link";
	}

	@Override
	public boolean isHarmlessAbility() {
		
		return true;
	}

	@Override
	public boolean isSneakAbility() {
		
		return true;
	}
	
	@Override
	public boolean requireAvatar() {
		
		return true;
	}

	@Override
	public void progress() {
		
		if (player == null || target == null ||
				!active || !player.isSneaking()) {
			remove();
			return;
		}
		
		System.out.println(System.currentTimeMillis() - activateTime);
		
		if (System.currentTimeMillis() - activateTime > duration) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		
		if (player.getLocation().distance(target.getLocation()) > range) {
			remove();
			return;
		}
		
		Location start = player.getLocation().clone().add(0, 1, 0);
		Location end = target.getLocation().clone().add(0, 1, 0);
		
		displayLink(start, end);
		spawnHelix(target.getLocation(), 1, 2, angle);
		spawnHelix(target.getLocation(), 1, 2, angle + 180);
		angle += 3;

		double midX = (start.getX() + end.getX()) / 2;
		double midY = (start.getY() + end.getY()) / 2;
		double midZ = (start.getZ() + end.getZ()) / 2;
		
		Location centre = new Location(player.getWorld(), midX, midY, midZ);
		boolean boo = iterator % 20 == 0 ? true : false;
		if (boo) {
			centre.getWorld().playSound(centre, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, soundVolume, 2);
			ParticleEffect.HEART.display(end.clone().add(0, 1, 0), 0, 0, 0, 0, 1);
		}
		iterator++;
		
		if (active) {
			
			double addition = hpPerSecond / 20;
			double health = target.getHealth() + addition;
			if (health > 20.0) {
				health = 20.0;
			}
			
			target.setHealth(health);
		}
		
	}

	@Override
	public String getAuthor() {
		
		return "Sobki";
	}
	
	@Override
	public String getDescription() {
		
		String heart = hpPerSecond / 2 == 1 ? " heart" : " hearts";
		double d = hpPerSecond / 2;
		String hps = d > (int) d + 0.001 ? String.valueOf(d) : String.valueOf((int) d);
		return "Developed by " + getAuthor() +
				"\nLink is an ability for the Avatar. It allows them to create a link of energy between them and another player, regenerating the targeted player's health over time. "
				+ "To use, look at the user you wish to heal and hold shift. Once the link has be created, the targeted player will begin to receive " + hps + heart + " per second. Release shift to break the link.";
	}

	@Override
	public String getVersion() {
		
		return "v1.0";
	}

	@Override
	public void load() {
		
		Bukkit.getPluginManager().registerEvents(new LinkListener(), ProjectKorra.plugin);
		
		ProjectKorra.log.info(getName() + " by " + getAuthor() + " " + getVersion() + " has been loaded!");

		ConfigManager.defaultConfig.get().addDefault(path + "Range", Integer.valueOf(12));
		ConfigManager.defaultConfig.get().addDefault(path + "HPPerSecond", Integer.valueOf(2));
		ConfigManager.defaultConfig.get().addDefault(path + "Duration", Long.valueOf(5000));
		ConfigManager.defaultConfig.get().addDefault(path + "Cooldown", Long.valueOf(6000));
		ConfigManager.defaultConfig.get().addDefault(path + "SoundVolume", Double.valueOf(0.5));
		
		ConfigManager.defaultConfig.save();

		range = ConfigManager.defaultConfig.get().getInt(path + "Range");
		hpPerSecond = ConfigManager.defaultConfig.get().getInt(path + "HPPerSecond");
		duration = ConfigManager.defaultConfig.get().getLong(path + "Duration");
		cooldown = ConfigManager.defaultConfig.get().getLong(path + "Cooldown");
		soundVolume = (float) ConfigManager.defaultConfig.get().getDouble(path + "SoundVolume");

		ProjectKorra.plugin.getServer().getPluginManager().addPermission(new Permission(permission));
		
	}

	@Override
	public void stop() {
		
		active = false;
	}
	
	public static boolean isValidBlock(Block block) {
		
		if (block.getType().isSolid()) {
			return false;
		} else if (block.isLiquid()) {
			return false;
		}
		
		return true;
	}
	
	public static List<Location> getLocationsBetweenTwoPoints(Location loc1, Location loc2, float... interval) {
		
		float f = interval.length == 0 ? 0.2F : interval[0];
		
		double distance = loc1.distance(loc2);
		Vector vec = new Vector(loc2.getX() - loc1.getX(), loc2.getY() - loc1.getY(), loc2.getZ() - loc1.getZ()).normalize();
		List<Location> locations = new ArrayList<Location>();
		for (int i = 0; i < distance / f; i++) {
			locations.add(loc1.clone().add(vec.clone().multiply(i * f)));
		}
		return locations;
	}
	
	public static Block getTargetBlock(Player player, int range) {
		BlockIterator iterator = new BlockIterator(player, range);
		Block prevBlock = iterator.next();
		while (iterator.hasNext()) {
			prevBlock = iterator.next();
			if (!prevBlock.getType().isSolid() && !prevBlock.isLiquid()) {
				continue;
			}
			break;
		}
		
		return prevBlock;
	}
	
	public static void displayLink(Location a, Location b) {
		
		for (Location loc : getLocationsBetweenTwoPoints(a, b)) {
			
			String colour;
			double randy = Math.random();
			colour = randy < 0.67 ? "00FFFF" : "028482";
			GeneralMethods.displayColoredParticle(loc, colour);
		}
	}
	
	public static void spawnHelix(Location origin, double radius, double height, double angle) {
		
		int particles = 20;
		for (int i = 0; i < particles; i++) {
			double y = height / particles * i;
			double a = 180 / particles * i + angle;
			double x = radius * Math.cos(Math.toRadians(a));
			double z = radius * Math.sin(Math.toRadians(a));
			
			String colour;
			colour = i % 2 == 0 ? "ffffff" : "ffd700";
			Location point = origin.clone().add(x, y, z);
			GeneralMethods.displayColoredParticle(point, colour);
		}
	}

}
