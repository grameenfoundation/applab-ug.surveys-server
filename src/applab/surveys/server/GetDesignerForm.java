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

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import applab.server.ApplabServlet;
import applab.server.ServletRequestContext;

/**
 * Fetches the XML for a survey form from our zebrasurveys table
 * and returns it to Purcforms
 *
 */
public class GetDesignerForm extends ApplabServlet {
    private static final long serialVersionUID = 1L;

    public void doApplabGet(HttpServletRequest request, HttpServletResponse response, ServletRequestContext context) throws IOException, SQLException, ClassNotFoundException {
        String surveyId = context.getRequestParameter("surveyId");
        String xformData = SurveyDatabaseHelpers.getXformData(surveyId);

        if(xformData != null) {
            log(xformData);
            //Charset set to UTF-8 for multi-language support. 
            response.setContentType("text/html; charset=UTF-8");
            response.getWriter().write(xformData);           
        }
        // We send a blank response if the data is null, and the purcforms client will use the empty response to indicate a new form
    }
}
