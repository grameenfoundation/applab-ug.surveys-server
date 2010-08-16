package applab.surveys.server;

/*

 Copyright (C) 2010 Grameen Foundation
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain a copy of
 the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 License for the specific language governing permissions and limitations under
 the License.
 */

import javax.servlet.http.*;

import applab.server.*;

import java.io.PrintWriter;
import java.util.ArrayList;

public class GetFormList extends ApplabServlet {
    private static final long serialVersionUID = 1L;

    protected void doApplabGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        writeFormsList(response.getWriter());
    }
    
    public static void writeFormsList(PrintWriter writer) throws Exception {
        writer.write("<forms>");
        SalesforceProxy salesforceProxy = SalesforceProxy.login();

        // get the list of published survey IDs 
        // TODO: also get their associated names from salesforce instead of all these extra lookups
        ArrayList<String> publishedSurveyIds = salesforceProxy.getPublishedSurveys();
        if (publishedSurveyIds.size() > 0) {
            for (String surveyId : publishedSurveyIds) {
                String surveyName = SurveyDatabaseHelpers.getSurveyName(surveyId);
                if (surveyName != null) {
                    writer.write("<form url=\"" + ApplabConfiguration.getHostUrl() + "getForm?surveyid=" + surveyId + "\" >"
                            + surveyName + "</form>");
                }
            }
        }
        else {
            writer.write("<form>Don't Click</form>");
        }
        writer.write("</forms>");
        salesforceProxy.logout();        
    }
}
