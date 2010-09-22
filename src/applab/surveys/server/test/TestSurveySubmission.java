package applab.surveys.server.test;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

import applab.server.DatabaseHelpers;
import applab.surveys.SurveyItemResponse;
import applab.surveys.server.ProcessSubmission;


public class TestSurveySubmission extends TestCase {
    @Test public void testSingleAnswerParsing() { 
        // process submission and assert bits about the results
    } 

    @Test public void testRepeatAnswerParsing() { 
        // process submission and assert bits about the results
    } 

    @Test public void testAttachments() throws IOException {
        // TODO: implement a stub context/FileItem to test the full ProcessSubmission.saveAttachment code directly

        // this should create a reference of the form /survey_images/<generated_id>.jpeg
        String attachmentReference = ProcessSubmission.createAttachmentReference("image/jpeg");
        Assert.assertTrue(attachmentReference.endsWith(".jpeg"));
        Assert.assertTrue(attachmentReference.startsWith("/survey_images/"));

        File targetFile = new File(attachmentReference);

        // create the directory if necessary
        File parentDirectory = targetFile.getParentFile();
        if (parentDirectory != null) {
            parentDirectory.mkdirs();
        }
        Assert.assertTrue(targetFile.createNewFile());
        targetFile.deleteOnExit();
        parentDirectory.deleteOnExit();
    }

    @Test
    public void testDuplicateSubmissions() throws Exception {
        
        // Create the submission
        HashMap<String, SurveyItemResponse> answers = new HashMap<String, SurveyItemResponse>();
        HashMap<String, SurveyItemResponse> answers2 = new HashMap<String, SurveyItemResponse>();
        HashMap<String, String> attachments = new HashMap<String, String>();
        
        // Add the basic details
        SurveyItemResponse surveyId = new SurveyItemResponse("survey_id", null);
        surveyId.addAnswerText("1");
        answers.put("survey_id", surveyId);
        answers2.put("survey_id", surveyId);
        
        SurveyItemResponse location = new SurveyItemResponse("location", null);
        location.addAnswerText("Where I Say It Is");
        answers.put("location", location);
        answers2.put("location", location);

        SurveyItemResponse handsetSubmitTime= new SurveyItemResponse("handset_submit_time", null);
        handsetSubmitTime.addAnswerText(DatabaseHelpers.formatDateTime(new Date()));
        answers.put("handset_submit_time", handsetSubmitTime);
        answers2.put("handset_submit_time", handsetSubmitTime);

        SurveyItemResponse handsetId= new SurveyItemResponse("handset_id", null);
        handsetId.addAnswerText("000000000000000");
        answers.put("handset_id", handsetId);
        answers2.put("handset_id", handsetId);

        SurveyItemResponse interviewerId = new SurveyItemResponse("interviewer_id", null);
        interviewerId.addAnswerText("Simon Jones");
        answers.put("interviewer_id", interviewerId);
        answers2.put("interviewer_id", interviewerId);
        

        // Add the answers
        SurveyItemResponse q1 = new SurveyItemResponse("q1", null);
        q1.addAnswerText("MyAnswer1");
        answers.put("q1", q1);
        answers2.put("q1", q1);
        SurveyItemResponse q2 = new SurveyItemResponse("q2", null);
        q2.addAnswerText("MyAnswer2");
        answers.put("q2", q2);
        answers2.put("q2", q2);
        SurveyItemResponse q3 = new SurveyItemResponse("q3", null);
        q3.addAnswerText("MyAnswer3");
        answers.put("q3", q3);
        answers2.put("q3", q3);
        SurveyItemResponse q4 = new SurveyItemResponse("q4", null);
        q4.addAnswerText("MyAnswer4");
        answers.put("q4", q4);
        answers2.put("q4", q4);
       
        // Submit the survey the first time. Should work
        int firstResult  = ProcessSubmission.storeSurveySubmission(answers, attachments);

        // Submit the survey the second time. Should get an error
        int secondResult = ProcessSubmission.storeSurveySubmission(answers2, attachments);
        
        // Conduct the tests
        Assert.assertEquals(firstResult, 400);
        Assert.assertEquals(secondResult, 201);
        
    }
}
