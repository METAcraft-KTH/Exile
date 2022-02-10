package com.hampushallkvist.exile.events

import com.hampushallkvist.metacraft.server.events.Event
import net.minecraft.command.DataCommandStorage
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.world.World
import java.lang.Math.abs

class ZoneNotifier : Event {

    companion object {
        enum class Zone(i: Int) {
            WILDERNESS(1),
            CIVILIZATION(2)
        }

        fun getPlayerZone(serverPlayerEntity: PlayerEntity): Zone {
            if (serverPlayerEntity.pos.z > 0)
                return Zone.CIVILIZATION
            return Zone.WILDERNESS
        }

        fun updatePlayerZone(storage: DataCommandStorage, serverPlayerEntity: PlayerEntity, newZone: Zone) {
            val exileNamespace = storage.get(Identifier("exile", "exiles"))
            val currentPlayerNbt = exileNamespace.getCompound(serverPlayerEntity.entityName) ?: return

            currentPlayerNbt.putString("current_zone", newZone.toString())
        }
    }

    override fun onEvent(server: MinecraftServer) {
        val namespaceIdentifier = Identifier("exile", "exiles")
        val exileNamespace = server.dataCommandStorage.get(namespaceIdentifier)

        // Runs for every online player on the server
        server.playerManager.playerList.forEach {
            val player = it

            val playerNBTCompound = exileNamespace.getCompound(player.entityName)
            // If player doesn't exist then we insert it in storage
            if (!playerNBTCompound.contains("current_zone"))
                Exile.manageUninitializedPlayer(server.dataCommandStorage, player, -2L)

            val currentZone = playerNBTCompound.getString("current_zone")
            if (player != null && currentZone != null) {
                if (player.getWorld().getRegistryKey().equals(World.END)) {
                    if (player.pos.x < 150 && player.pos.y > -150 && player.pos.z < 150 && player.pos.z > -150) {
                        return;
                    }
                }
                // Check if we have a diff in state and actual position
                if (player.pos.z < 0 && currentZone == Zone.CIVILIZATION.toString()) {
                    updatePlayerZone(server.dataCommandStorage, player, Zone.WILDERNESS)
                    player.sendMessage(LiteralText("You have entered wilderness, be alert!").formatted(
                        Formatting.GOLD), false)
                } else if (player.pos.z > 0 && currentZone == Zone.WILDERNESS.toString()) {
                    updatePlayerZone(server.dataCommandStorage, player, Zone.CIVILIZATION)
                    player.sendMessage(LiteralText("You have entered civilization, you are now safe").formatted(
                        Formatting.GOLD), false)
                }
            }
        }
    }
}