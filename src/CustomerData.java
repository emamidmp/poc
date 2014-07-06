import java.util.List;
public class CustomerData
{
    private String customerName;
    private String customerLocation;
    private String customerPhone;
    private String customerDisease;
    private String customerVitals;
    private String customerMailId;
    private String customerAppointment;
    private List<String> customerRecording;

    public CustomerData(String custName,String custLocation, String custPhone, String custDisease, String custVital, String custMailId, String custAppo)
    {
        customerName = custName;
        customerLocation = custLocation;
        customerPhone = custPhone;
        customerDisease = custDisease;
        customerVitals = custVital;
        customerMailId = custMailId;
        customerAppointment = custAppo;
    }

    public String getCustomerName()
    {
         return customerName;
    }

    public String getCustomerLocation()
    {
         return customerLocation;
    }

    public String getCustomerPhone()
    {
         return customerPhone;
    }
    public String getCustomerDisease()
    {
         return customerDisease;
    }
    public String getCustomerVitals()
    {
         return customerVitals;
    }
    public String getCustomerMailId()
    {
         return customerMailId;
    }
    public String getCustomerAppointment()
    {
         return customerAppointment;
    }
    public void setCustomerAppointment(String appint)
    {
         customerAppointment = appint;
    }
    public List<String> getCustomerRecording()
    {
         return customerRecording;
    }
    public void setCustomerRecording(List<String> custRecording)
    {
         customerRecording = custRecording;
    }
}
