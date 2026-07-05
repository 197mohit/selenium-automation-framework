
package scripts;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.paytm.apphelpers.AuthHelpers;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.datareader.DataReaderUtil;
import com.paytm.framework.reporting.listenerDecorators.DefaultListener;
import com.paytm.framework.utils.ServerUtil;
import com.paytm.listeners.FailureHandlingListener;
import io.qameta.allure.Allure;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.paytm.LocalConfig.JENKINS_SERVER_URI;
import static com.paytm.dto.PaymentDTO.*;

@Owner("Nikunj")
@Listeners({DefaultListener.class, FailureHandlingListener.class})
public class LogCheckBash  {
    //public static List<String> result = new ArrayList<>();
    private static final ServerUtil serverUtil = new ServerUtil();


    @BeforeSuite
    public void searchLog() throws JSchException,IOException
    {
         List<String> result = new ArrayList<>();

        ServerUtil serverutil = new ServerUtil();
        Session session = serverutil.getSession(JENKINS_SERVER_URI);
        ChannelExec channelExec = serverutil.getChannel(session, "exec");
        channelExec.setCommand("sh /paytm/script/new-scripts/logsearch.sh");
        channelExec.connect();
          InputStream in = channelExec.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
             result.add(line);
                System.out.println("Result SearchLog:: "+result);
             }
    }

    @Test(description="validate cards using regex",priority = 1003)
    public void searchCardUsingRegex() throws Exception{
        List<String> regexCard=new ArrayList<>();
        regexCard.add("\\D*?4\\D*?8\\D*?9\\D*?3\\D*?7\\D*?7\\D*?2\\D*?9\\D*?0\\D*?3\\D*?1\\D*?1\\D*?4\\D*?9\\D*?3\\D*?8\\D*?");
        int numberOfFilesFound= matcherFunction(regexCard,"Card");
        Assertions.assertThat(numberOfFilesFound).withFailMessage("Expecting No Saved Cards to be found in logs. Please see attached Logs.\nFound " + numberOfFilesFound + " Files").isEqualTo(0);

    }

    @Test(description = "Validate savedCards absence In Logs", priority = 1000)
    public void testSavedCardPresenceInLogs() throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        List<String> cardsList = Arrays.asList(
                AMEX_CARD_NUMBER,
                CREDIT_CARD_FOR_FAILED_TXN,
                MASTER_ICICI_DEBIT_CARD_NUMBER,
                MAESTRO_DEBIT_CARD_NUMBER,
                RUPAY_CARD_NUMBER,
                DINERS_CARD_NUMBER,
                INVALID_CARD,
                DEBIT_CARD_FOR_FAILED_TXN,
                BAJAJ_FINSERV_CREDIT_CARD_NUMBER,
                paymentDTO.getCreditCardNumber());
       int numberOfFilesFound= matcherFunction(cardsList, "Card");
       Assertions.assertThat(numberOfFilesFound).withFailMessage("Expecting No Saved Cards to be found in logs. Please see attached Logs.\nFound " + numberOfFilesFound + " Files").isEqualTo(0);
    }

    @Test(description = "Validate token absence In Logs", priority = 1001)
    public void testTokenPresenceInLogs() throws Exception {
        Set<String> tokens = AuthHelpers.tokens;
        int numberOfFilesFound= matcherFunction(new ArrayList<>(tokens), "Token");
        Assertions.assertThat(numberOfFilesFound).withFailMessage("Expecting No Token to be found in logs. Please see attached Logs.\nFound " + numberOfFilesFound + " Files").isEqualTo(0);
    }

    @Test(description = "Validate Phone Number absence In Logs", priority = 1001, enabled=false)
    public void testPhonePresenceInLogs() throws Exception {
        String[][] users = DataReaderUtil.readCSV("users.csv", "users");
        List<String> phoneNoList = new ArrayList<>();
        for (String[] user : users) {
            phoneNoList.add(user[0]);
        }
        int numberOfFilesFound= matcherFunction(phoneNoList, "Phone");
        Assertions.assertThat(numberOfFilesFound).withFailMessage("Expecting No Phone to be found in logs. Please see attached Logs.\nFound " + numberOfFilesFound + " Files").isEqualTo(0);
    }


    @Test(description = "Validate vpa absence In Logs", priority = 1002, enabled=false)
    public void testVPAPresenceInLogs() throws Exception {
        List<String> VPAList = new ArrayList<>();
        VPAList.add(new PaymentDTO().getVpa());
        int numberOfFilesFound= matcherFunction(VPAList, "VPA");
        Assertions.assertThat(numberOfFilesFound).withFailMessage("Expecting No VPA to be found in logs. Please see attached Logs.\nFound " + numberOfFilesFound + " Files").isEqualTo(0);
    }


    @Step("Search for the following {1} with values: {0}")
    private int matcherFunction(List<String> matchList, String type) throws JSchException, IOException {
        List<String> result = new ArrayList<>();
        String matchingString="";
        for(String param:matchList){
            matchingString+=param+",";
        }

        Session session=serverUtil.getSession(JENKINS_SERVER_URI);
        ChannelExec channelExec = serverUtil.getChannel(session, "exec");
        System.out.println("MatchList:: "+matchList);
        matchingString=matchingString.substring(0,matchingString.length()-1);
        System.out.println("matchingstring:: "+matchingString);
        InputStream in = channelExec.getInputStream();
        channelExec.setCommand("sh /paytm/script/new-scripts/searchscript.sh " + matchingString);
        channelExec.connect();

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = reader.readLine()) != null) {
            result.add(line);
            System.out.println("ResultSearchScript:: "+result);
        }
        attachLogs(result);
        //channelExec.disconnect();
        //session.disconnect();
        return result.size();
    }

    public void attachLogs(List<String> result) throws JSchException, IOException{
        for(String filename:result)
        {
            List<String> fileContent = new ArrayList<String>();
            Session session = serverUtil.getSession(JENKINS_SERVER_URI);
            ChannelExec channelExec1 = serverUtil.getChannel(session, "exec");
            InputStream ins=channelExec1.getInputStream();
            System.out.println("FileContentMethod");
            channelExec1.setCommand("sh /paytm/script/new-scripts/fileContent.sh "+filename);
            channelExec1.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(ins));
            String outline;
            while ((outline = br.readLine()) != null) {
                fileContent.add("\n====================================================\n"
                        +outline);
            }
            Allure.addAttachment(filename,fileContent.toString());
        }}
}

