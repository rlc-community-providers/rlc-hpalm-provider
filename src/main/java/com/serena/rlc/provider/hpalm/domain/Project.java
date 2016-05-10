/*
 *
 * Copyright (c) 2016 SERENA Software, Inc. All Rights Reserved.
 *
 * This software is proprietary information of SERENA Software, Inc.
 * Use is subject to license terms.
 *
 */

package com.serena.rlc.provider.hpalm.domain;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author klee
 */

@XmlRootElement
public class Project extends ALMObject {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(Project.class);

    public static List<Project> parse(String options) {
        List<Project> list = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            JSONObject jsonObject = (JSONObject) ((JSONObject) parsedObject).get("Projects");
            JSONArray array = (JSONArray) jsonObject.get("Project");
            for (Object object : array) {
                Project obj = new Project();
                JSONObject projObj = (JSONObject) object;
                obj.setName((String) projObj.get("Name"));
                list.add(obj);
            }
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }

        return list;
    }

    public static Project parseSingle(String options) {
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            JSONObject jsonObject = (JSONObject) parsedObject;
            Project project = parseSingle(jsonObject);
            return project;
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }
        return null;
    }

    public static Project parseSingle(JSONObject jsonObject) {
        Project obj = new Project();
        if (jsonObject != null) {
            obj.setName((String) jsonObject.get("Name"));
        }
        return obj;
    }

}