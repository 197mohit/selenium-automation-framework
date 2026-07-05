
package scripts;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.AuthHelpers;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.datareader.DataReaderUtil;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.utils.EsUtil;
import io.qameta.allure.Allure;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import org.assertj.core.api.Assertions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import java.net.UnknownHostException;
import java.util.*;

import static com.paytm.dto.PaymentDTO.*;

@Owner("Nikunj")
public class LogCheck{
    final EsUtil esUtil = EsUtil.getInstance();

    @Test(description = "Validate card Numbers absence In Logs", priority = 999)
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
        Map<String, List<String>> validHits = matcherFunction(cardsList, "Card");
        Assertions.assertThat(validHits.size()).withFailMessage("Expecting No Saved Cards to be found in logs. Please see attached Logs.\nFound " + validHits.size() + " hits").isEqualTo(0);
    }




    @Test(description = "Validate token absence In Logs", priority = 999)
    public void testTokenPresenceInLogs() throws Exception {
        Set<String> tokens = AuthHelpers.tokens;
        Map<String, List<String>> validHits = matcherFunction(new ArrayList<>(tokens), "Token");
        Assertions.assertThat(validHits.size()).withFailMessage("Expecting No Saved Cards to be found in logs. Please see attached Logs.\nFound " + validHits.size() + " hits").isEqualTo(0);
    }

   // @Test(description = "Validate Phone Number absence In Logs", priority = 999, enabled = false)
    public void testPhonePresenceInLogs() throws Exception {
        String[][] users = DataReaderUtil.readCSV("users.csv", "users");
        List<String> phoneNoList = new ArrayList<>();
        for (String[] user : users) {
            phoneNoList.add(user[0]);
        }
        Map<String, List<String>> validHits = matcherFunction(phoneNoList, "Phone No.");
        Assertions.assertThat(validHits.size()).withFailMessage("Expecting No Saved Cards to be found in logs. Please see attached Logs.\nFound " + validHits.size() + " hits").isEqualTo(0);
    }

   // @Test(description = "Validate vpa absence In Logs", priority = 999, enabled = false)
    public void testVPAPresenceInLogs() throws Exception {
        List<String> VPAList = new ArrayList<>();
        VPAList.add(new PaymentDTO().getVpa());
        Map<String, List<String>> validHits = matcherFunction(VPAList, "VPA");
        Assertions.assertThat(validHits.size()).withFailMessage("Expecting No VPAs to be found in logs. Please see attached Logs.\nFound " + validHits.size() + " hits").isEqualTo(0);
    }




    private Map<String, List<String>> matcherFunction(List<String> matchList, String type) throws UnknownHostException {

        Map<String, List<String>> validHits = new HashMap<>();
        Date date = new Date();
        long currentTime = date.getTime();
        long backTime = new DateTime().minusHours(4).toDate().getTime();
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        for (String card : matchList) {

            queryBuilder.should(QueryBuilders.termQuery("message", card));

            //queryBuilder.should(QueryBuilders.matchQuery("message", "\""+card+"\""));
            Reporter.report.info("Tesing for " + type + " : " + card);
        }
        queryBuilder.minimumShouldMatch(1);
        queryBuilder.must(QueryBuilders.rangeQuery("@timestamp")
                .gte(backTime).lte(currentTime));
        //Reporter.report.info("Query: " + queryBuilder.toString());
       // System.out.println("query:: "+queryBuilder);
        SearchHits searchHits = executeQuery(queryBuilder);
        for (int i = 0; i < searchHits.getHits().length; i++) {
            String logFileName = searchHits.getHits()[i].getSourceAsMap().get("source").toString();
            if (validHits.containsKey(logFileName)) {
                validHits.get(logFileName).add(searchHits.getHits()[i].getSourceAsMap().get("message").toString()+
                        "\n"+"---------------------------------------------------------"+"\n");
            } else {
                List<String> message = new ArrayList<>();
                message.add(searchHits.getHits()[i].getSourceAsMap().get("message").toString()+
                        "\n"+"---------------------------------------------------------"+"\n");
                validHits.put(logFileName, message);
            }
            // validHits.add(searchHits.getHits()[0])
            //validHits.add(searchHits.getHits()[0].getSourceAsString());
            //validHits.add("\n------------------------------------------------------\n");
        }
        for (String fileName: validHits.keySet()) {
            Allure.addAttachment(fileName, validHits.get(fileName).toString());
        }
        return validHits;
    }

    @Step("Execute Search Query. with {0}")
    private SearchHits executeQuery(QueryBuilder queryBuilders) throws UnknownHostException {
        return esUtil.executeSearchQuery(LocalConfig.ELASTIC_HOST, 0, 100, "kibana-pgp1", queryBuilders, LocalConfig.ELASTIC_INDEX);
    }

}
