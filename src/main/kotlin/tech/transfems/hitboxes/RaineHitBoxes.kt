package tech.transfems.hitboxes

import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText
import net.weavemc.loader.api.command.Command
import net.weavemc.loader.api.command.CommandBus
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Paths
import kotlin.math.abs

object RaineHitBoxes {

    private val cfgFile: File by lazy { Paths.get("RaineHitBoxes.config").toFile() }
    private val addChatMessage: (String) -> Unit = {
        Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText(it))
    }

    var expandAmount: Double = 0.0
    var enabled: Boolean = false

    private fun save() {
        if (!cfgFile.exists()) cfgFile.createNewFile()

        val config = arrayOf(expandAmount.toString(), enabled.toString())

        val writer = BufferedWriter(FileWriter(cfgFile))
        writer.write(config.joinToString("|"))
        writer.close()
    }

    private fun load() {
        if (!cfgFile.exists()) return

        val reader = BufferedReader(FileReader(cfgFile))
        val config = reader.lines().findFirst().get().split("|")
        reader.close()

        expandAmount = config[0].toDouble()
        enabled = config[1].toBoolean()
    }

    fun init() {
        println("[RaineHitBoxes] Cooked.")
        load()

        CommandBus.register(object : Command("rainehitboxes", "rhb") {
            override fun handle(args: Array<out String>) {
                if (args.isEmpty()) {
                    enabled = !enabled
                    addChatMessage("The mod has been ${if (enabled) "enabled" else "disabled"}.")
                    save()
                    return
                }

                if (args.size != 1) {
                    addChatMessage("Syntax: /rainehitboxes [expand amount]")
                    return
                }

                try {
                    val amount = args[0].toDoubleOrNull()

                    if (amount == null) {
                        addChatMessage("Error. Expand amount has to be a decimal number.")
                        return
                    }

                    if (amount < 0) {
                        addChatMessage("Error. Expand amount cannot be negative.")
                        return
                    }

                    expandAmount = amount

                    addChatMessage("Hitboxes are now expanded $expandAmount blocks.")

                    save()
                } catch (e: Throwable) {
                    addChatMessage("Error. Syntax: /rainehitboxes [expand amount]")
                }
            }
        })
    }

}