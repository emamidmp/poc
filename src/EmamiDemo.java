import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.*;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.sql.Timestamp;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.Process;
import java.io.OutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONObject;
import org.json.JSONArray;

//import javax.mail.*;
//import javax.mail.internet.*;
import javax.activation.*;

/**
 * Servlet implementation class EmamiDemo
 */
@WebServlet("/EmamiDemo")
public class EmamiDemo extends HttpServlet {
	private static final long serialVersionUID = 1L;
        private static String mailIds = "ruchi.tomar@emamigroup.com,geetha.kesavan@emamigroup.com";

        public static String conferenceParams = "{\"title\": \"XXXXXX\", \"scheduled_time\": \"YYYY\", \"duration\": 120, \"invite_emails\": \"ZZZZ\"}" ;
        public static String conferenceURL = "https://1click.io/api/v2/conference/schedule/?format=json";
        public static String confLinkURL = "https://1click.io/webdash/index.html#/Room/";
        public static String playBackURL = "https://1click.io/api/v2/record/playback/?confid=";
        public static String iClickKey = "ApiKey ruchi.tomar@emamigroup.com:366bf8536726e7dfc7b08ac123576c28fcae12e6";
        public static String smsAPIURL = "http://www.kookoo.in/outbound/outbound_sms.php?phone_no=";

        public static String videoConfLink = "https://1click.io/webdash/index.html#/Room/" ;
        public static String videoRecordingLink = "https://1click.io/api/v2/record/playback/?confid=";

        public static String outboundCallLink = "http://cloudagent.in/cloudAgentRestAPI/index.php/CloudAgent/CloudAgentAPI/addCamapaignData/api_key/KKaa4e2032c83732b5f62239a667d4c178/campaign_name/emami_outbound/PhoneNumber/XXXX/ruchi/testCall";
        public static String outboundCallStart = "http://cloudagent.in/cloudAgentRestAPI/index.php/CloudAgent/CloudAgentAPI/setCampaign/api_key/KKaa4e2032c83732b5f62239a667d4c178/campaign_name/emami_outbound/action/start";

        public static String smsAPIKey = "KKaa4e2032c83732b5f62239a667d4c178";
        public static EmamiConfiguration lbconfig;

    /**
     * Default constructor. 
     */
    public EmamiDemo() {
    	System.out.println("Emami Demo servlet instantiated ");
    }

    public void init() throws ServletException
    {
         System.out.println("Reading Emami load file");
         try {
            lbconfig = EmamiConfiguration.getInstance();
            lbconfig.printLBConfiguration();
         } catch(Exception ex) {
 
            System.out.println("Exception in Emami Config reading");
         }
 
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	System.out.println("**********In do get**********");
                
            String phNum = request.getParameter("outBoundCall");
            
            if ((phNum != null) && (!phNum.equals("")))
            {
                startOutboundCall(phNum); 
                return;
            }

            String docName = request.getParameter("docName");
            
            if ((docName != null))
            {
                handleDoctorLogin(request, response);
                return;
            }

            String showPatient = request.getParameter("showPatient");

            if (showPatient != null )
            {
                System.out.println("To show patient data - " + showPatient);
                showPatientData(request,response, showPatient);
                return;
            }

            String viewRecording = request.getParameter("viewRecording");

            String phoneNum = request.getParameter("phoneNumber");
          
            if (phoneNum.length() > 10)
            {
                 //truncate the phone number to 10 by taking the last 10 digits
                 int length = phoneNum.length(); 
                 int start = length - 10;
                 phoneNum = phoneNum.substring(start);
            }

            if(viewRecording != null && viewRecording.equals("yes") && phoneNum != null)
            {
                 displayRecording(request, response, phoneNum);
                 return;
            }
            
            String checkStatus = request.getParameter("checkStatus");
            String faceToFace = request.getParameter("faceToFace");
            String video = request.getParameter("video");

            System.out.println("Phone num is ------ " + phoneNum);
            System.out.println("faceToFace is ------ " + faceToFace);
            System.out.println("video is ----- " + video);
            
            if ((checkStatus != null) && checkStatus.equals("yes") && ( phoneNum != null) )
            {
                 OutputStream outputStream = response.getOutputStream();
                 if (lbconfig.isExistingCustomer(phoneNum) )
                 {
                     String str = "REGISTERED";
                     outputStream.write(str.getBytes());
                 } else {
                     String str = "NONREGISTERED";
                     outputStream.write(str.getBytes());
                 }
                 return;
            }

            if ((phoneNum == null) || (phoneNum.equals("")))
            {
                 System.out.println("Phone number is null");
                 return;
            }

            if (lbconfig.isExistingCustomer(phoneNum) )
            {
                  System.out.println(" This is an existing customer");

            } else if (faceToFace == null && video == null) {

                  request.setAttribute("phoneNum", phoneNum);
                  System.out.println(" This is a new customer");
                  request.getRequestDispatcher("/WEB-INF/addNew.jsp").forward(request, response);
                  return;
            }

            CustomerData custData = lbconfig.getCustomer(phoneNum);
            if (custData == null)
            {
                  System.out.println("Customer data not found");
                  return;
            }
            
            
            if (faceToFace == null && video == null)
            {
              /* Path for scheduling the appointment */
               request.setAttribute("phoneNum", phoneNum);

               request.setAttribute("patientName", custData.getCustomerName());
               request.setAttribute("patientLocation", custData.getCustomerLocation());
               request.setAttribute("patientDisease", custData.getCustomerDisease());
               request.setAttribute("patientVitals", custData.getCustomerVitals());
               request.getRequestDispatcher("/WEB-INF/page.jsp").forward(request, response);
            } else if (faceToFace != null) {

              try{
               /* Path for SMS API integration */

               System.out.println("Scheduling a face to face appointment");
               request.setAttribute("phoneNum", phoneNum);

               long currentTime = System.currentTimeMillis();
               long secondsInDay = 24*60*60*1000;
               currentTime = currentTime + secondsInDay;

               Timestamp timestamp = new Timestamp(currentTime);
               TimeZone timeZone = TimeZone.getTimeZone("IST");


               DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd%20HH:00");
               dateFormat.setTimeZone(timeZone);

               String currentDate = dateFormat.format(timestamp);

               
               String message = "Your%20Face%20To%20Face%20Appointment%20Has%20Been%20Scheduled%20Successfully%20With%20Dr.%20Dev%20Verma%20For%20" + currentDate;

                  request.setAttribute("message",message);

                  sendTheSMS(phoneNum, message);
   
                  request.getRequestDispatcher("/WEB-INF/f2f.jsp").forward(request, response);
               }catch(Exception ex) {
                   
                   System.out.println("Exception in SMS send :: " + ex);
               }
            } else if(video != null) {

               System.out.println("Scheduling a video consultation for " + custData.getCustomerMailId());
               String confid = scheduleVideoAppointment(request, custData.getCustomerMailId());
               if (confid.equals("ERROR"))
               {
                    
                    System.out.println("Error in scheduling video appointment");
                    request.getRequestDispatcher("/WEB-INF/errorvideo.jsp").forward(request, response);
                    return;
               }

               lbconfig.updateAppointment(phoneNum, confid);

               request.setAttribute("mailids",custData.getCustomerMailId());
               //request.getRequestDispatcher("/WEB-INF/video.jsp").forward(request, response);
               request.getRequestDispatcher("/WEB-INF/video.jsp").forward(request, response);
            }
	}


     public void  displayRecording(HttpServletRequest request,HttpServletResponse  response, String phoneNum)
     {
          List<CustomerData> custDataList = lbconfig.GetCustomerDataList();   

          for (CustomerData customerData: custDataList)
          {
             if (customerData.getCustomerPhone().equals(phoneNum)) {

                 request.setAttribute("name", customerData.getCustomerName());
                 request.setAttribute("location", customerData.getCustomerLocation());
                 request.setAttribute("disease", customerData.getCustomerDisease());
                 request.setAttribute("vitals", customerData.getCustomerVitals());
                 request.setAttribute("mailid", customerData.getCustomerMailId());
                 List<String> custRecordingList = null;
                 if ( (customerData.getCustomerAppointment() != null) && (!customerData.getCustomerAppointment().equals("")) )
                 {
                       custRecordingList = EmamiDemo.queryVideoRecording( customerData.getCustomerAppointment());
                       customerData.setCustomerRecording(custRecordingList);
                 }

                 if (custRecordingList != null)
                 {
                     int i = 0;
                     for (String custRecording : custRecordingList) {
                          //builder.append("<p><a href=\"" + custRecording + "\">" + custRecording + "</a></p>");
                          if (i == 0) {
                              request.setAttribute("recording", custRecording);
                          } else if (i == 1) {
                              request.setAttribute("recording1", custRecording);
                          } else if (i == 2) {
                              request.setAttribute("recording2", custRecording);
                          } else {
                              request.setAttribute("recording", custRecording);
                          }
                          ++i;
                      }
                      System.out.println("Setting conference id " + customerData.getCustomerAppointment());
                 } else {
                      request.setAttribute("recording", null);
                 }

                 try{
                    request.getRequestDispatcher("/WEB-INF/recording.jsp").forward(request, response);
                 } catch(Exception ex) {
                     System.out.println("Exception in recording data display");
                 }
                 return;
             }
        }
     }

public void showPatientData(HttpServletRequest request, HttpServletResponse response, String phoneNum)
  {
       List<CustomerData> custDataList = lbconfig.GetCustomerDataList();   

       for (CustomerData customerData: custDataList)
       {
             if (customerData.getCustomerPhone().equals(phoneNum)) {
                 System.out.println(" Patient found in the system ");

                 request.setAttribute("name", customerData.getCustomerName());
                 request.setAttribute("location", customerData.getCustomerLocation());
                 request.setAttribute("disease", customerData.getCustomerDisease());
                 request.setAttribute("vitals", customerData.getCustomerVitals());
                 request.setAttribute("mailid", customerData.getCustomerMailId());
                 request.setAttribute("confid", customerData.getCustomerAppointment());
                 request.setAttribute("phoneNum", customerData.getCustomerPhone());
                 System.out.println("Setting conference id " + customerData.getCustomerAppointment());
                 
                 try{
                    request.getRequestDispatcher("/WEB-INF/patient.jsp").forward(request, response);
                 } catch(Exception ex) {
                     System.out.println("Exception in patient data display");
                 }
                 return;
           }
       }
    }


    void handleDoctorLogin(HttpServletRequest request,  HttpServletResponse response)
        {
            try{
            List<CustomerData> custDataList = lbconfig.GetCustomerDataList();   
            StringBuilder builder = new StringBuilder();
            builder.append("<!DOCTYPE html>");
            builder.append("<html lang=\"en\">");
            builder.append("<head><title>Title</title></head>");
            builder.append("<img src=\"images/zandu.jpeg\" alt=\"Emami Logo\" align=\"center\" height=\"42\" width=\"102\">");
            builder.append("<div style=\"width:100%; height:3px; margin:10px 0px 10px; background-color:#bbbbbb\">");
            builder.append("<br>");

            builder.append("<body><h1>List of Patients </h1>");
            builder.append("<p>-------------------------------------</p>");
            for (CustomerData customerData: custDataList)
            {
                     builder.append("<p>");
		     builder.append("<a href=\"" + "http://54.255.204.71:8080/EmamiDemo/EmamiDemo?showPatient=" + customerData.getCustomerPhone() + "\">" +  customerData.getCustomerName() + "</a>");
                     builder.append("</p>");
            }

            builder.append("<p>-------------------------------------</p>");
            builder.append("</body>");
            builder.append("</html>");
            System.out.println("Writing output as " + builder.toString());

            OutputStream outputStream = response.getOutputStream();
            outputStream.write(builder.toString().getBytes());

            }catch(Exception ex) {
                System.out.println("Exception in doctors view of patient data");
            }
                 
/*
            try{

            List<CustomerData> custDataList = lbconfig.GetCustomerDataList();   
            StringBuilder builder = new StringBuilder();
            builder.append("<!DOCTYPE html>");
            builder.append("<html lang=\"en\">");
            builder.append("<head><title>Title</title></head>");
            builder.append("<body><h1>List of Patients </h1>");

            for (CustomerData customerData: custDataList)
            {
                 builder.append("<p>-------------------------------------</p>");
                 builder.append("<p>Patient Name::" + customerData.getCustomerName() + "</p>");
                 builder.append("<p>Patient Location::" + customerData.getCustomerLocation() + "</p>");
                 builder.append("<p>Patient Disease::" + customerData.getCustomerDisease() + "</p>");
                 builder.append("<p>Patient Vitals::" + customerData.getCustomerVitals() + "</p>");
                 builder.append("<p>Patient MailId::" + customerData.getCustomerMailId() + "</p>");
                 builder.append("<p>Video Conference Link ::");
                 
                 if ( (customerData.getCustomerAppointment() != null) && (!customerData.getCustomerAppointment().equals("") ) )
                 {
                     String confLink =  confLinkURL + customerData.getCustomerAppointment();
                     builder.append("<a href=\"" + confLink +  "\">" + confLink + "</a></p>");
                     //builder.append("<iframe src =\"WebContent/WEB-INF/emami.html\" width=\"100%\" height=\"300\" class=\"tutorial\"></iframe>");

                 }

                 builder.append("<p>Conference Recording::</p>" );

                 List<String> custRecordingList = null;
                 if ( (customerData.getCustomerAppointment() != null) && (!customerData.getCustomerAppointment().equals("")) )
                 {
                       custRecordingList = EmamiDemo.queryVideoRecording( customerData.getCustomerAppointment());
                       customerData.setCustomerRecording(custRecordingList);
                 }
                 if (custRecordingList != null)
                 {
                     for (String custRecording : custRecordingList) {
                          builder.append("<p><a href=\"" + custRecording + "\">" + custRecording + "</a></p>");
                      }
                 }
            }

            builder.append("<p>-------------------------------------</p>");
            builder.append("</body>");
            builder.append("</html>");
            System.out.println("Writing output as " + builder.toString());

            OutputStream outputStream = response.getOutputStream();
            outputStream.write(builder.toString().getBytes());

            }catch(Exception ex) {
                System.out.println("Exception in doctors view of patient data");
            }
          */
        }
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	    System.out.println("In do post");
            String name = request.getParameter("Name");
            String location = request.getParameter("Location");
            String disease = request.getParameter("Disease");
            String mail = request.getParameter("Mail");
            String weight = request.getParameter("Weight");
            String phoneNum = request.getParameter("PhoneNum");

            System.out.println("name is ------ " + name);
            System.out.println("location is ----- " + location);
            System.out.println("disease is ----- " + disease);
            System.out.println("mail is ----- " + mail);
            System.out.println("weight is ----- " + weight);
            System.out.println("phoneNum is ----- " + phoneNum);
            

            if ( lbconfig.isExistingCustomer(phoneNum) == true )
            {
                 System.out.println("Phone number is already registered");
                 return;
            } 

            lbconfig.addCustomer(name, location, phoneNum, disease, weight, mail, null);  
            String message = "You%20Have%20Been%20Successfully%20Registered%20With%20EmamiHealthCare%20Portal";
            sendTheSMS(phoneNum, message);
            String mailMessage = "You Have Been Successfully Registered With EmamiHealthCare Portal";
            //sendTheMail(mail, mailMessage);
            request.setAttribute("custName", name); 
            request.setAttribute("phoneNum", phoneNum); 
            
            request.getRequestDispatcher("/WEB-INF/added.jsp").forward(request, response);
       }

       public static List<String> queryVideoRecording(String confid)
       {
          List<String> recordingList = new ArrayList<String>();
          String recordingURL = null;
          try {

                URL url = new URL("https://1click.io/api/v2/record/playback/?confid=" + confid);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setRequestProperty("Authorization", "ApiKey ruchi.tomar@emamigroup.com:366bf8536726e7dfc7b08ac123576c28fcae12e6");

                if (conn.getResponseCode() != 200) {
                        System.out.println("Failed : HTTP error code : "
                                        + conn.getResponseCode());
                         return null;
                }

                System.out.println("Query recording URL returned " + conn.getResponseCode());

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));
                String output;
                System.out.println("Output from Server .... \n");
                output = br.readLine();
                System.out.println(output);

                JSONObject serviceResult = new JSONObject(output);

                System.out.println("JSon object created");

                JSONArray items = serviceResult.getJSONArray("objects");

                System.out.println("Extracted object array");

                for (int i = 0; i < items.length(); i++) {
                   JSONObject obj = items.getJSONObject(i);
                     System.out.println(" Recording URL is " + obj.getString("url") + " and duration is " + obj.getInt("duration"));
                     int duration = obj.getInt("duration");

                     if ( duration > 0 )
                     {
                         recordingURL = obj.getString("url");
                         recordingList.add(recordingURL);
                     } else {
                         System.out.println("Recording not yet ready");
                     }
 
                }

                conn.disconnect();

          } catch (Exception e) {
             System.out.println("Exception recved " + e);

          }
          return recordingList;
       }

       public String scheduleVideoAppointment(HttpServletRequest request, String mailId) 
       {
          long currentTime = System.currentTimeMillis();
          String config = Long.toString(currentTime);
          try{

           URL url = new URL(conferenceURL);
	   HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	   conn.setRequestMethod("POST");
           conn.setDoOutput(true);
	   conn.setRequestProperty("Content-Type", "application/json");
	   conn.setRequestProperty("Authorization", iClickKey);


           Timestamp timestamp = new Timestamp(System.currentTimeMillis());
           TimeZone timeZone = TimeZone.getTimeZone("IST");      

           DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
           dateFormat.setTimeZone(timeZone);    

           String currentDate = dateFormat.format(timestamp);

           String confParams = conferenceParams.replaceAll("XXXXXX", config);
           confParams = confParams.replaceAll("YYYY", currentDate);
           confParams = confParams.replaceAll("ZZZZ", mailId);

           System.out.println("Date is " + currentDate + " and config id is " + config + " and conferenceParams is ::: " + confParams);

           OutputStream os = conn.getOutputStream();
           os.write(confParams.getBytes());
           os.flush();


           System.out.println("Response from Server:" + conn.getResponseCode());
	   if (conn.getResponseCode() != 200) {
                     return "ERROR"; 
	   }
 
	   BufferedReader br = new BufferedReader(new InputStreamReader(
			(conn.getInputStream())));
 
	   String output;
	   output = br.readLine();
	   System.out.println("Output from Server .... " + output);
 
	   conn.disconnect();
 
	  } catch (MalformedURLException e) {
 
		e.printStackTrace();
 
	  } catch (IOException e) {
 
		e.printStackTrace();

          }
          return config;
    }

    public void sendTheSMS(String phoneNum, String message) 
    {
          String sendSMS = smsAPIURL + phoneNum + "&api_key=" + smsAPIKey + "&message=" + message ;
          System.out.println("SMS URL is  "  + sendSMS);
          String[] smsArgs = new String[2];
          smsArgs[0] = "/usr/bin/curl";
          smsArgs[1] = sendSMS;
               
          try{
               
          Process p = Runtime.getRuntime().exec(smsArgs);
          System.out.println("Executed smsArgs");

          p.waitFor();
          System.out.println("Executed wait for ");

          BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
 
          String line = "";			
          StringBuffer sb = new StringBuffer();
          while ((line = reader.readLine())!= null) {
	       sb.append(line + "\n");
          }

             System.out.println("SMS Request returned ::: " + sb.toString());
          } catch (Exception ex) {
              System.out.println("Exception in send SMS " + ex);
          }
    }

/*
    public void sendTheMail(String mailId, String msg) 
    {

      // Sender's email ID needs to be mentioned
      String username = "ruchtomar@gmail.com";

      final String password = "dpsblr#012";
 
      Properties props = new Properties();
      props.put("mail.smtp.auth", "true");
      props.put("mail.smtp.starttls.enable", "true");
      props.put("mail.smtp.host", "smtp.gmail.com");
      props.put("mail.smtp.port", "587");
 
      Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
		}
      });
 
	try {
 
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress("ruchtomar@gmail.com"));
		message.setRecipients(Message.RecipientType.TO,
		InternetAddress.parse(mailId));
		message.setSubject("Welcome to Emami Healthcare Portal");
		message.setText(msg);

		Transport.send(message);
 
		System.out.println("Done");
 
     	   } catch (MessagingException e) {
			throw new RuntimeException(e);
	   }
	}
     */

     public String startOutboundCall(String phoneNum)
     {
       
        try{

          String outboundCall = outboundCallLink.replaceAll("XXXX", phoneNum);

          Process p = Runtime.getRuntime().exec("curl " + outboundCall);
          p.waitFor();

          BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
 
          String line = "";			
          StringBuffer sb = new StringBuffer();
          while ((line = reader.readLine())!= null) {
	       sb.append(line + "\n");
          }

          System.out.println("SMS Request returned ::: " + sb.toString());

          p = Runtime.getRuntime().exec("curl " + outboundCallStart);
          p.waitFor();

          reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
 
          line = "";			
          StringBuffer sb1 = new StringBuffer();
          while ((line = reader.readLine())!= null) {
	       sb1.append(line + "\n");
          }

          System.out.println("SMS Request returned ::: " + sb.toString());

          return sb1.toString();

     } catch(Exception ex) {
          System.out.println("Exception in generating outbound call");
          return "ERROR";
     }

   }
}
