package me.ellbristow.ChestBank;

import net.minecraft.server.EntityHuman;
import net.minecraft.server.TileEntityChest;

public class TileEntityVirtualChest extends TileEntityChest {
	public TileEntityVirtualChest()
	{
		super();
	}

	public boolean a_(EntityHuman entityhuman) {
		return true;
	}
}
