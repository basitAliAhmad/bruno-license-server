package com.bebasit.brunolicenseserver.exception;

import java.util.UUID;

public class NoActivationExistException extends RuntimeException {
    public NoActivationExistException(UUID activationId) {
        super("No activation exist for activationId: " + activationId);
    }
}
