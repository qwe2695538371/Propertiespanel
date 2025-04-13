package qwedshuxingmianban.attribute;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import qwedshuxingmianban.Qwedshuxingmianban;
import qwedshuxingmianban.config.ModConfig;
import qwedshuxingmianban.network.NetworkHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AttributeManager {
    public static final Map<String, RegistryEntry<EntityAttribute>> ATTRIBUTES = new HashMap<>();
    private static final Map<String, UUID> MODIFIER_UUIDS = new HashMap<>();

    static {
        ATTRIBUTES.put("max_health", EntityAttributes.GENERIC_MAX_HEALTH);
        ATTRIBUTES.put("armor", EntityAttributes.GENERIC_ARMOR);
        ATTRIBUTES.put("armor_toughness", EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
        ATTRIBUTES.put("attack_damage", EntityAttributes.GENERIC_ATTACK_DAMAGE);
        ATTRIBUTES.put("attack_speed", EntityAttributes.GENERIC_ATTACK_SPEED);
        ATTRIBUTES.put("movement_speed", EntityAttributes.GENERIC_MOVEMENT_SPEED);
        ATTRIBUTES.put("knockback_resistance", EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
        ATTRIBUTES.put("luck", EntityAttributes.GENERIC_LUCK);

        for (String attr : ATTRIBUTES.keySet()) {
            MODIFIER_UUIDS.put(attr, UUID.nameUUIDFromBytes(("qwedshuxingmianban:" + attr).getBytes()));
        }
    }

    public static void applyAttributeModifier(PlayerEntity player, String attributeId, int level) {
        RegistryEntry<EntityAttribute> attributeEntry = ATTRIBUTES.get(attributeId);
        if (attributeEntry == null) return;

        EntityAttributeInstance instance = player.getAttributeInstance(attributeEntry);
        if (instance == null) return;

        net.minecraft.util.Identifier modifierId = net.minecraft.util.Identifier.of("qwedshuxingmianban", attributeId);
        EntityAttributeModifier existingModifier = instance.getModifier(modifierId);
        if (existingModifier != null) {
            instance.removeModifier(existingModifier);
        }

        ModConfig.AttributeConfig config = Qwedshuxingmianban.CONFIG.attributes.get(attributeId);
        double value = config.valuePerLevel * level;

        // 更新为新的构造函数格式
        EntityAttributeModifier modifier = new EntityAttributeModifier(
                modifierId,  // 使用 Identifier 而不是 UUID
                value,      // 属性值
                getOperationForAttribute(attributeId)  // 操作类型
        );
        instance.addPersistentModifier(modifier);

        if (!player.getWorld().isClient() && player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            NetworkHandler.sendAttributeSync(serverPlayer);

            Qwedshuxingmianban.LOGGER.info("属性 {} 已修改，等级: {}, 值: {}",
                    attributeId, level, player.getAttributeValue(attributeEntry));
        }
    }

    // 其余方法保持不变，因为它们的逻辑不依赖于版本特定的API
    private static EntityAttributeModifier.Operation getOperationForAttribute(String attributeId) {
        return EntityAttributeModifier.Operation.ADD_VALUE;  // 新的枚举值名称
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
        return (int) (totalExp * 0.7);
    }

    public static EntityAttribute getAttributeById(String attributeId) {
        RegistryEntry<EntityAttribute> entry = ATTRIBUTES.get(attributeId);
        return entry != null ? entry.value() : null;
    }

    public static void resetAttributeModifiers(PlayerEntity player) {
        for (Map.Entry<String, RegistryEntry<EntityAttribute>> entry : ATTRIBUTES.entrySet()) {
            String attributeId = entry.getKey();
            RegistryEntry<EntityAttribute> attributeEntry = entry.getValue();

            EntityAttributeInstance instance = player.getAttributeInstance(attributeEntry);
            if (instance != null) {
                // 使用 Identifier.of() 静态方法创建标识符
                net.minecraft.util.Identifier modifierId = net.minecraft.util.Identifier.of("qwedshuxingmianban", attributeId);
                EntityAttributeModifier existingModifier = instance.getModifier(modifierId);
                if (existingModifier != null) {
                    instance.removeModifier(existingModifier);
                }
            }
        }
    }
}