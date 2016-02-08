package applab.surveys.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;

import org.xml.sax.SAXException;

import applab.server.DatabaseHelpers;
import applab.server.FileHelpers;
import applab.surveys.SubmissionStatus;

public class ChemonicsCustomCSVExport {

	private static final long serialVersionUID = 1L;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static boolean successfull = false;

	public ChemonicsCustomCSVExport() {
	}

	public static void main(String[] args) throws URISyntaxException {

		if(args == null || args.length <= 1) {
			System.out.println();
			System.out.println("See correct usage below");
			System.out.println("\t <jar-file> date survey-id");
			System.out.println("\t\t date 		=> This is the datetime from which to begin importing data e.g \"2015-10-23 16:26:45\"");
			System.out.println("\t\t survey-id 	=> This is the salesforce survey id of the survey whose data is to be exported e.g 20150947072");
			System.out.println();
			return;
		}
		//if(args.length > 1) {
		//	args[0] = args[0] + " " + args[1];
		//}
		System.out.println("Processing started...");
		System.out.println("In start date = " + args[0]);
		System.out.println("In survey-id  = " + args[1]);
		try {
			Calendar calFrom = Calendar.getInstance();
			calFrom.setTime(sdf.parse(args[0]));
			Date calTo = Calendar.getInstance().getTime();
			
			doApplabGet(args[0], sdf.format(calTo), args[1]);
			
			if(successfull) {
		        File targetFile = new File("./last_run");

		        FileWriter writer = new FileWriter(targetFile);
		        writer.write(sdf.format(calTo));
		        writer.close();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} 
	}

	protected static void doApplabGet(String startDateStr, String endDateStr, String salesforceSurveyId) throws ClassNotFoundException, SQLException,
			IOException, ServiceException, ParseException, SAXException, ParserConfigurationException {

		DownloadType.valueOf("Csv");
		DownloadTarget downloadTarget = DownloadTarget.valueOf("SubmissionCsv");
		String outputString = null;
        java.util.Date startDate = null;
        java.sql.Date endDate = null;
		
		switch (downloadTarget) {
		    case SubmissionCsv:
		
		        // Extract the request data
		        //String salesforceSurveyId = "20150947072";
		
		        // Extract the optional parameters
		        //if null is passed, both drafts and none drafts are fetched
		        Boolean showDraft = null;
		
		        try {
		            startDate =  DatabaseHelpers.getSqlDateFromString(startDateStr, 0);
		            //startDate.toString();
		        }
		        catch (Exception e) {		
					e.printStackTrace();
		            // Do nothing and leave the dates blank
		        }
		        try {
		            endDate   = DatabaseHelpers.getSqlDateFromString(endDateStr, 0);
		            endDate.toString();
		        }
		        catch (Exception e) {		
					e.printStackTrace();
		            // Do nothing and leave the dates blank
		        }
		   
		        java.util.Date now = new java.util.Date();
		        
		        // If we only have one of the dates provided then set the other one to now
		        if (startDate != null && endDate == null) {
		            endDate = new java.sql.Date(now.getTime());    
		            endDate.toString();
		        }
		    
		        if (startDate == null && endDate != null) {
		            startDate = new java.sql.Date(now.getTime());    
		            startDate.toString();
		        }
		
		        SubmissionStatus status = null; //SubmissionStatus.parseHtmlParameter("none");
		        //if (status != null) {
		            //status.toString();
		        //}
				System.out.println("From " + sdf.format(startDate));
				System.out.println("To " + sdf.format(endDate));
		        ChemonicsSurvey.ChemonicsSubmissionStatistics statistics = new ChemonicsSurvey(salesforceSurveyId).new ChemonicsSubmissionStatistics().getStatistics(salesforceSurveyId, startDate, endDate);
		
		        if (statistics == null) {		
		            // TODO - ErrorPage
					System.out.println("Statistics NULL");
		        }
		        else {		
		            // We have some statistics so lets load the submission
					System.out.println("Generating Stats...");
		            statistics.getSurvey().loadSubmissions(status, startDate, endDate, false, salesforceSurveyId, showDraft);
					System.out.println("Generating CSV...");
		            outputString = statistics.getSurvey().generateCsv();
		            if(statistics.getSurvey().getSubmissionOrder().size() > 0) {
		            	successfull = true;
		            }
		        }
		        break;
		    default:
		        break;
		}
		
		if (outputString == null || !successfull) {
			System.out.println("Export EMPTY");
		}
		else {		    
		    // The output is a string so just write it out
		    File file = new File("./" + endDate + ".csv");
			PrintWriter writer = new PrintWriter(file, "UTF-8");
		    writer.print(outputString);
		    writer.close();
		}
	}
    private enum DownloadType {
        Csv,
        Pdf;
    }
    
    private enum DownloadTarget {
        SubmissionCsv;
    }
}
