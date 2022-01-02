package com.hampushallkvist.exile.commands
import com.hampushallkvist.exile.events.ZoneNotifier
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource.suggestMatching
import net.minecraft.command.DataCommandStorage
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.time.Instant
import java.util.*

class Exile : Command {

    companion object {
        /**
         * This function accesses the NBT storage and saves the data properly
         * if timestamp is -2 then the user is NOT exiled
         * -1 means exiled until turned of
         * all positive numbers describe timestamp until release
         */
        fun setPlayerExileStatus(storage: DataCommandStorage, player: PlayerEntity, timestamp: Long) {
            val exiles = storage.get(Identifier("exile", "exiles"))

            val compound = NbtCompound()
            compound.putString("current_zone", ZoneNotifier.getPlayerZone(player).toString())
            compound.putString("exiled_until", timestamp.toString())
            exiles.put(player.entityName, compound)
            storage.set(Identifier("exile", "exiles"), exiles)
        }
    }

    override fun register(dispatcher: CommandDispatcher<ServerCommandSource>, dedicated: Boolean) {
        val command: LiteralArgumentBuilder<ServerCommandSource> = CommandManager.literal("exile")
            // List command
            .then(CommandManager.literal("list")
                .executes(this::listExiledPlayers))
            // Sub-commands that require admin privileges
            .requires { source -> source.hasPermissionLevel(4) }
            // Add exile player
            .then(CommandManager.literal("set")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .suggests { context, b -> suggestMatching(context.source.server.playerNames, b) }
                    .executes(this::exilePlayer)))
            // Add exile with duration
            .then(CommandManager.literal("set")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .suggests { context, b -> suggestMatching(context.source.server.playerNames, b) }
                    .then(CommandManager.argument("duration count", IntegerArgumentType.integer(1, 100))
                        .then(CommandManager.argument("duration type", StringArgumentType.word())
                            .suggests { _, b -> suggestMatching(arrayOf(
                                "seconds",
                                "minutes",
                                "hours",
                                "days"
                            ), b) }
                            .executes(this::exilePlayerWithExpiration)
                        )
                    )
                )
            )
            // Remove exile player
            .then(CommandManager.literal("remove")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .suggests { context, b -> suggestMatching(context.source.server.playerNames, b) }
                    .executes(this::removeExileFromPlayer)))

        dispatcher.register(command)
    }

    private fun listExiledPlayers(context: CommandContext<ServerCommandSource>): Int {

        val player = context.source.player
        player.sendMessage(LiteralText("==================== Exiled players ====================").formatted(Formatting.GOLD), false)

        val namespaceIdentifier = Identifier("exile", "exiles")
        val exileNamespace = context.source.server.dataCommandStorage.get(namespaceIdentifier)

        exileNamespace.keys.forEach {
            val exiledUntil = exileNamespace.getCompound(it).getString("exiled_until")

            val message = LiteralText("$it ").formatted(Formatting.GOLD)

            if (exiledUntil.equals("-2"))
                return 0
            else if (exiledUntil.equals("-1"))
                message.append(LiteralText("until pardoned").formatted(Formatting.WHITE))
            else
                message.append(LiteralText("until ${Date(exiledUntil.toLong())}").formatted(Formatting.WHITE))

            player.sendMessage(message, false)
        }

        return 0
    }

    private fun exilePlayer(context: CommandContext<ServerCommandSource>): Int {
        val player = EntityArgumentType.getPlayer(context, "player")

        setPlayerExileStatus(context.source.server.dataCommandStorage, player, -1)

        context.source.player.sendMessage(LiteralText("Player ${player.entityName} is now an exile").formatted(Formatting.GREEN), false)
        return 0
    }

    private fun exilePlayerWithExpiration(context: CommandContext<ServerCommandSource>): Int {
        val player = EntityArgumentType.getPlayer(context, "player")
        val count = IntegerArgumentType.getInteger(context, "duration count")
        val type = StringArgumentType.getString(context, "duration type").toString()
        val cal = Calendar.getInstance()
        cal.time = Date()

        val time: Long = when (type) {
            "seconds" -> {
                cal.add(Calendar.SECOND, count)
                cal.time.time
            }
            "minutes" -> {
                cal.add(Calendar.MINUTE, count)
                cal.time.time
            }
            "hours" -> {
                cal.add(Calendar.HOUR, count)
                cal.time.time
            }
            "days" -> {
                cal.add(Calendar.DATE, count)
                cal.time.time
            }
            else -> -2
        }
        if (time == -2L) {
            context.source.player.sendMessage(LiteralText("Invalid duration type, must be \"seconds\", \"minutes\", \"hours\", \"days\""), false)
            return 0
        }

        context.source.player.sendMessage(LiteralText("Player has been exiled until ${Date.from(Instant.ofEpochMilli(time))}"), false)

        setPlayerExileStatus(context.source.server.dataCommandStorage, player, time)
        context.source.player.sendMessage(LiteralText("Player ${player.entityName} is now an exile").formatted(Formatting.GREEN), false)
        return 0
    }

    private fun removeExileFromPlayer(context: CommandContext<ServerCommandSource>): Int {
        val player = EntityArgumentType.getPlayer(context, "player")

        val storage = context.source.server.dataCommandStorage
        val exiles = storage.get(Identifier("exile", "exiles"))

        exiles.remove(player.entityName)
        storage.set(Identifier("exile", "exiles"), exiles)
        context.source.player.sendMessage(LiteralText("Player ${player.entityName} is no longer an exile").formatted(Formatting.GREEN), false)
        return 0
    }
}