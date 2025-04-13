package qwedshuxingmianban.client;

import java.util.HashMap;
import java.util.Map;

public class ClientAttributeData {
    private static final Map<String, Integer> attributeLevels = new HashMap<>();
    private static final Map<String, Double> attributeValues = new HashMap<>();
    private static int experienceLevel; // 添加经验等级字段

    public static void updateData(Map<String, Integer> levels, int expLevel, Map<String, Double> values) {
        attributeLevels.clear();
        attributeLevels.putAll(levels);
        experienceLevel = expLevel; // 更新经验等级
        attributeValues.clear();
        attributeValues.putAll(values);
    }

    public static int getAttributeLevel(String attributeId) {
        return attributeLevels.getOrDefault(attributeId, 0);
    }

    public static double getAttributeValue(String attributeId) {
        return attributeValues.getOrDefault(attributeId, 0.0);
    }

    // 获取经验等级的方法
    public static int getAvailableExperience() {
        return experienceLevel;
    }
}