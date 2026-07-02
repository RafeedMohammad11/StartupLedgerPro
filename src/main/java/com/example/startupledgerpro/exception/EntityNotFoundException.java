package com.example.startupledgerpro.exception;

public class EntityNotFoundException extends RuntimeException {
    private final String entityType;
    private final String entityId;

    public EntityNotFoundException(String entityType, String entityId) {
        super(entityType + " not found with ID: " + entityId);
        this.entityType = entityType;
        this.entityId   = entityId;
    }

    public String getEntityType() { return entityType; }
    public String getEntityId()   { return entityId; }
}