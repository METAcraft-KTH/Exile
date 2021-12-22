package com.hampushallkvist.metacraft.server.events

import com.hampushallkvist.exile.events.Exile
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer

interface Event {
    fun register(server: MinecraftServer)
}

class EventRegister {

    fun init() {
        register(Exile())
    }

    private fun register(event: Event) {
        ServerTickEvents.END_SERVER_TICK.register(event::register)
    }
}