/*
 *
 * Copyright (c) 2016 SERENA Software, Inc. All Rights Reserved.
 *
 * This software is proprietary information of SERENA Software, Inc.
 * Use is subject to license terms.
 *
 * @author Kevin Lee
 */
package com.serena.rlc.provider.hpalm;

import com.serena.rlc.provider.BaseRequestProvider;
import com.serena.rlc.provider.annotations.*;
import com.serena.rlc.provider.domain.*;
import com.serena.rlc.provider.exceptions.ProviderException;
import com.serena.rlc.provider.hpalm.client.ALMClient;
import com.serena.rlc.provider.hpalm.domain.Defect;
import com.serena.rlc.provider.hpalm.domain.ALMObject;
import com.serena.rlc.provider.hpalm.domain.Project;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.serena.rlc.provider.hpalm.exception.ALMClientException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class ALMRequestProvider extends BaseRequestProvider {

    final static Logger logger = LoggerFactory.getLogger(ALMRequestProvider.class);
    final static String PROJECT = "project";
    final static String STATUS_FILTERS = "statusFilters";
    final static String TITLE_FILTER = "titleFilter";

    private ALMClient almClient;
    private Integer resultLimit;


    //================================================================================
    // Configuration Properties
    // -------------------------------------------------------------------------------
    // The configuration properties are marked with the @ConfigProperty annotaion
    // and will be displayed in the provider administration page when creating a 
    // configuration of this plugin for use.
    //================================================================================



    @ConfigProperty(name = "request_provider_name", displayName = "Request Provider Name",
            description = "provider name",
            defaultValue = "HP ALM Provider",
            dataType = DataType.TEXT)
    private String providerName;

    @ConfigProperty(name = "request_provider_description", displayName = "Request Provider Description",
            description = "provider description",
            defaultValue = "",
            dataType = DataType.TEXT)
    private String providerDescription;

    @ConfigProperty(name = "hpalm_url", displayName = "HP ALM URL",
            description = "HP ALM Server URL.",
            defaultValue = "http://<servername>:8080/qcbin/",
            dataType = DataType.TEXT)
    private String hpalmUrl;

    @ConfigProperty(name = "hpalm_serviceuser", displayName = "User Name",
            description = "HP ALM service username.",
            defaultValue = "",
            dataType = DataType.TEXT)
    private String serviceUser;

    @ConfigProperty(name = "hpalm_servicepassword", displayName = "Password",
            description = "HP ALM service password.",
            defaultValue = "",
            dataType = DataType.PASSWORD)
    private String servicePassword;

    @ConfigProperty(name = "useXsrf", displayName = "Use XSRF-TOKEN cookie",
            description = "Use XSRF-TOKEN cookie, required for HP ALM 12.0 onwards",
            defaultValue = "true",
            dataType = DataType.TEXT)
    private String useXsrf;

    @ConfigProperty(name = "hpalm_domain", displayName = "HP ALM Domain",
            description = "The HP ALM domain to query.",
            defaultValue = "DEFAULT",
            dataType = DataType.TEXT)
    private String hpalmDomain;

    @ConfigProperty(name = "request_status_filters", displayName = "Status Filters",
            description = "status filters separated by commas",
            defaultValue = "Closed,Fixed,New,Open,Rejected,Reopen",
            dataType = DataType.TEXT)
    private String statusFilters;

    @ConfigProperty(name = "request_result_limit", displayName = "Result Limit",
            description = "Result limit for find requests action",
            defaultValue = "300",
            dataType = DataType.TEXT)
    private String requestResultLimit;

    @Override
    public String getProviderName() {
        return this.providerName;
    }

    @Autowired(required = false)
    @Override
    public void setProviderName(String providerName) {
        if (StringUtils.isNotEmpty(providerName)) {
            providerName = providerName.trim();
        }

        this.providerName = providerName;
    }

    @Override
    public String getProviderDescription() {
        return this.providerDescription;
    }

    @Autowired(required = false)
    @Override
    public void setProviderDescription(String providerDescription) {
        if (StringUtils.isNotEmpty(providerDescription)) {
            providerDescription = providerDescription.trim();
        }

        this.providerDescription = providerDescription;
    }

    public String getHpalmUrl() {
        return hpalmUrl;
    }

    @Autowired(required = false)
    public void setHpalmUrl(String hpalmUrl) {
        if (StringUtils.isNotEmpty(hpalmUrl)) {
            this.hpalmUrl = hpalmUrl.replaceAll("/qcbin", "");
            this.hpalmUrl = hpalmUrl.replaceAll("^\\s+", "");
        } else {
            this.hpalmUrl = "http://localhost:8080";
        }
    }

    public String getServiceUser() {
        return serviceUser;
    }

    @Autowired(required = false)
    public void setServiceUser(String serviceUser) {
        if (!StringUtils.isEmpty(serviceUser)) {
            this.serviceUser = serviceUser.replaceAll("^\\s+", "");
        }
    }

    public String getServicePassword() {
        return servicePassword;
    }

    @Autowired(required = false)
    public void setServicePassword(String servicePassword) {
        if (!StringUtils.isEmpty(servicePassword)) {
            this.servicePassword = servicePassword.replaceAll("^\\s+", "");
        }
    }

    public String useXsrf() {
        return useXsrf;
    }

    @Autowired(required = false)
    public void setUseXsrf(String useXsrf) {
        if (StringUtils.isNotEmpty(useXsrf))
            this.useXsrf = useXsrf.replaceAll("^\\s+", "");
    }

    public String getHpalmDomain() {
        return hpalmDomain;
    }

    @Autowired(required = false)
    public void setHpalmDomain(String hpalmDomain) {
        if (!StringUtils.isEmpty(hpalmDomain)) {
            hpalmDomain = hpalmDomain.trim();
        }

        this.hpalmDomain = hpalmDomain;
    }

    public String getStatusFilters() {
        return statusFilters;
    }

    @Autowired(required = false)
    public void setStatusFilters(String statusFilters) {
        if (!StringUtils.isEmpty(statusFilters)) {
            statusFilters = statusFilters.trim();
        }

        this.statusFilters = statusFilters;
    }

    public String getRequestResultLimit() {
        return requestResultLimit;
    }

    @Autowired(required = false)
    public void setRequestResultLimit(String requestResultLimit) {
        this.requestResultLimit = requestResultLimit;
    }


    //================================================================================
    // IRequestProvider Overrides
    //================================================================================

    @Override
    @Service(name = FIND_REQUESTS, displayName = "Find Defects", description = "Find HP ALM Defects.")
    @Params(params = {
            @Param(fieldName = PROJECT, displayName = "Project", description = "HP ALM project name", required = true, dataType = DataType.SELECT),
            @Param(fieldName = STATUS_FILTERS, displayName = "Status Filters", description = "JIRA Status filters.", dataType = DataType.MULTI_SELECT, required = false),
            @Param(fieldName = TITLE_FILTER, displayName = "Name Filter", description = "Defect Name filter."),})
    public ProviderInfoResult findRequests(List<Field> properties, Long startIndex, Long resultCount) throws ProviderException {
        Field field = Field.getFieldByName(properties, PROJECT);
        if (field == null) {
            throw new ProviderException("Missing required property: " + PROJECT);
        }

        String projectId = field.getValue();
        logger.debug("Filtering on project: " + projectId);

        String titleFilter = null;
        field = Field.getFieldByName(properties, TITLE_FILTER);
        if (field != null) {
            titleFilter = field.getValue();
        }

        List<String> requestStatusFilters = null;
        List<Field> fields = Field.getFieldsByName(properties, STATUS_FILTERS);
        if (fields != null && fields.size() > 0) {
            requestStatusFilters = new ArrayList<>();
            for (Field fieldFilter : fields) {
                requestStatusFilters.add(fieldFilter.getValue());
            }
        }

        List<ProviderInfo> list = new ArrayList<>();

        setALMClientConnectionDetails();
        try {
            List<Defect> requests = getALMClient().getDefects(projectId, requestStatusFilters, titleFilter, getResultLimit());
            if (requests != null) {
                ProviderInfo pReqInfo;
                for (Defect request : requests) {
                    pReqInfo = new ProviderInfo(request.getId(), request.getName(), request.getType(), request.getName());
                    // combine project and id for unique id
                    pReqInfo.setId(projectId + ":" + request.getId());

                    pReqInfo.setDescription(request.getDescription());
                    //http://localhost:8080/qcbin/ui/?p=DEFAULT/Demo#/defects/1/details
                    pReqInfo.setUrl(this.getHpalmUrl() + "/qcbin/ui/?p=" + getHpalmDomain() + "/"
                            + projectId + "#/defects/" + request.getId() + "/details");
                    fields = new ArrayList<>();

                    addField(fields, "project", "Project", request.getProject());
                    addField(fields, "owner", "Owner", request.getOwner());
                    addField(fields, "status", "Status", request.getStatus());
                    addField(fields, "severity", "Severity", request.getSeverity());
                    addField(fields, "priority", "Priority", request.getPriority());
                    addField(fields, "creator", "Creator", request.getCreator());
                    addField(fields, "dateCreated", "Date Created", request.getDateCreated());
                    addField(fields, "lastUpdated", "Last Updated", request.getLastUpdated());

                    pReqInfo.setProperties(fields);
                    list.add(pReqInfo);
                }
            }
        } catch (ALMClientException e) {
            logger.debug(e.getLocalizedMessage());
            throw new ProviderException(e.getLocalizedMessage());
        }

        return new ProviderInfoResult(0, list.size(), list.toArray(new ProviderInfo[list.size()]));
    }



    @Override
    @Service(name = GET_REQUEST, displayName = "Get Request", description = "Get HP ALM request information.")
    @Params(params = {
            @Param(fieldName = REQUEST_ID, displayName = "Defect Id", description = "HP ALM Defect identifier", required = true, deployUnit = false, dataType = DataType.SELECT)
    })
    public ProviderInfo getRequest(Field property) throws ProviderException {
        if (StringUtils.isEmpty(property.getValue())) {
            throw new ProviderException("Missing required field: " + REQUEST_ID);
        }

        setALMClientConnectionDetails();
        try {
            String project[] = property.getValue().split(":");
            logger.debug("Request has project=" + project[0] + " and id=" + project[1]);
            Defect request = getALMClient().getDefect(project[0], project[1]);
            if (request == null) {
                throw new ProviderException("Unable to find request: " + property.getValue());
            }

            ProviderInfo pReqInfo = new ProviderInfo(request.getId(), request.getName(), request.getType(), request.getName());
            if (StringUtils.isEmpty(request.getId())) {
                pReqInfo.setId(request.getName());
            }

            pReqInfo.setDescription(request.getDescription());
            //http://localhost:8080/qcbin/ui/?p=DEFAULT/Demo#/defects/1/details
            pReqInfo.setUrl(this.getHpalmUrl() + "/qcbin/ui/?p=" + getHpalmDomain() + "/"
                    + request.getProject() + "#/defects/" + request.getId() + "/details");
            List<Field> fields = new ArrayList<>();

            addField(fields, "project", "Project", request.getProject());
            addField(fields, "owner", "Owner", request.getOwner());
            addField(fields, "status", "Status", request.getStatus());
            addField(fields, "severity", "Severity", request.getSeverity());
            addField(fields, "priority", "Priority", request.getPriority());
            addField(fields, "creator", "Creator", request.getCreator());
            addField(fields, "dateCreated", "Date Created", request.getDateCreated());
            addField(fields, "lastUpdated", "Last Updated", request.getLastUpdated());

            pReqInfo.setProperties(fields);
            return pReqInfo;
        } catch (ALMClientException e) {
            logger.debug(e.getLocalizedMessage());
            throw new ProviderException(e.getLocalizedMessage());
        }
    }

    @Override
    public FieldInfo getFieldValues(String fieldName, List<Field> properties)
            throws ProviderException {

        if (fieldName.equalsIgnoreCase(PROJECT)) {
            return getProjectFieldValues(fieldName);
        } else if (fieldName.equalsIgnoreCase(STATUS_FILTERS)) {
            return getStatusFiltersFieldValues(fieldName);
        }

        throw new ProviderException("Unsupported get values for field name: " + fieldName);
    }

    //================================================================================
    // Getter Methods
    // -------------------------------------------------------------------------------
    // These methods are used to get the field values. The @Getter annotation is used
    // by the system to generate a user interface and pass the correct parameters to
    // to the provider
    //================================================================================

    @Getter(name = PROJECT, displayName = "Project", description = "Get HP ALM project field values.")
    public FieldInfo getProjectFieldValues(String fieldName) throws ProviderException {
        FieldInfo fieldInfo = new FieldInfo(fieldName);
        setALMClientConnectionDetails();

        try {
            List<Project> hpalmProjects = almClient.getProjects();
            if (hpalmProjects == null || hpalmProjects.size() < 1) {
                return null;
            }

            List<FieldValueInfo> values = new ArrayList<>();
            FieldValueInfo value;
            for (ALMObject hpProj : hpalmProjects) {

                value = new FieldValueInfo(hpProj.getId(), hpProj.getName());
                if (hpProj.getId() == null || StringUtils.isEmpty(hpProj.getId())) {
                    value.setId(hpProj.getName());
                }

                value.setDescription(hpProj.getName());
                values.add(value);
            }

            fieldInfo.setValues(values);
            return fieldInfo;
        } catch (ALMClientException e) {
            throw new ProviderException(e.getLocalizedMessage());
        }
    }


    @Getter(name = STATUS_FILTERS, displayName = "Defect Status Filters", description = "Get HP ALM status filters field values.")
    public FieldInfo getStatusFiltersFieldValues(String fieldName) throws ProviderException {
        if (StringUtils.isEmpty(statusFilters)) {
            return null;
        }

        StringTokenizer st = new StringTokenizer(statusFilters, ",;");
        FieldInfo fieldInfo = new FieldInfo(fieldName);
        List<FieldValueInfo> values = new ArrayList<>();
        FieldValueInfo value;
        String status;
        while (st.hasMoreElements()) {
            status = (String) st.nextElement();
            status = status.trim();
            value = new FieldValueInfo(status, status);
            values.add(value);
        }

        fieldInfo.setValues(values);
        return fieldInfo;
    }

    //================================================================================
    // Private Methods
    //================================================================================

	private ALMClient getALMClient() {
        if (almClient == null) {
            almClient = new ALMClient();
        }
        
        return almClient;
    }
	
    private void addField(List<Field> fieldCollection, String fieldName, String fieldDisplayName, String fieldValue) {
        if (StringUtils.isNotEmpty(fieldValue)) {
            Field field = new Field(fieldName, fieldDisplayName);
            field.setValue(fieldValue);
            fieldCollection.add(field);
        }
    }

    private void setALMClientConnectionDetails() {
        getALMClient().createConnection(getSession(), getHpalmUrl(), getServiceUser(), getServicePassword(), useXsrf(), getHpalmDomain());
    }

    private int getResultLimit() {
        if (resultLimit == null) {
            resultLimit = 300;

            if (StringUtils.isNotBlank(requestResultLimit)) {
                try {
                    resultLimit = Integer.parseInt(requestResultLimit);
                } catch (Throwable e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }

        return resultLimit;
    }

}
