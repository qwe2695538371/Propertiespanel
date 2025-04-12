属性面板，消耗经验加点各种属性，让我的世界如同rpg游戏一般。
默认配置文件对应中文：
{
  // 属性配置部分
  "attributes": {
    // 击退抗性属性配置
    "knockback_resistance": {
      "baseExperienceCost": 5,      // 基础升级所需经验值
      "experienceIncrement": 2,      // 每级增加的经验值消耗
      "maxLevel": 20,               // 最大等级
      "valuePerLevel": 0.05,         // 每级增加的属性值
      "displayName": "击退抗性"      // 显示名称
    },
    // 最大生命值属性配置
    "max_health": {
      "baseExperienceCost": 5,      // 基础升级所需经验值
      "experienceIncrement": 2,      // 每级增加的经验值消耗
      "maxLevel": 20,               // 最大等级
      "valuePerLevel": 1.0,         // 每级增加的生命值
      "displayName": "最大生命值"    // 显示名称
    },
    // 护甲值属性配置
    "armor": {
      "baseExperienceCost": 5,      // 基础升级所需经验值
      "experienceIncrement": 2,      // 每级增加的经验值消耗
      "maxLevel": 20,               // 最大等级
      "valuePerLevel": 1.0,         // 每级增加的护甲值
      "displayName": "护甲值"        // 显示名称
    },
    // 幸运属性配置
    "luck": {
      "baseExperienceCost": 5,      // 基础升级所需经验值
      "experienceIncrement": 2,      // 每级增加的经验值消耗
      "maxLevel": 20,               // 最大等级
      "valuePerLevel": 0.1,         // 每级增加的幸运值
      "displayName": "幸运"          // 显示名称
    },
    // 攻击伤害属性配置
    "attack_damage": {
      "baseExperienceCost": 5,      // 基础升级所需经验值
      "experienceIncrement": 2,      // 每级增加的经验值消耗
      "maxLevel": 20,               // 最大等级
      "valuePerLevel": 0.1,         // 每级增加的攻击伤害
      "displayName": "攻击伤害"      // 显示名称
    },
    // 攻击速度属性配置
    "attack_speed": {
      "baseExperienceCost": 5,      // 基础升级所需经验值
      "experienceIncrement": 2,      // 每级增加的经验值消耗
      "maxLevel": 20,               // 最大等级
      "valuePerLevel": 0.2,         // 每级增加的攻击速度
      "displayName": "攻击速度"      // 显示名称
    },
    // 盔甲韧性属性配置
    "armor_toughness": {
      "baseExperienceCost": 5,      // 基础升级所需经验值
      "experienceIncrement": 2,      // 每级增加的经验值消耗
      "maxLevel": 20,               // 最大等级
      "valuePerLevel": 1.0,         // 每级增加的盔甲韧性
      "displayName": "盔甲韧性"      // 显示名称
    },
    // 移动速度属性配置
    "movement_speed": {
      "baseExperienceCost": 5,      // 基础升级所需经验值
      "experienceIncrement": 2,      // 每级增加的经验值消耗
      "maxLevel": 20,               // 最大等级
      "valuePerLevel": 0.002,         // 每级增加的移动速度
      "displayName": "移动速度"      // 显示名称
    }
  },
  // 通用配置部分
  "general": {
    "allowReset": true,             // 是否允许重置属性
    "resetCostPercentage": 30.0,    // 重置属性需要消耗的经验值百分比
    "attributePriority": 800        // 属性修改器的优先级
  }
}
