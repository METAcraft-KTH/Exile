package com.hampushallkvist.exile.commands

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.minecraft.server.command.ServerCommandSource

interface Command {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>, dedicated: Boolean)
}

class CommandRegister {
    fun init() {
        register(Exile())
    }

    private fun register(command: Command) {
        CommandRegistrationCallback.EVENT.register(command::register)
    }
}