package qwedshuxingmianban.data;

import java.util.HashMap;
import java.util.Map;

public class PlayerAttributeData {
    private final Map<String, Integer> attributeLevels = new HashMap<>();
    private boolean changed = false; // 添加变化标记

    public void setAttributeLevel(String attributeId, int level) {
        // 检查值是否真的改变了
        Integer currentLevel = attributeLevels.get(attributeId);
        if (currentLevel == null || currentLevel != level) {
            attributeLevels.put(attributeId, level);
            changed = true;
        }
    }

    public int getAttributeLevel(String attributeId) {
        return attributeLevels.getOrDefault(attributeId, 0);
    }

    public Map<String, Integer> getAllLevels() {
        return new HashMap<>(attributeLevels);
    }

    public void resetAllAttributes() {
        if (!attributeLevels.isEmpty()) {
            attributeLevels.clear();
            changed = true;
        }
    }

    // 新增：检查数据是否发生变化
    public boolean hasChanged() {
        return changed;
    }

    // 新增：重置变化标记
    public void resetChanged() {
        changed = false;
    }

    // 新增：从现有数据更新属性
    public void updateFromMap(Map<String, Integer> data) {
        boolean hasChanges = false;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            Integer currentLevel = attributeLevels.get(entry.getKey());
            if (currentLevel == null || !currentLevel.equals(entry.getValue())) {
                hasChanges = true;
                attributeLevels.put(entry.getKey(), entry.getValue());
            }
        }
        if (hasChanges) {
            changed = true;
        }
    }
}