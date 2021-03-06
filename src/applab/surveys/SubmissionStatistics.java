package applab.surveys;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import applab.surveys.server.SurveyDatabaseHelpers;

/**
 * represents the set of submission statistics for a given survey (based on SF id)
 * 
 */
public class SubmissionStatistics {
    private Survey survey;
    private HashMap<SubmissionStatus, Integer> numberOfSubmissions;

    private SubmissionStatistics(Survey survey) {
        assert (survey != null) : "internal API: caller must ensure non-null survey";
        this.survey = survey;
        this.numberOfSubmissions = new HashMap<SubmissionStatus, Integer>();
    }

    public int getNumberOfSubmissions(SubmissionStatus status) {
        return this.numberOfSubmissions.get(status);
    }

    public Survey getSurvey() {
        return this.survey;
    }

    public static SubmissionStatistics getStatistics(String salesforceSurveyId, java.sql.Date startDate, java.sql.Date endDate)
            throws ClassNotFoundException, SQLException {

        Connection connection = SurveyDatabaseHelpers.getReaderConnection();
        SubmissionStatistics statistics = null;

        // Build the SQL for the query.
        StringBuilder commandText = new StringBuilder();
        commandText.append(" SELECT surveys.id as SurveyPrimaryKey,");
        commandText.append(" SUM(case when submissions.survey_status='Approved' then 1 else 0 end) AS Approved,");
        commandText.append(" SUM(case when submissions.survey_status='Rejected' then 1 else 0 end) AS Rejected,");
        commandText.append(" SUM(case when submissions.survey_status='Pending' then 1 else 0 end) AS Pending,");
        commandText.append(" SUM(case when submissions.survey_status='Not Reviewed' then 1 else 0 end) As NotReviewed,");
        commandText.append(" SUM(case when submissions.survey_status='Duplicate' then 1 else 0 end) AS Duplicate");
        commandText.append(" FROM zebrasurveysubmissions AS submissions, zebrasurvey AS surveys");
        commandText.append(" WHERE submissions.survey_id = surveys.id");
        commandText.append(" AND surveys.survey_id = ?");

        if (startDate != null && endDate != null) {
            commandText.append(" AND server_entry_time >= ?");
            commandText.append(" AND server_entry_time <= ?");
        }
        commandText.append(" GROUP BY submissions.survey_id");

        // Prepare the statement
        PreparedStatement query = connection.prepareStatement(commandText.toString());

        // Pass the variables to the prepared statement
        query.setString(1, salesforceSurveyId);

        if (startDate != null && endDate != null) {
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(startDate);
            startCalendar.set(Calendar.HOUR_OF_DAY, 0);
            startCalendar.set(Calendar.MINUTE, 0);
            startCalendar.set(Calendar.SECOND, 0);
            Timestamp startTimeStamp = new Timestamp(startCalendar.getTimeInMillis());
            query.setTimestamp(2, startTimeStamp);
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(endDate);
            endCalendar.set(Calendar.HOUR_OF_DAY, 23);
            endCalendar.set(Calendar.MINUTE, 59);
            endCalendar.set(Calendar.SECOND, 59);
            Timestamp endTimeStamp = new Timestamp(endCalendar.getTimeInMillis());
            query.setTimestamp(3, endTimeStamp);

        }

        // Execute the query
        ResultSet resultSet = query.executeQuery();

        // Check if we have any results for this survey
        if (resultSet.next()) {

            // If so, grab some base information
            Survey parentSurvey = new Survey(resultSet.getInt("SurveyPrimaryKey"), salesforceSurveyId);
            statistics = new SubmissionStatistics(parentSurvey);

            // And count up our submission statistics
            int notReviewedCount = resultSet.getInt("NotReviewed");
            int pendingCount = resultSet.getInt("Pending");
            int approvedCount = resultSet.getInt("Approved");
            int rejectedCount = resultSet.getInt("Rejected");
            int duplicateCount = resultSet.getInt("Duplicate");

            statistics.numberOfSubmissions.put(SubmissionStatus.NotReviewed, notReviewedCount);
            statistics.numberOfSubmissions.put(SubmissionStatus.Pending, pendingCount);
            statistics.numberOfSubmissions.put(SubmissionStatus.Approved, approvedCount);
            statistics.numberOfSubmissions.put(SubmissionStatus.Rejected, rejectedCount);
            statistics.numberOfSubmissions.put(SubmissionStatus.Duplicate, duplicateCount);
        }

        return statistics;
    }
}