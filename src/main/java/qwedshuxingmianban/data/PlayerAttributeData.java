package qwedshuxingmianban.data;

import java.util.HashMap;
import java.util.Map;

public class PlayerAttributeData {
    private final Map<String, Integer> attributeLevels = new HashMap<>();

    public void setAttributeLevel(String attributeId, int level) {
        attributeLevels.put(attributeId, level);
    }

    public int getAttributeLevel(String attributeId) {
        return attributeLevels.getOrDefault(attributeId, 0);
    }

    public Map<String, Integer> getAllLevels() {
        return new HashMap<>(attributeLevels);
    }

    public void resetAllAttributes() {
        attributeLevels.clear();
    }
}