package com.charity_hub.infrastructure.services.firebase;

import com.charity_hub.domain.contracts.IAuthProvider;
import com.charity_hub.domain.contracts.ILogger;
import com.charity_hub.domain.exceptions.AppException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class FirebaseAuthProvider implements IAuthProvider {

    private final FirebaseAuth firebaseAuth;
    private final ILogger logger;

    public FirebaseAuthProvider(FirebaseAuth firebaseAuth, ILogger logger) {
        this.firebaseAuth = firebaseAuth;
        this.logger = logger;
    }

    @Override
    public CompletableFuture<String> getVerifiedMobileNumber(String idToken) {
        return CompletableFuture.supplyAsync(() -> {
            var firebaseToken = verify(idToken).join();
            try {
                var userRecord = firebaseAuth.getUser(firebaseToken.getUid());
                if (userRecord.getPhoneNumber() != null) {
                    return userRecord.getPhoneNumber().replace("+", "");
                } else {
                    logger.errorLog("Failed to verify mobile number");
                    throw new AppException.UnAuthorized();
                }
            } catch (FirebaseAuthException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private CompletableFuture<FirebaseToken> verify(String idToken) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return firebaseAuth.verifyIdToken(idToken);
            } catch (Exception authError) {
                logger.error("Failed to verify Id token: %s".formatted(idToken), authError);
                throw new AppException.UnAuthorized();
            }
        });
    }
}