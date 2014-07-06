import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.util.ArrayList;
import java.util.List;

/*
 * The Stringo Load Balancer Configuration Store
 */
public class EmamiConfiguration {

   /*
    * The singleton instance of the Load Balancer Configuration Store
    */
    private static EmamiConfiguration lbconfig;

   /*
    * List to store the instance data from the configuration file
    */
    private List<CustomerData> customerDataList;

    protected EmamiConfiguration()
    {
    }

    boolean isExistingCustomer(String phone)
    {
        for (CustomerData custData: customerDataList)
        {
            if (custData.getCustomerPhone().equals(phone) )
            {
                  System.out.println("Is existing customer");
                  return true;
            }
        } 
        System.out.println("Is new customer");
        return false;
    }

    public CustomerData getCustomer(String phone)
    {
        for (CustomerData custData: customerDataList)
        {
            if (custData.getCustomerPhone().equals(phone) )
            {
                  return custData;
            }
        } 
        System.out.println("Customer not found");
        return null;
    }

    void updateRecording(String phone, List<String> recordingLink)
    {
        for (CustomerData custData: customerDataList)
        {
            if (custData.getCustomerPhone().equals(phone) )
            {
                  custData.setCustomerRecording(recordingLink);
            }
        } 
    }

    void updateAppointment(String phone, String appoLink)
    {
        for (CustomerData custData: customerDataList)
        {
            if (custData.getCustomerPhone().equals(phone) )
            {
                  custData.setCustomerAppointment(appoLink);
            }
        } 
    }


    void addCustomer(String customerName, String customerLocation, String customerPhone, String customerDisease, String customerVitals, String customerMailIds, String customerAppointment)
    {
        customerDataList.add( new CustomerData(customerName,customerLocation,customerPhone,customerDisease,customerVitals, customerMailIds, customerAppointment));
 
    }

    public static EmamiConfiguration getInstance() throws Exception
    {
        if ( lbconfig == null ) {
             lbconfig = new EmamiConfiguration();
             lbconfig.ReadLBConfiguration(); 
        }
        return lbconfig;
    }
    

    private void ReadLBConfiguration() throws Exception
    {

     /*
      * Read and extract the configuration parameters for Stringo Load Balancer
      */
      try {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        //System.out.println(" Parsing the file object");
        Document doc = dBuilder.parse(this.getClass().getResourceAsStream("EmamiConfiguration.xml"));

        doc.getDocumentElement().normalize();

        System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
        /*
        NodeList nList = doc.getElementsByTagName("INSTANCE_PARAMETER");

        for (int temp = 0; temp < nList.getLength(); temp++) {

           Node nNode = nList.item(temp);

           if (nNode.getNodeType() == Node.ELEMENT_NODE) {

              Element eElement = (Element) nNode;

              String tagReturnVal;

              tagReturnVal =  getTagValue("AWS_REGION", eElement);
              if  (!tagReturnVal.equals(""))
                   region = tagReturnVal;

              tagReturnVal =  getTagValue("AWS_INSTANCE_TYPE", eElement);
              if  (!tagReturnVal.equals(""))
                   instanceType = tagReturnVal;

              tagReturnVal =  getTagValue("AWS_INSTANCE_MONITORING", eElement);
              if  (!tagReturnVal.equals(""))
                   monitoring = new Boolean(tagReturnVal);

         }
       }
       */

       NodeList nList = doc.getElementsByTagName("CUSTOMERDATA");
       customerDataList = new ArrayList<CustomerData>();

       for (int temp = 0; temp < nList.getLength(); temp++) {
           Node nNode = nList.item(temp);
           if (nNode.getNodeType() == Node.ELEMENT_NODE) {
              Element eElement = (Element) nNode;
              String customerName = getTagValue("CUSTOMERNAME", eElement);
              String customerLocation = getTagValue("CUSTOMERLOCATION",eElement);
              String customerPhone = getTagValue("CUSTOMERPHONE",eElement);
              String customerDisease = getTagValue("CUSTOMERDISEASE",eElement);
              String customerVitals = getTagValue("CUSTOMERVITALS",eElement);
              String customerMailId = getTagValue("CUSTOMERMAILID",eElement);
              String customerAppointment = getTagValue("CUSTOMERAPPOINTMENT",eElement);
              String customerRecording = getTagValue("CUSTOMERRECORDING",eElement);
              customerDataList.add( new CustomerData(customerName,customerLocation,customerPhone,customerDisease,customerVitals,customerMailId,customerAppointment));
            }
         }

       } catch (Exception ex) {
            System.out.println("Exception in parsing the configuration file" 
                                + ex.toString());
            throw ex;
     }
  }

  void printLBConfiguration()
  {
      System.out.println( "---------------------------------");

      for (CustomerData custData : customerDataList) {
            System.out.println(custData.getCustomerName() );
            System.out.println(custData.getCustomerLocation());
            System.out.println(custData.getCustomerPhone());
            System.out.println(custData.getCustomerDisease());
            System.out.println(custData.getCustomerVitals());
            System.out.println(custData.getCustomerRecording());

      }

      System.out.println( "---------------------------------");

  }

  private static String getTagValue(String sTag, Element eElement) 
  {
        
	NodeList nlList = eElement.getElementsByTagName(sTag);
        
        if ((nlList != null) && (nlList.getLength() > 0)) {
	    NodeList chNList = nlList.item(0).getChildNodes();
            if ( (chNList != null) && (chNList.getLength() > 0)) {
                Node nValue = (Node) chNList.item(0);
	        return nValue.getNodeValue();
            }
        }
        return "";
  }
 
  List<CustomerData> GetCustomerDataList()
  {
       return customerDataList;
  }
}
