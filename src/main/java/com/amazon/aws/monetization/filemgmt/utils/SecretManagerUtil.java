package com.amazon.aws.monetization.filemgmt.utils;

import com.amazon.aws.monetization.filemgmt.NotificationException;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.DecryptionFailureException;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InternalServiceErrorException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;

import java.security.InvalidParameterException;

@Slf4j
public class SecretManagerUtil {

    /**
     * Gets the webhook URL from secret
     * @return
     */
    public static String getSecretFromSecretManager(AWSSecretsManager secretsManagerClient, String secretName, String secretKey) {
        String secret = getSecretString(secretsManagerClient, secretName);
        return new JsonParser().parse(secret).getAsJsonObject().get(secretKey).getAsString();
    }

    /**
     * Gets the secret string from secret manager
     * @return
     */
    private static String getSecretString(AWSSecretsManager secretsManagerClient, String secretName) {
        log.info("Getting secret string for secret");
        String secret = null;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);

        GetSecretValueResult getSecretValueResult = null;

        try {
            getSecretValueResult = secretsManagerClient.getSecretValue(getSecretValueRequest);
        } catch (DecryptionFailureException | InternalServiceErrorException | InvalidParameterException |
                 InvalidRequestException | ResourceNotFoundException e) {
            log.error("An error occurred while retrieving the secret");
            throw e;
        }

        if (getSecretValueResult.getSecretString() != null) {
            secret = getSecretValueResult.getSecretString();
        }

        if (secret == null) {
            log.error("getSecretValueResult.getSecretString() returned null");
            throw new NotificationException();
        }

        return secret;
    }
}
