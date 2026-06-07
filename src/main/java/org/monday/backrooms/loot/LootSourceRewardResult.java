package org.monday.backrooms.loot;

public record LootSourceRewardResult(
        LootSourceRewardStatus status,
        LootSourceDefinition source,
        int items,
        boolean droppedLeftovers
) {

    public static LootSourceRewardResult failed(LootSourceRewardStatus status, LootSourceDefinition source) {
        return new LootSourceRewardResult(status, source, 0, false);
    }

    public static LootSourceRewardResult success(LootSourceDefinition source, int items, boolean droppedLeftovers) {
        return new LootSourceRewardResult(LootSourceRewardStatus.SUCCESS, source, items, droppedLeftovers);
    }
}
