package com.hampushallkvist.exile.events

import com.hampushallkvist.metacraft.server.events.Event
import net.minecraft.server.MinecraftServer
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.util.*

class Exile : Event {
    override fun register(server: MinecraftServer) {
        val namespaceIdentifier = Identifier("exile", "exiles")
        val exileNamespace = server.dataCommandStorage.get(namespaceIdentifier)
        val exiledPlayers = exileNamespace.keys

        var hasChanged = false
        exiledPlayers.forEach {
            val player = server.playerManager.getPlayer(it) ?: return
            val exileTimestamp = exileNamespace.getString(it).toLong()

            if (exileTimestamp != -1L && exileTimestamp < Date().time) {
                player.sendMessage(LiteralText("You are no longer an exile, congrats"), false)
                exileNamespace.remove(it)
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