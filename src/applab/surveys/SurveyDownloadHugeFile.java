package applab.surveys;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;

import org.xml.sax.SAXException;

import applab.server.ApplabServlet;
import applab.server.CsvHelpers;
import applab.server.DatabaseHelpers;
import applab.server.ServletRequestContext;

/**
 * Servlet implementation class SurveyDownloadHugeFile
 */
public class SurveyDownloadHugeFile extends ApplabServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public SurveyDownloadHugeFile() {
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doApplabGet(HttpServletRequest request, HttpServletResponse response)  throws ClassNotFoundException, SQLException,
    IOException, ServiceException, ParseException, ServletException, SAXException, ParserConfigurationException {
		doApplabPost(request, response, null); 
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
    public void doApplabPost(HttpServletRequest request, HttpServletResponse response, ServletRequestContext context) throws ClassNotFoundException, SQLException,
    IOException, ServiceException, ParseException, ServletException, SAXException, ParserConfigurationException {

    	DownloadType downloadType = DownloadType.valueOf(request.getParameter("downloadType"));
		DownloadTarget downloadTarget = DownloadTarget.valueOf(request.getParameter("downloadTarget"));
		
		switch (downloadType) {
		case Csv:
			log("request for CSV format");
		    response.setContentType("text/csv");
		    break;
		default:
			log("request for HTML format");
		    response.setContentType("text/html");
		}
		
		String fileDownloadName = "";
		
		switch (downloadTarget) {
		    case SubmissionCsv:
		    	log("now processing CSV");
		        // Extract the request data
		        String salesforceSurveyId = request.getParameter("surveySalesforceId");
		
		        // Extract the optional parameters
		        java.sql.Date startDate = null;
		        java.sql.Date endDate = null;
		        String startDateDescription = "NoStartDate";
		        String endDateDescription = "NoEndDate";
		        String statusDescription = "NoStatus";
		        boolean showDraft = false;
		        if ("true".equals(request.getParameter("showDraft"))) {
		            showDraft = true;
		        }
		
		        try {
		            startDate =  DatabaseHelpers.getSqlDateFromString(request.getParameter("startDate"), 0);
		            startDateDescription = startDate.toString();
		        }
		        catch (Exception e) {
		
		            // Do nothing and leave the dates blank
		        }
		        try {
		            endDate   = DatabaseHelpers.getSqlDateFromString(request.getParameter("endDate"), 0);
		            endDateDescription = endDate.toString();
		        }
		        catch (Exception e) {
		
		            // Do nothing and leave the dates blank
		        }
		   
		        java.util.Date now = new java.util.Date();
		        
		        // If we only have one of the dates provided then set the other one to now
		        if (startDate != null && endDate == null) {
		            endDate = new java.sql.Date(now.getTime());    
		            endDateDescription = endDate.toString();
		        }
		    
		        if (startDate == null && endDate != null) {
		            startDate = new java.sql.Date(now.getTime());    
		            startDateDescription = startDate.toString();
		        }
		
		        SubmissionStatus status = request.getParameter("status") == null && request.getParameter("status") == "null" ? null: SubmissionStatus.parseHtmlParameter(request.getParameter("status"));
		        if (status != null) {
		            statusDescription =  status.toString();
		        }
		        log("obtaining submission statistics");
		        SubmissionStatistics statistics = SubmissionStatistics.getStatistics(salesforceSurveyId, startDate, endDate);
		        	            
		        fileDownloadName = "SubmissionCSV-" + startDateDescription + "-" + endDateDescription + "-" + statusDescription + ".csv";
		        response.setHeader("Content-Disposition", "attachment; filename=" + fileDownloadName);
		        
		        if (statistics == null) {
		        	log("could not acquire statistics");
		            // TODO - ErrorPage
		        }
		        else {
		        	log("statistic acquired");
		        	statistics.getSurvey().loadSurvey(true);
		        	log("survey loaded");
		            // We have some statistics so lets load the submission
		            //statistics.getSurvey().loadSubmissions(status, startDate, endDate, false, salesforceSurveyId, showDraft);
		            generateCsv(response, statistics, status, startDate, endDate, false, salesforceSurveyId, showDraft);
		            //fileDownloadName = "SubmissionCSV-" + startDateDescription + "-" + endDateDescription + "-" + statusDescription + ".csv";
		        }
		        break;
		    default:
		        break;
		        // TODO - Error
		}
    }

    private enum DownloadType {
        Csv,
        Pdf;
    }
    
    private enum DownloadTarget {
        SubmissionCsv;
    }
    
    
    /**
     * Generates a CSV file for the survey loaded along with its submissions
     * 
     * @return - a string of the csv.
     */
    private void generateCsv(HttpServletResponse response, SubmissionStatistics statistics, SubmissionStatus submissionStatus,
    		java.sql.Date startDate, java.sql.Date endDate, boolean basic, String salesforceId, boolean showDraft) throws IOException, SAXException,
            ParserConfigurationException, ParseException, SQLException, ClassNotFoundException {
    	
    	log("SubmissionStatus.NotReviewed " + statistics.getNumberOfSubmissions(SubmissionStatus.NotReviewed) );
    	log("SubmissionStatus.Pending " + statistics.getNumberOfSubmissions(SubmissionStatus.Pending) );
    	log("SubmissionStatus.Approved " + statistics.getNumberOfSubmissions(SubmissionStatus.Approved) );
    	log("SubmissionStatus.Rejected " + statistics.getNumberOfSubmissions(SubmissionStatus.Rejected) );
    	log("SubmissionStatus.Duplicate " + statistics.getNumberOfSubmissions(SubmissionStatus.Duplicate) );

    	log("writing CSV...");
    	PrintWriter writer = response.getWriter();
        //StringBuilder writer = new StringBuilder();

        // Extract and save the standard column headers
        writer.append("Survey ID,");
        writer.append("Submission Id,");
        writer.append("Survey Start Time,");
        writer.append("Survey End Time,");
        writer.append("Server Entry Date,");
        writer.append("ID,");
        writer.append("Name,");
        writer.append("Location,");
        writer.append("Submission Location,");
        writer.append("Interviewer - Interviewee Distance,");
        writer.append("Customer Care Review,");
        writer.append("Data Team Review,");

        // Extract and save the question bindings as these are used by the DC
        // team
        for (String questionBinding : statistics.getSurvey().getBackEndSurveyXml().getQuestionOrder()) {
            Question question = statistics.getSurvey().getBackEndSurveyXml().getQuestions().get(
                    questionBinding);

            for (int i = 1; i <= question.getTotalInstances(); i++) {
                String questionDisplayName = questionBinding;
                if (question.getTotalInstances() > 1) {
                    questionDisplayName = questionDisplayName + "_" + i;
                }
                questionDisplayName = "(" + questionDisplayName + ") " + question.getDisplayValue();

                if ("Select".equals(question.getType().toString())) {
                    for (int j = 1; j <= question.getNumberOfSelects(); j++) {
                        writer.append(CsvHelpers.escapeAndQuoteForCsv(questionDisplayName)
                                + "_" + question.getValueAtIndex(j) + ",");
                    }
                }
                else {
                    writer.append(CsvHelpers.escapeAndQuoteForCsv(questionDisplayName) + ",");
                }
            }
        }
        writer.append('\n');
        log("header written");
        writer.flush();
        //int flushConter = 0;
        log("fetching submissions, this might take a while... submissionStatus=" + submissionStatus.toString() + ", basic=" + basic + ", showDraft=" + showDraft);
        HashMap<Integer, Submission> submissions = statistics.getSurvey().loadSubmissions(submissionStatus, startDate, endDate, basic, showDraft);
        log("...submissions loaded = " + submissions.size());
        // Extract and save the answers
        for (int submissionId : statistics.getSurvey().getSubmissionOrder()) {
            //Submission submission = statistics.getSurvey().cachedSubmissions.get(submissionId);
            Submission submission = submissions.get(submissionId);

            // Add the standard answers
            writer.append(CsvHelpers.escapeAndQuoteForCsv(String
                    .valueOf(submission.getSurveyId())) + ',');
            writer.append(CsvHelpers.escapeAndQuoteForCsv(String
                    .valueOf(submissionId)) + ',');
            writer.append(CsvHelpers.escapeAndQuoteForCsv(String
                    .valueOf(submission.getSurveyStartTime())) + ',');
            writer.append(CsvHelpers.escapeAndQuoteForCsv(submission
                    .getHandsetSubmissionTime()) + ',');
            writer.append(CsvHelpers.escapeAndQuoteForCsv(submission
                    .getServerSubmissionTime()) + ',');
            writer.append(CsvHelpers.escapeAndQuoteForCsv(submission
                    .getInterviewerId()) + ',');
            writer.append(CsvHelpers.escapeAndQuoteForCsv(submission
                    .getInterviewerName()) + ',');
            writer.append(CsvHelpers.escapeAndQuoteForCsv(submission
                    .getLocation()) + ',');
            writer.append(CsvHelpers.escapeAndQuoteForCsv(submission
                    .getSubmissionLocation()) + ',');

            if (submission.getInterviewerDistance() >= 0) {
                writer.append(CsvHelpers.escapeAndQuoteForCsv(Double.toString(submission
                        .getInterviewerDistance())) + ',');
            }
            else {
                writer.append(CsvHelpers.escapeAndQuoteForCsv("Unknown") + ',');
            }
            writer.append(CsvHelpers.escapeAndQuoteForCsv(submission
                    .getCustomerCareStatus().toString()) + ',');
            writer.append(CsvHelpers.escapeAndQuoteForCsv(submission
                    .getStatus().toString()) + ',');
            for (String questionName : statistics.getSurvey().getBackEndSurveyXml().getQuestionOrder()) {
                Question question = statistics.getSurvey().getBackEndSurveyXml().getQuestions().get(
                        questionName);
                for (int i = 1; i <= question.getTotalInstances(); i++) {
                    String answerKey = questionName + "_" + i;
                    String answerText = "";
                    Answer answer = submission.getAnswers().get(answerKey);

                    if ("Select".equals(question.getType().toString())) {
                        boolean hasAnswerText = true;
                        if (answer == null
                                || answer.getFriendlyAnswerText(true, statistics.getSurvey()) == null
                                || answer.getFriendlyAnswerText(true, statistics.getSurvey())
                                        .length() == 0) {
                            hasAnswerText = false;
                        }
                        for (int j = 1; j <= question.getNumberOfSelects(); j++) {
                            if (!hasAnswerText) {
                                writer.append("[No Answer],");
                            }
                            else if (answer.getFriendlyAnswerText(true, statistics.getSurvey())
                                    .contains(" " + question.getValueAtIndex(j) + " ")) {
                                writer.append(CsvHelpers
                                        .escapeAndQuoteForCsv(question.getValueAtIndex(j))
                                        + ",");
                            }
                            else {
                                writer.append(",");
                            }
                        }
                    }
                    else {
                        if (answer == null
                                || answer.getFriendlyAnswerText(true, statistics.getSurvey()) == null
                                || answer.getFriendlyAnswerText(true, statistics.getSurvey())
                                        .length() == 0) {
                            answerText = "[No Answer]";
                        }
                        else {
                            answerText = answer.getFriendlyAnswerText(true, statistics.getSurvey());
                        }
                        writer.append(CsvHelpers
                                .escapeAndQuoteForCsv(answerText) + ',');
                    }
                }
            }
            writer.append('\n');
            writer.flush(); 
//            if(flushConter >= 100){
//            	log("flushing...");
//            	flushConter  = 0;
//            	writer.flush();            	
//            }
//            else{
//            	flushConter++;
//            }
        }

        //writer.append('\n');
        //writer.append('\n');
        writer.flush();
        writer.close();
        // return fileName;
        //return writer.toString();
    }
}
