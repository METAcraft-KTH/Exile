package com.hampushallkvist.metacraft.server.events

import com.hampushallkvist.exile.events.Exile
import com.hampushallkvist.exile.events.ZoneNotifier
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer

interface Event {
    fun onEvent(server: MinecraftServer)
}

class EventRegister {

    fun init() {
        registerTick(Exile())
        registerTick(ZoneNotifier())
    }

    private fun registerTick(event: Event) {
        ServerTickEvents.END_SERVER_TICK.register(event::onEvent)
    }
}