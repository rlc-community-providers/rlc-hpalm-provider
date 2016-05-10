/*
 *
 * Copyright (c) 2016 SERENA Software, Inc. All Rights Reserved.
 *
 * This software is proprietary information of SERENA Software, Inc.
 * Use is subject to license terms.
 *
 */

package com.serena.rlc.provider.hpalm.domain;

import java.io.Serializable;

/**
 *
 * @author klee
 */
public class ALMObject implements Serializable {

    private static final long serialVersionUID = 1L;


    private String id;
    private String name;
    private String description;
    private Long created;

    public ALMObject() {

    }

    public ALMObject(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getCreated() {
        return created;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return super.toString();
    }

}