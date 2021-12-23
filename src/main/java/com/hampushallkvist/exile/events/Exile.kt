package com.hampushallkvist.exile.events

import com.hampushallkvist.exile.commands.Exile
import com.hampushallkvist.metacraft.server.events.Event
import net.minecraft.command.DataCommandStorage
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.util.*

class Exile : Event {

    companion object {
        fun manageUninitializedPlayer(storage: DataCommandStorage, player: PlayerEntity, timestamp: Long) {
            Exile.setPlayerExileStatus(storage, player, timestamp)
        }
    }

    override fun onEvent(server: MinecraftServer) {
        val namespaceIdentifier = Identifier("exile", "exiles")
        val exileNamespace = server.dataCommandStorage.get(namespaceIdentifier)
        val players = exileNamespace.keys

        var hasChanged = false
        players.forEach {
            val player = server.playerManager.getPlayer(it) ?: return
            val playerNBTCompound = exileNamespace.getCompound(player.entityName)

            // If player doesn't exist then we insert it in storage
            if (playerNBTCompound == null) {
                manageUninitializedPlayer(server.dataCommandStorage, player, -2L)
                hasChanged = true
                return
            }
            val exileTimestamp = playerNBTCompound.getString("exiled_until").toLong()
            if (exileTimestamp == -2L)
                return

            if (exileTimestamp != -1L && exileTimestamp < Date().time) {
                player.sendMessage(LiteralText("You are no longer an exile, congrats").formatted(Formatting.GREEN), false)
                playerNBTCompound.putString("exiled_until", (-2L).toString())
                exileNamespace.put(player.entityName, playerNBTCompound)
                hasChanged = true
            }

            if (player.pos.z > 0) {
                player.teleport(player.pos.x, player.pos.y + 1, -5.0)
                player.sendMessage(LiteralText("You are an exile, you are not allowed to enter the civilization").formatted(Formatting.RED), false)
            }
        }

        if (hasChanged)
            server.dataCommandStorage.set(namespaceIdentifier, exileNamespace)
    }
}