package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftbquests.gui.GuiTask;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class QuestTaskData<T extends QuestTask> implements ICapabilityProvider, IItemHandler
{
	public final T task;
	public final IProgressData data;
	public boolean isComplete = false;

	public QuestTaskData(T q, IProgressData d)
	{
		task = q;
		data = d;
	}

	@Nullable
	public abstract NBTBase toNBT();

	public abstract void fromNBT(@Nullable NBTBase nbt);

	public abstract long getProgress();

	public abstract void resetProgress();

	public abstract void completeInstantly();

	public double getRelativeProgress()
	{
		long max = task.getMaxProgress();

		if (max == 0)
		{
			return 0D;
		}

		long progress = getProgress();

		if (progress >= max)
		{
			return 1D;
		}

		return (double) progress / (double) max;
	}

	public String getProgressString()
	{
		return Long.toString(getProgress());
	}

	public String toString()
	{
		return data + "@" + task.getID();
	}

	public ItemStack insertItem(ItemStack stack, boolean singleItem, boolean simulate)
	{
		return stack;
	}

	@Override
	public final int getSlots()
	{
		return 1;
	}

	@Override
	public final ItemStack getStackInSlot(int slot)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public final ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		if (task.canInsertItem() && task.getMaxProgress() > 0L && getProgress() < task.getMaxProgress() && !stack.isEmpty())
		{
			return insertItem(stack, false, simulate);
		}

		return stack;
	}

	@Override
	public final ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@SideOnly(Side.CLIENT)
	public void addTabs(List<GuiTask.Tab> tabs)
	{
	}
}