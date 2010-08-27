/*
 * Copyright (C) 2010 Grameen Foundation
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


package applab.surveys.server;

import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.xml.rpc.ServiceException;

import applab.surveys.*;

import com.sforce.soap.enterprise.*;
import com.sforce.soap.enterprise.fault.*;
import com.sforce.soap.enterprise.sobject.*;
import applab.server.SalesforceProxy;


public class SurveysSalesforceProxy extends SalesforceProxy {
    public SurveysSalesforceProxy() throws ServiceException, InvalidIdFault, UnexpectedErrorFault, LoginFault, RemoteException {
        super();
    }

    public boolean surveyIdExists(String survey_id) throws Exception {
        QueryResult query = getBinding().query("Select Name from Survey__c where Name='" + survey_id + "'");
        return (query.getSize() == 1);
    }

    public String getSurveyName(String survey_id) throws InvalidSObjectFault, MalformedQueryFault, InvalidFieldFault, InvalidIdFault, UnexpectedErrorFault, InvalidQueryLocatorFault, RemoteException {
        QueryResult query = getBinding().query("Select Survey_Name__c from Survey__c where Name='" + survey_id + "'");
        if (query.getSize() == 1) {
            Survey__c survey = (Survey__c)query.getRecords(0);
            return survey.getSurvey_Name__c();
        }
        return "Not Available";
    }

    public ArrayList<Survey> getPublishedSurveys() throws Exception {
        QueryResult query = getBinding().query("select Name, Survey_Name__c from Survey__c where Survey_Status__c = 'Published'");
        ArrayList<Survey> surveys = new ArrayList<Survey>();
        for (int i = 0; i < query.getSize(); i++) {
            Survey__c survey = (Survey__c)query.getRecords(i);
            surveys.add(new Survey(survey.getName(), survey.getSurvey_Name__c()));
        }
        return surveys;
    }
}