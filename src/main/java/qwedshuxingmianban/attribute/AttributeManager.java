package qwedshuxingmianban.attribute;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import qwedshuxingmianban.Qwedshuxingmianban;
import qwedshuxingmianban.config.ModConfig;
import qwedshuxingmianban.network.NetworkHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AttributeManager {
    // 改为 public static final
    public static final Map<String, EntityAttribute> ATTRIBUTES = new HashMap<>();
    private static final Map<String, UUID> MODIFIER_UUIDS = new HashMap<>();

    static {
        // 验证属性映射是否正确
        ATTRIBUTES.put("max_health", EntityAttributes.GENERIC_MAX_HEALTH);           // 生命值
        ATTRIBUTES.put("armor", EntityAttributes.GENERIC_ARMOR);                     // 护甲值
        ATTRIBUTES.put("armor_toughness", EntityAttributes.GENERIC_ARMOR_TOUGHNESS); // 护甲韧性
        ATTRIBUTES.put("attack_damage", EntityAttributes.GENERIC_ATTACK_DAMAGE);     // 攻击伤害
        ATTRIBUTES.put("attack_speed", EntityAttributes.GENERIC_ATTACK_SPEED);       // 攻击速度
        ATTRIBUTES.put("movement_speed", EntityAttributes.GENERIC_MOVEMENT_SPEED);   // 移动速度
        ATTRIBUTES.put("knockback_resistance", EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE); // 击退抗性
        ATTRIBUTES.put("luck", EntityAttributes.GENERIC_LUCK);                       // 幸运

        // 生成唯一的UUID
        for (String attr : ATTRIBUTES.keySet()) {
            MODIFIER_UUIDS.put(attr, UUID.nameUUIDFromBytes(("qwedshuxingmianban:" + attr).getBytes()));
        }
    }

    public static void applyAttributeModifier(PlayerEntity player, String attributeId, int level) {
        EntityAttribute attribute = ATTRIBUTES.get(attributeId);
        if (attribute == null) return;

        EntityAttributeInstance instance = player.getAttributeInstance(attribute);
        if (instance == null) return;

        // 移除旧的修改器
        UUID modifierId = MODIFIER_UUIDS.get(attributeId);
        instance.removeModifier(modifierId);

        // 计算新的属性值
        ModConfig.AttributeConfig config = Qwedshuxingmianban.CONFIG.attributes.get(attributeId);
        double value = config.valuePerLevel * level;

        // 添加新的修改器
        EntityAttributeModifier modifier = new EntityAttributeModifier(
                modifierId,
                "属性面板加成",
                value,
                getOperationForAttribute(attributeId)
        );
        instance.addPersistentModifier(modifier);

        // 如果在服务器端，强制同步属性
        if (!player.getWorld().isClient() && player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            // 同步属性修改到客户端
            NetworkHandler.sendAttributeSync(serverPlayer);

            // 添加调试日志
            Qwedshuxingmianban.LOGGER.info("属性 {} 已修改，等级: {}, 值: {}",
                    attributeId, level, player.getAttributeValue(attribute));
        }
    }
    private static EntityAttributeModifier.Operation getOperationForAttribute(String attributeId) {
        if ("attack_damage".equals(attributeId)) {
            return EntityAttributeModifier.Operation.MULTIPLY_BASE;
        }
        return EntityAttributeModifier.Operation.ADDITION;
    }

    public static int calculateExperienceCost(String attributeId, int currentLevel) {
        ModConfig.AttributeConfig config = Qwedshuxingmianban.CONFIG.attributes.get(attributeId);
        return config.baseExperienceCost + (currentLevel * config.experienceIncrement);
    }

    public static int calculateTotalExperience(Map<String, Integer> levels) {
        int total = 0;
        for (Map.Entry<String, Integer> entry : levels.entrySet()) {
            String attributeId = entry.getKey();
            int level = entry.getValue();
            ModConfig.AttributeConfig config = Qwedshuxingmianban.CONFIG.attributes.get(attributeId);

            for (int i = 0; i < level; i++) {
                total += config.baseExperienceCost + (i * config.experienceIncrement);
            }
        }
        return total;
    }

    public static int calculateRefundExperience(int totalExp) {
        // 计算70%的经验返还（向下取整）
        return (int) (totalExp * 0.7);
    }
    public static EntityAttribute getAttributeById(String attributeId) {
        return ATTRIBUTES.get(attributeId);
    }
    /**
     * 重置玩家的所有属性修改器
     */
    public static void resetAttributeModifiers(PlayerEntity player) {
        // 遍历所有注册的属性
        for (Map.Entry<String, EntityAttribute> entry : ATTRIBUTES.entrySet()) {
            String attributeId = entry.getKey();
            EntityAttribute attribute = entry.getValue();

            EntityAttributeInstance instance = player.getAttributeInstance(attribute);
            if (instance != null) {
                // 获取对应的修改器 UUID
                UUID modifierId = MODIFIER_UUIDS.get(attributeId);
                // 移除修改器
                instance.removeModifier(modifierId);
            }
        }
    }

}