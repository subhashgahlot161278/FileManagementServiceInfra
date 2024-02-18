package com.amazon.aws.monetization.filemgmt.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


class S3UtilTest {
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void formatDocumentName() {
        Map<String, String> testFileMap = new HashMap<>();
        testFileMap.put("ABC / casdc hjas+", "ABC casdc hjas");
        testFileMap.put("ahsbcsbasuy6ghasc.g", "ahsbcsbasuy6ghascg");
        testFileMap.put( "Onica by Rackspace Technology - SPP0133734047641222AUSD", "Onica by Rackspace Technology - SPP0133734047641222AUSD");
        testFileMap.put("scd*4891237%hjb{/", "scd4891237hjb");

        testFileMap.forEach((inputDocumentName,expectedDocumentName) -> {
            assertEquals(expectedDocumentName, S3Util.formatDocumentName(inputDocumentName.toString()));
        });
    }
}