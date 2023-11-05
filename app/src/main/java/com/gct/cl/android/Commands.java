package com.gct.cl.android;

import java.util.Arrays;

public enum Commands {
    ADMIN_CHANNELS_CREATE(new String[]{"ADMIN", "CHANNELS", "CREATE"}, null),
    ADMIN_CHANNELS_DELETE(new String[]{"ADMIN", "CHANNELS", "DELETE"}, null),
    ADMIN_CHANNELS_USERS_ADD(new String[]{"ADMIN", "CHANNELS", "USERS", "ADD"}, null),
    ADMIN_CHANNELS_USERS_REMOVE(new String[]{"ADMIN", "CHANNELS", "USERS", "REMOVE"}, null),

    ADMIN_CHANNELS_SETTINGS_EXTERNAL_CHANGE_TITLE(new String[]{"ADMIN", "CHANNELS", "SETTINGS", "EXTERNAL", "CHANGE", "TITLE"}, null),
    ADMIN_CHANNELS_SETTINGS_EXTERNAL_CHANGE_DESCRIPTION(new String[]{"ADMIN", "CHANNELS", "SETTINGS", "EXTERNAL", "CHANGE", "DESCRIPTION"}, null),

    USER_CHANNELS_JOIN(new String[]{"USER", "CHANNELS", "JOIN"}, null),
    USER_CHANNELS_LEAVE(new String[]{"USER", "CHANNELS", "LEAVE"}, null),

    USER_CHANNELS_MESSAGES_POST_NEW(new String[]{"USER", "CHANNELS", "MESSAGES", "POST", "NEW"}, CommandPackets.Channels.Messages.Post.New.class),
    USER_CHANNELS_MESSAGES_POST_FORWARD_MESSAGE(new String[]{"USER", "CHANNELS", "MESSAGES", "POST", "FORWARD", "MESSAGE"}, null),
    USER_CHANNELS_MESSAGES_POST_FORWARD_POST(new String[]{"USER", "CHANNELS", "MESSAGES", "POST", "FORWARD", "POST"}, null),
    USER_CHANNELS_MESSAGES_EDIT(new String[]{"USER", "CHANNELS", "MESSAGES", "EDIT"}, null),
    USER_CHANNELS_MESSAGES_DELETE(new String[]{"USER", "CHANNELS", "MESSAGES", "DELETE"}, null),

    USER_CHANNELS_MESSAGES_REACTIONS_ADD(new String[]{"USER", "CHANNELS", "MESSAGES", "REACTIONS", "ADD"}, null),
    USER_CHANNELS_MESSAGES_REACTIONS_REMOVE(new String[]{"USER", "CHANNELS", "MESSAGES", "REACTIONS", "REMOVE"}, null),

    SYSTEM_CHANNELS_LISTEN_ADD(new String[]{"SYSTEM", "CHANNELS", "LISTEN", "ADD"}, CommandPackets.Systems.Listen.Add.class),
    SYSTEM_CHANNELS_LISTEN_REMOVE(new String[]{"SYSTEM", "CHANNELS", "LISTEN", "REMOVE"}, CommandPackets.Systems.Listen.Remove.class);

    final String[] intents;
    final Class<?> pattern;

    Commands (String[] intents, Class<?> pattern) {
        this.intents = intents;
        this.pattern = pattern;
    }

    public static Commands byIntents (String[] intents) {
        return Arrays.stream(Commands.values())
                .filter(v -> Arrays.equals(v.intents, intents))
                .findFirst()
                .orElse(null);
    }
}

