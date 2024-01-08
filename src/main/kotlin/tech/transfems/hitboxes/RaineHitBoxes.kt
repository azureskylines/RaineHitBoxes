package tech.transfems.hitboxes

import net.minecraft.client.Minecraft
import net.minecraft.entity.EntityLivingBase
import net.minecraft.potion.Potion
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

    object Config {
        var masterToggle: Boolean = false
        var expand: Double = 0.0
        var healthEnabled: Boolean = false
        var speedEnabled: Boolean = false
    }

    private val cfgFile: File by lazy { Paths.get("RaineHitBoxes.config").toFile() }
    private val addChatMessage: (String) -> Unit = {
        Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText(it))
    }

    var expandAmount: Double
        get() = if (Config.masterToggle) Config.expand else 0.0
        set(value) { Config.expand = value }

    var healthEnabled: Boolean
        get() = Config.masterToggle && Config.healthEnabled
        set(value) { Config.healthEnabled = value }

    var speedEnabled: Boolean
        get() = Config.masterToggle && Config.speedEnabled
        set(value) { Config.speedEnabled = value }

    private fun save() {
        if (!cfgFile.exists()) cfgFile.createNewFile()

        val config = arrayOf(Config.masterToggle, Config.expand.toString(), Config.healthEnabled.toString(), Config.speedEnabled.toString())

        val writer = BufferedWriter(FileWriter(cfgFile))
        writer.write(config.joinToString("|"))
        writer.close()
    }

    private fun load() {
        if (!cfgFile.exists()) return

        try {
            val reader = BufferedReader(FileReader(cfgFile))
            val config = reader.lines().findFirst().get().split("|")
            reader.close()

            Config.masterToggle = config[0].toBoolean()
            Config.expand = config[1].toDouble()
            Config.healthEnabled = config[2].toBoolean()
            Config.speedEnabled = config[3].toBoolean()
        } catch (e: Throwable) {
            println("[RaineHitBoxes] Failed to load config, resetting it :3")
            Config.masterToggle = false
            Config.expand = 0.0
            Config.healthEnabled = false
            Config.speedEnabled = false
        }
    }

    const val SYNTAX: String = "/rainehitboxes [speed|health|<expand amount>]"

    fun init() {
        println("[RaineHitBoxes] Cooked.")
        load()

        CommandBus.register(object : Command("rainehitboxes", "rhb") {
            override fun handle(args: Array<out String>) {
                if (args.isEmpty()) {
                    Config.masterToggle = !Config.masterToggle
                    addChatMessage("The mod has been ${if (Config.masterToggle) "enabled" else "disabled"}.")
                }

                if (args.size != 1) {
                    addChatMessage("Syntax: $SYNTAX")
                    return
                }

                when (args[0]) {
                    "health" -> {
                        Config.healthEnabled = !Config.healthEnabled
                        addChatMessage("Health sorting has been ${if (Config.healthEnabled) "enabled" else "disabled"}.")
                    }
                    "speed" -> {
                        Config.speedEnabled = !Config.speedEnabled
                        addChatMessage("Speed II check has been ${if (Config.speedEnabled) "enabled" else "disabled"}.")
                    }
                    else -> {
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

                            Config.expand = amount

                            addChatMessage("HitBoxes are now expanded $amount blocks.")

                            save()
                        } catch (e: Throwable) {
                            addChatMessage("Error. Syntax: $SYNTAX")
                        }
                    }
                }
            }
        })
    }

}

fun EntityLivingBase.potionAmp(potion: Potion): Int {
    return (this.getActivePotionEffect(potion) ?: return 0).amplifier + 1
}