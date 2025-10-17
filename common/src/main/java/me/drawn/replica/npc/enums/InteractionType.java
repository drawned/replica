package me.drawn.replica.npc.enums;

public enum InteractionType {
    LEFT_CLICK,
    RIGHT_CLICK;

    public static InteractionType get(String of) {
        InteractionType type;
        try {
            type = InteractionType.valueOf(of.toUpperCase());
            return type;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
