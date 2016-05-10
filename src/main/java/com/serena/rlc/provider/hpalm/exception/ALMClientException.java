/*
 *
 * Copyright (c) 2016 SERENA Software, Inc. All Rights Reserved.
 *
 * This software is proprietary information of SERENA Software, Inc.
 * Use is subject to license terms.
 *
 */

package com.serena.rlc.provider.hpalm.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kevin Lee
 */

public class ALMClientException extends Exception {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ALMClientException.class);

    public ALMClientException() {
    }

    public ALMClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ALMClientException(String message) {
        super(message);
    }

    public ALMClientException(Throwable cause) {
        super(cause);
    }
}
