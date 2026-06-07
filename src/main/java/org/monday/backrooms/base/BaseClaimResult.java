package org.monday.backrooms.base;

public record BaseClaimResult(
        BaseClaimStatus status,
        BaseDefinition definition,
        BaseClaim claim
) {

    public static BaseClaimResult failed(BaseClaimStatus status, BaseDefinition definition, BaseClaim claim) {
        return new BaseClaimResult(status, definition, claim);
    }

    public static BaseClaimResult success(BaseDefinition definition, BaseClaim claim) {
        return new BaseClaimResult(BaseClaimStatus.SUCCESS, definition, claim);
    }
}
