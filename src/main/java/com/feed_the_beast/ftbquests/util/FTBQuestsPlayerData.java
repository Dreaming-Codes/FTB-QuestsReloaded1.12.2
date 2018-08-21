package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.events.player.ForgePlayerDataEvent;
import com.feed_the_beast.ftblib.events.player.ForgePlayerLoadedEvent;
import com.feed_the_beast.ftblib.events.player.ForgePlayerLoggedInEvent;
import com.feed_the_beast.ftblib.events.player.ForgePlayerSavedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerLeftEvent;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.PlayerData;
import com.feed_the_beast.ftblib.lib.util.FileUtils;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageSyncQuests;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.rewards.PlayerRewards;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

/**
 * @author LatvianModder
 */
@Mod.EventBusSubscriber(modid = FTBQuests.MOD_ID)
public class FTBQuestsPlayerData extends PlayerData
{
	public static FTBQuestsPlayerData get(ForgePlayer player)
	{
		return player.getData().get(FTBQuests.MOD_ID);
	}

	@SubscribeEvent
	public static void registerTeamData(ForgePlayerDataEvent event)
	{
		event.register(new FTBQuestsPlayerData(event.getPlayer()));
	}

	@SubscribeEvent
	public static void onPlayerSaved(ForgePlayerSavedEvent event)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		FTBQuestsPlayerData data = get(event.getPlayer());
		data.writeData(nbt);

		File file = event.getPlayer().getDataFile("ftbquests");

		if (nbt.isEmpty())
		{
			FileUtils.delete(file);
		}
		else
		{
			NBTUtils.writeNBTSafe(file, nbt);
		}
	}

	@SubscribeEvent
	public static void onPlayerLoaded(ForgePlayerLoadedEvent event)
	{
		FTBQuestsPlayerData data = get(event.getPlayer());
		data.rewards = new PlayerRewards(ServerQuestFile.INSTANCE);

		NBTTagCompound nbt = NBTUtils.readNBT(event.getPlayer().getDataFile("ftbquests"));
		data.readData(nbt == null ? new NBTTagCompound() : nbt);
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(ForgePlayerLoggedInEvent event)
	{
		FTBQuestsPlayerData data = get(event.getPlayer());

		if (data.rewards == null)
		{
			data.rewards = new PlayerRewards(ServerQuestFile.INSTANCE);
		}

		EntityPlayerMP player = event.getPlayer().getPlayer();
		NBTTagCompound teamData = new NBTTagCompound();

		for (ForgeTeam team : event.getUniverse().getTeams())
		{
			teamData.setTag(team.getName(), FTBQuestsTeamData.get(team).serializeTaskData());
		}

		NBTTagCompound nbt = new NBTTagCompound();
		ServerQuestFile.INSTANCE.writeData(nbt);
		new MessageSyncQuests(nbt, event.getPlayer().team.getName(), teamData, FTBQuests.canEdit(player), data.rewards.items).sendTo(player);
	}

	@SubscribeEvent
	public static void onPlayerLeftTeam(ForgeTeamPlayerLeftEvent event)
	{
		FTBQuestsPlayerData data = get(event.getPlayer());

		if (!data.rewards.items.isEmpty())
		{
			data.rewards.items.clear();
			event.getPlayer().markDirty();
		}
	}

	public PlayerRewards rewards;

	private FTBQuestsPlayerData(ForgePlayer player)
	{
		super(player);
	}

	@Override
	public String getName()
	{
		return FTBQuests.MOD_ID;
	}

	private void writeData(NBTTagCompound nbt)
	{
		if (!rewards.items.isEmpty())
		{
			nbt.setTag("Rewards", rewards.serializeNBT());
		}
	}

	private void readData(NBTTagCompound nbt)
	{
		rewards.deserializeNBT(nbt.getTagList("Rewards", Constants.NBT.TAG_COMPOUND));
	}
}