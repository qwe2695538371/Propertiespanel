package qwedshuxingmianban.attribute;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import qwedshuxingmianban.Qwedshuxingmianban;
import qwedshuxingmianban.config.ModConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AttributeManager {
    private static final Map<String, EntityAttribute> ATTRIBUTES = new HashMap<>();
    private static final Map<String, UUID> MODIFIER_UUIDS = new HashMap<>();

    static {
        // 初始化属性映射
        ATTRIBUTES.put("max_health", EntityAttributes.GENERIC_MAX_HEALTH);
        ATTRIBUTES.put("armor", EntityAttributes.GENERIC_ARMOR);
        ATTRIBUTES.put("armor_toughness", EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
        ATTRIBUTES.put("attack_damage", EntityAttributes.GENERIC_ATTACK_DAMAGE);
        ATTRIBUTES.put("attack_speed", EntityAttributes.GENERIC_ATTACK_SPEED);
        ATTRIBUTES.put("movement_speed", EntityAttributes.GENERIC_MOVEMENT_SPEED);
        ATTRIBUTES.put("knockback_resistance", EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
        ATTRIBUTES.put("luck", EntityAttributes.GENERIC_LUCK);

        // 为每个属性生成唯一的UUID
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
        EntityAttributeModifier oldModifier = instance.getModifier(MODIFIER_UUIDS.get(attributeId));
        if (oldModifier != null) {
            instance.removeModifier(MODIFIER_UUIDS.get(attributeId));
        }

        // 计算新的属性值
        ModConfig.AttributeConfig config = Qwedshuxingmianban.CONFIG.attributes.get(attributeId);
        double value = config.valuePerLevel * level;

        // 添加新的修改器
        EntityAttributeModifier modifier = new EntityAttributeModifier(
                MODIFIER_UUIDS.get(attributeId),
                "属性面板加成",
                value,
                EntityAttributeModifier.Operation.ADDITION
        );
        instance.addPersistentModifier(modifier);
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