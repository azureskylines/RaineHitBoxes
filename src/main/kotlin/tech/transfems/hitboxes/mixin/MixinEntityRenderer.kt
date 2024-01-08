package tech.transfems.hitboxes.mixin

import com.google.common.base.Predicate
import com.google.common.base.Predicates
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.EntityRenderer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.potion.Potion
import net.minecraft.util.*
import net.minecraft.util.MovingObjectPosition.MovingObjectType
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Overwrite
import org.spongepowered.asm.mixin.Shadow
import tech.transfems.hitboxes.RaineHitBoxes.expandAmount
import tech.transfems.hitboxes.RaineHitBoxes.healthEnabled
import tech.transfems.hitboxes.RaineHitBoxes.speedEnabled
import tech.transfems.hitboxes.potionAmp

@Mixin(value = [EntityRenderer::class], priority = 100)
class MixinEntityRenderer {
    @Shadow
    private var pointedEntity: Entity? = null

    @Overwrite // :trollcat: (this is pretty much fine, it's a low priority mixin)
    fun getMouseOver(partialTicks: Float) {
        val mc = Minecraft.getMinecraft()
        val entity: Entity? = mc.renderViewEntity
        if (entity != null) {
            if (mc.theWorld != null) {
                mc.mcProfiler.startSection("pick")
                mc.pointedEntity = null
                var reach: Double = mc.playerController.blockReachDistance.toDouble()
                mc.objectMouseOver = entity.rayTrace(reach, partialTicks)
                var dist = reach
                val hitOrigin = entity.getPositionEyes(partialTicks)
                var flag = false
                if (mc.playerController.extendedReach()) {
                    reach = 6.0
                    dist = 6.0
                } else {
                    if (reach > 3.0) {
                        flag = true
                    }
                }
                if (mc.objectMouseOver != null) {
                    dist = mc.objectMouseOver.hitVec.distanceTo(hitOrigin)
                }
                val lookVec = entity.getLook(partialTicks)
                val hitVec = hitOrigin.addVector(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach)
                this.pointedEntity = null
                var interceptPoint: Vec3? = null
                val entities: List<Entity> = mc.theWorld.getEntitiesInAABBexcluding(
                    entity,
                    entity.entityBoundingBox.addCoord(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach)
                        .expand(1.0, 1.0, 1.0),
                    Predicates.and(EntitySelectors.NOT_SPECTATING,
                        Predicate { it?.canBeCollidedWith() == true })
                )
                var targetDist = dist
                var targetHealth = Float.MAX_VALUE
                for (i in entities.indices) {
                    val target = entities[i]

                    if (speedEnabled && target is EntityLivingBase && target.potionAmp(Potion.moveSpeed) == 2) {
                        continue
                    }

                    val borderSize = target.collisionBorderSize
                    val targetBoundingBox = target.entityBoundingBox
                        .expand(borderSize.toDouble(), borderSize.toDouble(), borderSize.toDouble())
                        .expand(expandAmount, expandAmount, expandAmount)
                    val intercept = targetBoundingBox.calculateIntercept(hitOrigin, hitVec)
                    if (targetBoundingBox.isVecInside(hitOrigin)) {
                        if (targetDist >= 0.0 && (if (target is EntityLivingBase && healthEnabled) target.health <= targetHealth else true)) {
                            this.pointedEntity = target
                            if (target is EntityLivingBase && healthEnabled) {
                                targetHealth = target.health
                            }
                            interceptPoint = if (intercept == null) hitOrigin else intercept.hitVec
                            targetDist = 0.0
                        }
                    } else if (intercept != null) {
                        val interceptReach = hitOrigin.distanceTo(intercept.hitVec)
                        if ((if (target is EntityLivingBase&& healthEnabled) target.health < targetHealth else interceptReach < targetDist) || targetDist == 0.0) {
                            if (target === entity.ridingEntity) {
                                if (targetDist == 0.0) {
                                    this.pointedEntity = target
                                    interceptPoint = intercept.hitVec
                                    if (target is EntityLivingBase && healthEnabled) {
                                        targetHealth = target.health
                                    }
                                }
                            } else {
                                this.pointedEntity = target
                                interceptPoint = intercept.hitVec
                                targetDist = interceptReach
                                if (target is EntityLivingBase && healthEnabled) {
                                    targetHealth = target.health
                                }
                            }
                        }
                    }
                }
                if (this.pointedEntity != null && flag && hitOrigin.distanceTo(interceptPoint) > 3.0) {
                    this.pointedEntity = null
                    mc.objectMouseOver =
                        MovingObjectPosition(MovingObjectType.MISS, interceptPoint, null, BlockPos(interceptPoint))
                }
                if (this.pointedEntity != null && (targetDist < dist || mc.objectMouseOver == null)) {
                    mc.objectMouseOver = MovingObjectPosition(this.pointedEntity, interceptPoint)
                    if (this.pointedEntity is EntityLivingBase || this.pointedEntity is EntityItemFrame) {
                        mc.pointedEntity = this.pointedEntity
                    }
                }
                mc.mcProfiler.endSection()
            }
        }
    }
}
