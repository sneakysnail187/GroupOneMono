package main;

import java.util.UUID;

public record Participant(UUID uuid, UUID eventId, String name, String email) {
    public static Participant create(String uuid, String eventId, String name, String email) throws Event.HandledIllegalValueException {
            return new Participant(UUID.fromString(uuid), UUID.fromString(eventId), name, Event.validateEmail(email));
    }
    public static Participant create(String eventId, String name, String email) throws Event.HandledIllegalValueException {
        return Participant.create(UUID.randomUUID().toString(), eventId, name, email);
    }

}
