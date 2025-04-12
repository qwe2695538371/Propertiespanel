package qwedshuxingmianban.client;

import java.util.HashMap;
import java.util.Map;

public class ClientAttributeData {
    private static final Map<String, Integer> attributeLevels = new HashMap<>();
    private static int availableExperience = 0;

    public static void updateData(Map<String, Integer> levels, int experience) {
        attributeLevels.clear();
        attributeLevels.putAll(levels);
        availableExperience = experience;
    }

    public static int getAttributeLevel(String attributeId) {
        return attributeLevels.getOrDefault(attributeId, 0);
    }

    public static int getAvailableExperience() {
        return availableExperience;
    }

    public static Map<String, Integer> getAllLevels() {
        return new HashMap<>(attributeLevels);
    }
}