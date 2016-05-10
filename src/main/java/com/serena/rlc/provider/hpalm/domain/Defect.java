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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author klee
 */

public class Defect extends ALMObject {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger(Defect.class);

    private String status;
    private String url;
    private String owner;
    private String project;
    private String priority;
    private String severity;
    private String type;
    private String dateCreated;
    private String creator;
    private String lastUpdated;
    private String assignee;
    private String dueDate;
    private String estimatedEffort;
    private String actualEffort;
    private String subject;
    private String targetRel;

    public Defect() {

    }

    public Defect(String id, String name, String description, String status, String url) {
        this.setId(id);
        this.setName(name);
        this.setDescription(description);
        this.setStatus(status);
        this.setUrl(url);
        this.setType("Defect");
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }
    public String getProject() {
        return project;
    }
    public void setProject(String project) {
        this.project = project;
    }
    public String getPriority() {
        return priority;
    }
    public void setPriority(String priority) {
        this.priority = priority;
    }
    public String getSeverity() {
        return severity;
    }
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getDateCreated() {
        return dateCreated;
    }
    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }
    public String getCreator() {
        return creator;
    }
    public void setCreator(String creator) {
        this.creator = creator;
    }
    public String getLastUpdated() {
        return lastUpdated;
    }
    public void setLastUpdated(String lastModifier) {
        this.lastUpdated = lastModifier;
    }
    public String getAssignee() {
        return assignee;
    }
    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }
    public String getDueDate() {
        return dueDate;
    }
    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
    public String getEstimatedEffort() {
        return estimatedEffort;
    }
    public void setEstimatedEffort(String estimatedEffort) {
        this.estimatedEffort = estimatedEffort;
    }
    public String getActualEffort() {
        return actualEffort;
    }
    public void setActualEffort(String actualEffort) {
        this.actualEffort = actualEffort;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public String getTargetRelease() {
        return targetRel;
    }
    public void setTargetRelease(String targetRel) {
        this.targetRel = targetRel;
    }

    public static Defect parseSingle(String options) {
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            JSONObject jsonObject = (JSONObject) parsedObject;
            Defect defect = parseSingle(jsonObject);
            return defect;
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }
        return null;
    }

    public static List<Defect> parse(String options) {
        List<Defect> list = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            Object parsedObject = parser.parse(options);
            JSONArray array = (JSONArray) ((JSONObject) parsedObject).get("entities");
            for (Object object : array) {
                JSONObject jsonObject = (JSONObject) object;
                Defect obj = parseSingle(jsonObject);
                String defectType = jsonObject.get("Type").toString();
                obj.setType(defectType);
                list.add(obj);
            }
        } catch (ParseException e) {
            logger.error("Error while parsing input JSON - " + options, e);
        }

        return list;
    }

    public static Defect parseSingle(JSONObject jsonObject) {
        Defect obj = new Defect();
        if (jsonObject != null) {
            JSONArray fieldsArray = (JSONArray) jsonObject.get("Fields");
            for (Object defect : fieldsArray) {
                JSONObject defectObject = (JSONObject) defect;
                String fieldName = defectObject.get("Name").toString();
                JSONArray fieldValueArray  = (JSONArray) defectObject.get("values");
                switch (fieldName) {
                    case "id":
                        obj.setId(getFieldValue((JSONObject) fieldValueArray.get(0)));
                        break;
                    case "name":
                        obj.setName(getFieldValue((JSONObject) fieldValueArray.get(0)));
                        break;
                    case "status":
                        obj.setStatus(getFieldValue((JSONObject) fieldValueArray.get(0)));
                        break;
                    case "priority":
                        obj.setPriority(getFieldValue((JSONObject) fieldValueArray.get(0)));
                        break;
                    case "severity":
                        obj.setSeverity(getFieldValue((JSONObject) fieldValueArray.get(0)));
                        break;
                    case "description":
                        obj.setDescription(getFieldValue((JSONObject) fieldValueArray.get(0)));
                        break;
                    case "project":
                        obj.setProject(getFieldValue((JSONObject) fieldValueArray.get(0)));
                        break;
                    case "detected-by":
                        obj.setCreator(getFieldValue((JSONObject) fieldValueArray.get(0)));
                        break;
                    case "creation-time":
                        obj.setDateCreated(getFieldValue((JSONObject) fieldValueArray.get(0)));
                        break;
                    case "owner":
                        obj.setOwner(getFieldValue((JSONObject) fieldValueArray.get(0)));
                        break;
                    case "last-modified":
                        obj.setLastUpdated(getFieldValue((JSONObject) fieldValueArray.get(0)));
                        break;
                    case "target-rcyc":
                        obj.setTargetRelease(getFieldValue((JSONObject) fieldValueArray.get(0)));
                        break;
                    default:
                        break;
                }
            }
        }
        return obj;
    }

    private static String getFieldValue(JSONObject jsonObject) {
        if (jsonObject.get("value") == null)
            return "";
        else
            return jsonObject.get("value").toString();
    }

}