package org.example;

import java.util.UUID;

public record TaskGroup(UUID groupID) {
    public TaskGroup {
        if (groupID == null) {
            throw new IllegalArgumentException("GroupID should not be null");
        }
    }

}
