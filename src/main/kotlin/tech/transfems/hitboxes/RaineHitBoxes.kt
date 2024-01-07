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

object RaineHitBoxes {

    private val cfgFile: File by lazy { Paths.get("RaineHitBoxes.config").toFile() }
    private val addChatMessage: (String) -> Unit = {
        Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText(it))
    }

    var expandAmount: Double = 0.0

    private fun save() {
        if (!cfgFile.exists()) cfgFile.createNewFile()

        val writer = BufferedWriter(FileWriter(cfgFile))
        writer.write(expandAmount.toString())
        writer.close()
    }

    private fun load() {
        if (!cfgFile.exists()) return

        val reader = BufferedReader(FileReader(cfgFile))
        expandAmount = reader.lines().findFirst().get().toDouble()
        reader.close()
    }

    fun init() {
        println("[RaineHitBoxes] Cooked.")
        load()

        CommandBus.register(object : Command("rainehitboxes", "rhb") {
            override fun handle(args: Array<out String>) {
                if (args.size != 1) {
                    addChatMessage("Syntax: /rainehitboxes <expand amount>")
                    return
                }

                try {
                    val amount = args[0].toDoubleOrNull()

                    if (amount == null) {
                        addChatMessage("Error. Expand amount has to be a decimal number.")
                        return
                    }

                    expandAmount = amount

                    addChatMessage("Hitboxes are now expanded $expandAmount blocks.")

                    save()
                } catch (e: Throwable) {
                    addChatMessage("Error. Syntax: /rainehitboxes <expand amount>")
                }
            }
        })
    }

}