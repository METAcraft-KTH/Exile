package com.hampushallkvist.exile

import com.hampushallkvist.exile.commands.CommandRegister
import com.hampushallkvist.metacraft.server.events.EventRegister
import net.fabricmc.api.ModInitializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class Main : ModInitializer {

    companion object {
        const val MOD_ID = "exile"
        val LOGGER: Logger = LogManager.getLogger(MOD_ID)
    }

    override fun onInitialize() {

        CommandRegister().init()
        EventRegister().init()

        LOGGER.info("Exile server mod by Hampus Hallkvist initialized")
    }
}