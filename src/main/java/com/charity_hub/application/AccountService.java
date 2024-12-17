package com.charity_hub.application;

import com.charity_hub.domain.contracts.*;
import com.charity_hub.domain.models.account.Tokens;
import com.charity_hub.domain.models.account.Account;
import com.charity_hub.domain.exceptions.AppException;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AccountService {
    private final IAccountRepo accountRepo;
    private final IInvitationRepo invitationRepo;
    private final IAuthProvider authProvider;
    private final IJWTGenerator jwtGenerator;
    private final ILogger logger;

    public AccountService(
            IAccountRepo accountRepo,
            IInvitationRepo invitationRepo,
            IAuthProvider authProvider,
            IJWTGenerator jwtGenerator,
            ILogger logger
    ) {
        this.accountRepo = accountRepo;
        this.invitationRepo = invitationRepo;
        this.authProvider = authProvider;
        this.jwtGenerator = jwtGenerator;
        this.logger = logger;
    }

    public CompletableFuture<AuthenticateResponse> authenticate(Authenticate command) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Handling authentication for idToken: %s".formatted(command.idToken()));

            String mobileNumber = authProvider.getVerifiedMobileNumber(command.idToken()).join();

            logger.info("check for account: %s".formatted(command));

            Account account = existingAccountOrNewAccount(mobileNumber, command);

            logger.info("finish check for account: %s".formatted(command.idToken()));

            Tokens tokens = account.authenticate(jwtGenerator, command.deviceId(), command.deviceType());

            accountRepo.save(account);
            logger.info("Authentication successful for account: %s".formatted(account.getMobileNumber()));

            return new AuthenticateResponse(tokens.accessToken(), tokens.refreshToken());
        });
    }

    private Account existingAccountOrNewAccount(String mobileNumber, Authenticate request) {
        logger.info("get the account by mobile number");
        var account = accountRepo.getByMobileNumber(mobileNumber).join();
        if (accountExists(account)) {
            return account;
        }
        return createNewAccount(mobileNumber, request.deviceType(), request.deviceId());
    }

    private static boolean accountExists(Account existingAccount) {
        return existingAccount != null;
    }

    private Account createNewAccount(String mobileNumber, String aDeviceType, String aDeviceId) {
        boolean isAdmin = accountRepo.isAdmin(mobileNumber).join();
        boolean hasNoInvitations = !invitationRepo.hasInvitation(mobileNumber).join();
        assertIsAdminOrInvited(mobileNumber, isAdmin, hasNoInvitations);

        return Account.newAccount(mobileNumber, isAdmin, aDeviceType, aDeviceId);
    }

    private void assertIsAdminOrInvited(String mobileNumber, boolean isAdmin, boolean hasNoInvitations) {
        if (!isAdmin && hasNoInvitations) {
            logger.warn("Account not invited: %s".formatted(mobileNumber));
            throw new AppException.RequirementException("Account not invited to use the App");
        }
    }
}