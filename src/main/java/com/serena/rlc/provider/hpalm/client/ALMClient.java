/*
 *
 * Copyright (c) 2016 SERENA Software, Inc. All Rights Reserved.
 *
 * This software is proprietary information of SERENA Software, Inc.
 * Use is subject to license terms.
 *
 */

package com.serena.rlc.provider.hpalm.client;

import com.serena.rlc.provider.domain.SessionData;
import com.serena.rlc.provider.hpalm.domain.Defect;
import com.serena.rlc.provider.hpalm.domain.Project;
import com.serena.rlc.provider.hpalm.exception.ALMClientException;
import org.apache.http.*;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author klee
 */

@Component
public class ALMClient {
    private static final Logger logger = LoggerFactory.getLogger(ALMClient.class);

    public static String DEFAULT_HTTP_CONTENT_TYPE = "application/json";

    private String almUrl;
    private String almUsername;
    private String almPassword;
    private String almDomain;
    private String useXsrf;
    private SessionData session;

    private DefaultHttpClient httpClient;
    private HttpHost httpHost = null;
    private String ssoCookie = null;
    private String sessionCookie = null;
    private String xsrfToken = null;

    public ALMClient() {
    }

    public ALMClient(SessionData session, String url, String username, String password, String useXsrf, String domain) {
        this.session = session;
        this.almUrl = url;
        this.almUsername = username;
        this.almPassword = password;
        this.almDomain = domain;
        this.useXsrf = useXsrf;
        this.createConnection(session, url, username, password, useXsrf, domain);
    }

    public SessionData getSession() {
        return session;
    }

    public void setSession(SessionData session) {
        this.session = session;
    }

    public DefaultHttpClient getHttpClient() {
        return httpClient;
    }

    public void setDefaultHttpClient(DefaultHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String getALMUrl() {
        return almUrl;
    }

    public void setALMUrl(String url) {
        this.almUrl = url;
    }

    public String getALMUsername() {
        return almUsername;
    }

    public void setALMUsername(String username) {
        this.almUsername = username;
    }

    public String getALMPassword() {
        return almPassword;
    }

    public void setALMPassword(String password) {
        this.almPassword = password;
    }

    public String getUseXsrf() { return useXsrf; }

    public void setUseXsrf(String useXsrf) { this.useXsrf = useXsrf; }

    public String getALMDomain() {
        return almDomain;
    }

    public void setALMDomain(String domain) {
        this.almDomain = domain;
    }

    public void createConnection(SessionData session, String url, String username, String password) {
        createConnection(session, url, username, password, "true", null);
    }

    public void createConnection(SessionData session, String url, String username, String password, String useXsrf, String domain) {
        this.session = session;
        this.almUrl = url;
        this.almUsername = username;
        this.almPassword = password;
        this.almDomain = domain;
        this.useXsrf = useXsrf;

        this.httpClient = new DefaultHttpClient();
        String[] urlParts = this.almUrl.split(":");
        if (urlParts.length > 2) {
            this.httpHost = new HttpHost(urlParts[1].replaceAll("/",""), Integer.parseInt(urlParts[2]), urlParts[0]);
        } else {
            this.httpHost = new HttpHost(urlParts[1].replaceAll("/", ""), 80, urlParts[0]);
        }
        this.httpClient.getParams().setParameter(ClientPNames.DEFAULT_HOST, httpHost);

        try {
            login(session);
        } catch (ALMClientException ex) {
            logger.error(ex.getLocalizedMessage());
        }

    }

    /**
     * Get a list of defects
     *
     * @param projectId  the id of the project, e.g. Demo
     * @param statusFilters  list of statuses to filter for, e.g. New,Open
     * @param titleFilter  the title (name) of the defect to search for
     * @param resultLimit  the number of defects to return
     * @return  a list of defects
     * @throws ALMClientException
     */
    public List<Defect> getDefects(String projectId, List<String> statusFilters, String titleFilter, Integer resultLimit) throws ALMClientException {
        logger.debug("Using HP ALM URL: " + this.almUrl);
        logger.debug("Using HP ALM Credential: " + this.almUsername);
        logger.debug("Using HP ALM Domain: " + getALMDomain());
        logger.debug("Using HP ALM Status Filter: " + (statusFilters != null && !statusFilters.isEmpty() ? statusFilters.toString() : "none defined"));
        logger.debug("Using HP ALM Title Filter: " + (titleFilter != null && !titleFilter.isEmpty() ? titleFilter : "none defined"));
        logger.debug("Limiting results to: " + resultLimit.toString());

        String defAPI = "/qcbin/rest/domains/" + getALMDomain() + "/projects/" + projectId + "/defects";
        String defQuery= "{";
        if (titleFilter != null && !titleFilter.isEmpty()) {
            defQuery += "name[*" + titleFilter + "*]";
        }
        if (statusFilters != null && !statusFilters.isEmpty()) {
            if (titleFilter != null && !titleFilter.isEmpty()) { defQuery += "; "; }
            defQuery +="status[";
            for (String status : statusFilters) {
                defQuery += status + " or ";
            }
            // remove last or operator if it exists
            if (defQuery.endsWith(" or "))
                defQuery = defQuery.substring(0, defQuery.length() - 4);
            defQuery += "]";
        }
        defQuery +="}";

        logger.debug("Using HP ALM Query:" + defQuery);
        String encodedQuery = "";
        try {
            encodedQuery = URLEncoder.encode(defQuery, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
            throw new ALMClientException("Unable to encode HP ALM query string", e);
        }
        if (resultLimit > 0) {
            defQuery += "&page-size=" + resultLimit.toString();
        }

        logger.debug("Retrieving HP ALM Defects");
        String defResponse = processGet(session, defAPI + "?query=" + encodedQuery);
        logger.debug(defResponse);

        List<Defect> defects = Defect.parse(defResponse);
        return defects;

    }

    /**
     * Get a specfic defect
     *
     * @param projectId  the id of the project, e.g. Demo
     * @param defectId  the id of the defect, e.g. 1
     * @return the defect if found
     * @throws ALMClientException
     */
    public Defect getDefect(String projectId, String defectId) throws ALMClientException {
        logger.debug("Using HP ALM URL: " + this.almUrl);
        logger.debug("Using HP ALM Credential: " + this.almUsername);
        logger.debug("Using HP ALM Domain: " + getALMDomain());
        logger.debug("Using HP ALM Defect Id: " + defectId);

        logger.debug("Retrieving HP ALM Defect");
        String defResponse = processGet(session, "/qcbin/rest/domains/" + getALMDomain() + "/projects/" + projectId + "/defects/" + defectId);
        logger.debug(defResponse);

        Defect defect = Defect.parseSingle(defResponse);
        return defect;
    }

    /**
     * Get a list of projects in the domain
     * @return a list of projects
     * @throws ALMClientException
     */
    public List<Project> getProjects() throws ALMClientException {
        logger.debug("Using HP ALM URL: " + this.almUrl);
        logger.debug("Using HP ALM Credential: " + this.almUsername);
        logger.debug("Using HP ALM Domain: " + this.almDomain);

        logger.debug("Retrieving HP ALM Projects");
        String projResponse = processGet(session, "/qcbin/rest/domains/" + getALMDomain() + "/projects");
        logger.debug(projResponse);

        List<Project> projects = Project.parse(projResponse);
        return projects;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Check whether the user is currently authenticated
     *
     * @param session
     * @return true if the use is authentication else false
     * @throws com.serena.rlc.provider.hpalm.exception.ALMClientException
     */
    protected boolean isAuthenticated(SessionData session) throws ALMClientException {
        String uri = getALMUrl() + "/rest/is-authenticated";

        logger.debug("Start executing HP ALM request to url=\"{}\"", uri);

        HttpGet authRequest = new HttpGet(uri);
        authRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/xml");
        authRequest.addHeader(HttpHeaders.ACCEPT, "application/xml");

        try {
            HttpResponse response = getHttpClient().execute(authRequest);
            HttpEntity responseEntity = response.getEntity();
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                EntityUtils.consume(responseEntity);
                return false;
            } else {
                EntityUtils.consume(responseEntity);
                return true;
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ALMClientException("Server not available", e);
        }
    }

    /**
     * Execute a login request
     *
     * @param session
     * @throws com.serena.rlc.provider.hpalm.exception.ALMClientException
     */
    protected void login(SessionData session) throws ALMClientException {
        String uri = getALMUrl() + "/qcbin/authentication-point/authenticate";

        logger.debug("Start executing HP ALM Login request to url=\"{}\"", uri);

        HttpPost authRequest = new HttpPost(uri);
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(getALMUsername(), getALMPassword());
        authRequest.addHeader(BasicScheme.authenticate(creds, "US-ASCII", false));
        authRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/xml");
        authRequest.addHeader(HttpHeaders.ACCEPT, "application/xml");
        authRequest.addHeader(HttpHeaders.CONNECTION, "keep-alive");

        try {
            HttpResponse response = getHttpClient().execute(authRequest);

            Header[] headers = response.getHeaders("Set-Cookie");

            for (int i = 0; i < headers.length; i++) {
                if (headers[i].toString().contains("LWSSO_COOKIE_KEY")) {
                    ssoCookie = headers[i].toString();
                }
            }

            HttpEntity responseEntity = response.getEntity();
            EntityUtils.consume(responseEntity);

            if (getUseXsrf().equals("true")) {

                HttpPost sessionpost = new HttpPost("/qcbin/rest/site-session");
                sessionpost.addHeader("Cookie", ssoCookie);
                response = getHttpClient().execute(sessionpost);
                headers = response.getHeaders("Set-Cookie");

                for (int i = 0; i < headers.length; i++) {
                    if (headers[i].toString().contains("QCSession")) {
                        this.sessionCookie = headers[i].toString();
                    }
                    if (headers[i].toString().contains("XSRF-TOKEN")) {
                        this.xsrfToken = headers[i].toString();
                    }
                }

                responseEntity = response.getEntity();
                EntityUtils.consume(responseEntity);
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ALMClientException("Server not available", e);
        }

    }

    /**
     * Execute a get request
     *
     * @param path the url path to execute get for
     * @return Response body
     * @throws com.serena.rlc.provider.hpalm.exception.ALMClientException
     */
    protected String processGet(SessionData session, String path) throws ALMClientException {
        String uri = getALMUrl() + path;

        logger.debug("Start executing HP ALM GET request to url=\"{}\"", path);

        HttpGet getRequest = new HttpGet(uri);

        getRequest.addHeader("Cookie", this.ssoCookie);
        if (getUseXsrf().equals("true")) { getRequest.addHeader("X-XSRF-TOKEN", this.xsrfToken); }
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        getRequest.addHeader(HttpHeaders.ACCEPT, "application/json,application/xml");
        String result = "";

        try {
            HttpResponse response = getHttpClient().execute(getRequest);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw createHttpError(response);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            StringBuilder sb = new StringBuilder(1024);
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            result = sb.toString();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ALMClientException("Server not available", e);
        }

        logger.debug("End executing HP ALM GET request to url=\"{}\" and receive this result={}", uri, result);

        return result;
    }

    /**
     * Encode a URI path
     *
     * @param path
     * @return
     */
    private String encodePath(String path) {
        String result;
        URI uri;
        try {
            uri = new URI(null, null, path, null);
            result = uri.toASCIIString();
        } catch (Exception e) {
            result = path;
        }
        return result;
    }

    /**
     * Returns an ALM Client specific Client Exception
     * @param response  the exception to throw
     * @return
     */
    private ALMClientException createHttpError(HttpResponse response) {
        String message;
        try {
            StatusLine statusLine = response.getStatusLine();
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            StringBuffer responsePayload = new StringBuffer();
            // Read response until the end
            while ((line = rd.readLine()) != null) {
                responsePayload.append(line);
            }

            message = String.format(" request not successful: %d %s. Reason: %s", statusLine.getStatusCode(), statusLine.getReasonPhrase(), responsePayload);

            logger.debug(message);

            if (new Integer(HttpStatus.SC_UNAUTHORIZED).equals(statusLine.getStatusCode())) {
                return new ALMClientException("Invalid credentials provided.");
            } else if (new Integer(HttpStatus.SC_NOT_FOUND).equals(statusLine.getStatusCode())) {
                return new ALMClientException("HP ALM: Request URL not found.");
            } else if (new Integer(HttpStatus.SC_BAD_REQUEST).equals(statusLine.getStatusCode())) {
                return new ALMClientException("HP ALM: Bad request. " + responsePayload);
            }
        } catch (IOException e) {
            return new ALMClientException("HP ALM: Can't read response");
        }

        return new ALMClientException(message);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Testing API
    static public void main(String[] args) {
        ALMClient alm = new ALMClient(null, "http://40.76.28.141:8080", "admin1", "adminpw", "true", "DEFAULT");

        String firstProj = "";
        String firstDef = "";

        System.out.println("Checking if user is authenticated");
        try {
            if (alm.isAuthenticated(null)) {
                System.out.println("User is authenticated");
            } else {
                System.out.println("User is not authenticated");
            }
        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
        }

        System.out.println("Retrieving HP ALM Projects...");
        List<Project> projects = null;
        try {
            projects = alm.getProjects();
            for (Project p : projects) {
                if (firstProj.length() == 0) firstProj = p.getName();
                System.out.println("Found Project " + p.getName());
            }
        } catch (ALMClientException e) {
            System.out.print(e.toString());
        }

        // default statuses: Closed,Fixed,New,Open,Rejected,Reopen
        List<String> statuses = new ArrayList<String>();
        statuses.add("New");
        statuses.add("Open");
        for (Project project : projects) {
            System.out.println("Retrieving defects for project " + project.getName());
            List<Defect> defects = null;
            try {
                defects = alm.getDefects(project.getName(), statuses , "first", 2);
                for (Defect d: defects) {
                    if (firstDef.length() == 0) firstDef = d.getId();
                    System.out.println("Found Defect " + d.getName());
                }
            } catch (ALMClientException e){
                System.out.print(e.toString());
            }
        }

        System.out.println("Retrieving Defect " + firstDef + " for Project " + firstProj + "...");
        try {
            Defect d = alm.getDefect(firstProj, firstDef);
            System.out.println("Found Defect " + d.getId());
            System.out.println("Name: " + d.getName());
            System.out.println("Status: " + d.getStatus());
            System.out.println("Severity: " + d.getSeverity());
            System.out.println("Description: " + d.getDescription());
        } catch (ALMClientException e) {
            System.out.print(e.toString());
        }
    }
}
