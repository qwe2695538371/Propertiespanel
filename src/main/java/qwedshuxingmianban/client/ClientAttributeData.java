package qwedshuxingmianban.client;

import qwedshuxingmianban.attribute.AttributeManager;

import java.util.HashMap;
import java.util.Map;

public class ClientAttributeData {
    private static final Map<String, Integer> attributeLevels = new HashMap<>();
    private static final Map<String, Double> attributeValues = new HashMap<>();
    private static int availableExperience;

    public static void updateData(Map<String, Integer> levels, int experience, Map<String, Double> values) {
        attributeLevels.clear();
        attributeLevels.putAll(levels);
        availableExperience = experience;
        attributeValues.clear();
        // 确保所有注册的属性都有值
        AttributeManager.ATTRIBUTES.forEach((attributeId, attribute) -> {
            attributeValues.put(attributeId, values.getOrDefault(attributeId, 0.0));
        });
    }

    public static int getAttributeLevel(String attributeId) {
        return attributeLevels.getOrDefault(attributeId, 0);
    }

    public static int getAvailableExperience() {
        return availableExperience;
    }

    public static double getAttributeValue(String attributeId) {
        return attributeValues.getOrDefault(attributeId, 0.0);
    }

    public static Map<String, Integer> getAllLevels() {
        return new HashMap<>(attributeLevels);
    }
    public static void clearAttributeValues() {
        attributeValues.clear();
    }
}