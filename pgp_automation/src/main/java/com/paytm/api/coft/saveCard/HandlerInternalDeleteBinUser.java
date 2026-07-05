package com.paytm.api.coft.saveCard;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.VAULTIDENTIFIER;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.User;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import java.util.Arrays;
import org.assertj.core.api.SoftAssertions;

public class HandlerInternalDeleteBinUser extends BaseApi {

  public HandlerInternalDeleteBinUser(String ssoToken,String saveCardId ) {
    setMethod(MethodType.POST);
    getRequestSpecBuilder().setContentType(ContentType.JSON);
    getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
    getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
    getRequestSpecBuilder().setBasePath(Constants.savedCard.SAVEDCARD_SERVICE_HANDLER_INTERNAL_DELETE_BIN);
    getRequestSpecBuilder().addQueryParam("JsonData", "{\"SSOToken\":\""+ssoToken+"\",\"SavedCardId\":\""+saveCardId+"\"}");
  }

  public static void deleteAllCards(String ssoToken)
  {
    HanlerInternalBinInfo binInfo = new HanlerInternalBinInfo(ssoToken,true);
    JsonPath binInfoResponse = binInfo.execute().jsonPath();
    int size = Integer.parseInt(binInfoResponse.getString("SIZE"));
    System.out.println("Size is "+ size);
    while (size !=0)
    {
      String savedCardId= binInfoResponse.getString("BIN_DETAILS[0].SAVE_CARD_ID");
      HandlerInternalDeleteBinUser deleteBinUser =new HandlerInternalDeleteBinUser(ssoToken,savedCardId);
      JsonPath delBinResp = deleteBinUser.execute().jsonPath();
      int noOfCardsDeleted = Integer.parseInt(delBinResp.getString("NUMBER_OF_RECORDS"));
      SoftAssertions softly = new SoftAssertions();
      softly.assertThat(noOfCardsDeleted).isNotEqualTo(0);
      System.out.println("No of cards deleted "+noOfCardsDeleted);
      System.out.println(savedCardId+ " Deleted");
      binInfoResponse = binInfo.execute().jsonPath();
      size = size -1;
//            size = Integer.parseInt(binInfoResponse.getString("SIZE"));
    }
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(size).isEqualTo(0);
    softly.assertAll();

  }

/*  public static void deleteAllCards(User user)
  {
    //getting OCL tokens size
    HanlerInternalBinInfo binInfoOcl = new HanlerInternalBinInfo(user.ssoToken(),true,
        VAULTIDENTIFIER.OCL.get());
    JsonPath binInfoResponseOCL = binInfoOcl.execute().jsonPath();
    int oclSize=Integer.parseInt(binInfoResponseOCL.getString("SIZE"));
    System.out.println("OCL Size is "+ oclSize);
    //getting PPBL tokens size
    HanlerInternalBinInfo binInfoPpbl = new HanlerInternalBinInfo(user.ssoToken(),true,
        VAULTIDENTIFIER.PPBL.get());
    JsonPath binInfoResponsePPBL = binInfoPpbl.execute().jsonPath();
    int ppblSize=Integer.parseInt(binInfoResponsePPBL.getString("SIZE"));
    System.out.println("PPBL Size is "+ ppblSize);

    SoftAssertions softly = new SoftAssertions();

    //deleting OCL cards
    if(oclSize!=0)
    {
      while (oclSize !=0)
      {
        System.out.println("OCL Size is "+ oclSize);
        String cardToBeDeleted= binInfoResponseOCL.getString("BIN_DETAILS[0].SAVE_CARD_ID");
        HandlerInternalDeleteBinUser deleteBinUser =new HandlerInternalDeleteBinUser(user.ssoToken(), cardToBeDeleted);
        JsonPath delBinResp = deleteBinUser.execute().jsonPath();
        int noOfCardsDeleted = Integer.parseInt(delBinResp.getString("NUMBER_OF_RECORDS"));

        softly = new SoftAssertions();

        System.out.println("No of cards deleted "+noOfCardsDeleted);
        if (oclSize > 0 && noOfCardsDeleted == 0){
          DeleteCardInCardCenter deleteCardInCardCenter = new DeleteCardInCardCenter(user, cardToBeDeleted);
          JsonPath deletedCardResponse = deleteCardInCardCenter.execute().jsonPath();
          String actual = deletedCardResponse.getString("message");
          String[] expected = {
              "SUCCESS",
              "card not exist"};
          softly.assertThat(Arrays.asList(expected).contains(actual));
          softly.assertAll();
          System.out.println(cardToBeDeleted+ " Deleted in card center");
        }

        binInfoResponseOCL = binInfoOcl.execute().jsonPath();
        oclSize = Integer.parseInt(binInfoResponseOCL.getString("SIZE"));
        String saveCardId= binInfoResponseOCL.getString("BIN_DETAILS[0].SAVE_CARD_ID");
        System.out.println("Save card ID is "+ saveCardId);

        if (oclSize!=0 && noOfCardsDeleted!=0 && saveCardId.equalsIgnoreCase(cardToBeDeleted))
        {
          DeleteCardInCardCenter deleteCardInCardCenter = new DeleteCardInCardCenter(user, cardToBeDeleted);
          JsonPath deletedCardResponse = deleteCardInCardCenter.execute().jsonPath();
          String actual = deletedCardResponse.getString("message");
          String[] expected = {
              "SUCCESS",
              "card not exist"};
          softly.assertThat(Arrays.asList(expected).contains(actual));
          softly.assertAll();
          System.out.println(cardToBeDeleted+ " Deleted in card center");
        }

        System.out.println(cardToBeDeleted+ " Deleted");
      }
      binInfoResponseOCL = binInfoOcl.execute().jsonPath();
      oclSize = Integer.parseInt(binInfoResponseOCL.getString("SIZE"));
      softly = new SoftAssertions();
      softly.assertThat(oclSize).isEqualTo(0);
      softly.assertAll();
      System.out.println("OCL Size after deletion is "+oclSize);
    }
    //Deleting PPBL cards
    if(ppblSize!=0)
    {
      System.out.println("Deleting PPBL cards");
      while ( ppblSize!=0)
      {   MerchantType ppblVaultMerchant= MerchantType.PPBL_VAULT_MID;
        String cardToBeDeleted= binInfoResponsePPBL.getString("BIN_DETAILS[0].SAVE_CARD_ID");
        HandlerInternalDeleteBinUser deleteBinUser =new HandlerInternalDeleteBinUser(user.ssoToken(), cardToBeDeleted);
        JsonPath delBinResp = deleteBinUser.execute().jsonPath();
        int noOfCardsDeleted = Integer.parseInt(delBinResp.getString("NUMBER_OF_RECORDS"));

        System.out.println("No of cards deleted "+noOfCardsDeleted);
        if (ppblSize > 0 && noOfCardsDeleted == 0){
          DeleteCardInCardCenter deleteCardInCardCenter = new DeleteCardInCardCenter(user, cardToBeDeleted,ppblVaultMerchant.getId());
          JsonPath deletedCardResponse = deleteCardInCardCenter.execute().jsonPath();
          String actual = deletedCardResponse.getString("message");
          String[] expected = {
              "SUCCESS",
              "card not exist"};
          softly.assertThat(Arrays.asList(expected).contains(actual));
          softly.assertAll();
          System.out.println(cardToBeDeleted+ " Deleted in card center");
        }

        binInfoResponsePPBL = binInfoPpbl.execute().jsonPath();
        ppblSize = Integer.parseInt(binInfoResponsePPBL.getString("SIZE"));
        String saveCardId= binInfoResponsePPBL.getString("BIN_DETAILS[0].SAVE_CARD_ID");
        System.out.println("Save card ID is "+ saveCardId);

        if (ppblSize!=0 && noOfCardsDeleted!=0 && saveCardId.equalsIgnoreCase(cardToBeDeleted))
        {
          DeleteCardInCardCenter deleteCardInCardCenter = new DeleteCardInCardCenter(user, cardToBeDeleted,ppblVaultMerchant.getId());
          JsonPath deletedCardResponse = deleteCardInCardCenter.execute().jsonPath();
          String actual = deletedCardResponse.getString("message");
          String[] expected = {
              "SUCCESS",
              "card not exist"};
          softly.assertThat(Arrays.asList(expected).contains(actual));
          softly.assertAll();
          System.out.println(cardToBeDeleted+ " Deleted in card center");
        }

        System.out.println(cardToBeDeleted+ " Deleted");
      }
      binInfoResponsePPBL = binInfoPpbl.execute().jsonPath();
      ppblSize = Integer.parseInt(binInfoResponsePPBL.getString("SIZE"));
      softly = new SoftAssertions();
      softly.assertThat(ppblSize).isEqualTo(0);
      softly.assertAll();
      System.out.println("PPBL Size after deletion is "+ ppblSize);
    }
  } */

  public static void deleteAllCards(User user) {
    // getting OCL tokens size
    HanlerInternalBinInfo binInfoOcl = new HanlerInternalBinInfo(user.ssoToken(), true, VAULTIDENTIFIER.OCL.get());
    JsonPath binInfoResponseOCL = binInfoOcl.execute().jsonPath();
    int oclSize = Integer.parseInt(binInfoResponseOCL.getString("SIZE"));
    System.out.println("OCL Size is " + oclSize);
    // getting PPBL tokens size
    HanlerInternalBinInfo binInfoPpbl = new HanlerInternalBinInfo(user.ssoToken(), true, VAULTIDENTIFIER.PPBL.get());
    JsonPath binInfoResponsePPBL = binInfoPpbl.execute().jsonPath();
    int ppblSize = Integer.parseInt(binInfoResponsePPBL.getString("SIZE"));
    System.out.println("PPBL Size is " + ppblSize);

    SoftAssertions softly = new SoftAssertions();

    // deleting OCL cards
    if (oclSize != 0) {
      while (oclSize != 0) {
        System.out.println("OCL Size is " + oclSize);
        String cardToBeDeleted = binInfoResponseOCL.getString("BIN_DETAILS[0].SAVE_CARD_ID");
        HandlerInternalDeleteBinUser deleteBinUser = new HandlerInternalDeleteBinUser(user.ssoToken(), cardToBeDeleted);
        JsonPath delBinResp = deleteBinUser.execute().jsonPath();
        int noOfCardsDeleted = Integer.parseInt(delBinResp.getString("NUMBER_OF_RECORDS"));

        softly = new SoftAssertions();

        System.out.println("No of cards deleted " + noOfCardsDeleted);
        if (oclSize > 0 && noOfCardsDeleted == 0) {
          DeleteCardInCardCenter deleteCardInCardCenter = new DeleteCardInCardCenter(user, cardToBeDeleted);
          JsonPath deletedCardResponse = deleteCardInCardCenter.execute().jsonPath();
          String actual = deletedCardResponse.getString("message");
          String[] expected = {"SUCCESS", "card not exist"};
          softly.assertThat(Arrays.asList(expected).contains(actual));
          softly.assertAll();
          System.out.println(cardToBeDeleted + " Deleted in card center");

          if ("Access Denied : Client Id cannot be null or empty in header.".equals(actual)) {
            break; // exit if access is denied
          }
        }

        binInfoResponseOCL = binInfoOcl.execute().jsonPath();
        int newOclSize = Integer.parseInt(binInfoResponseOCL.getString("SIZE"));
        if (newOclSize == oclSize) {
          break; // exit if size does not decrease
        }
        oclSize = newOclSize;
        String saveCardId = binInfoResponseOCL.getString("BIN_DETAILS[0].SAVE_CARD_ID");
        System.out.println("Save card ID is " + saveCardId);

        if (oclSize != 0 && noOfCardsDeleted != 0 && saveCardId.equalsIgnoreCase(cardToBeDeleted)) {
          DeleteCardInCardCenter deleteCardInCardCenter = new DeleteCardInCardCenter(user, cardToBeDeleted);
          JsonPath deletedCardResponse = deleteCardInCardCenter.execute().jsonPath();
          String actual = deletedCardResponse.getString("message");
          String[] expected = {"SUCCESS", "card not exist"};
          softly.assertThat(Arrays.asList(expected).contains(actual));
          softly.assertAll();
          System.out.println(cardToBeDeleted + " Deleted in card center");

          if ("Access Denied : Client Id cannot be null or empty in header.".equals(actual)) {
            break; // exit if access is denied
          }
        }

        System.out.println(cardToBeDeleted + " Deleted");
      }
      binInfoResponseOCL = binInfoOcl.execute().jsonPath();
      oclSize = Integer.parseInt(binInfoResponseOCL.getString("SIZE"));
      softly = new SoftAssertions();
      softly.assertThat(oclSize).isEqualTo(0);
      softly.assertAll();
      System.out.println("OCL Size after deletion is " + oclSize);
    }
    // Deleting PPBL cards
    if (ppblSize != 0) {
      System.out.println("Deleting PPBL cards");
      while (ppblSize != 0) {
        MerchantType ppblVaultMerchant = MerchantType.PPBL_VAULT_MID;
        String cardToBeDeleted = binInfoResponsePPBL.getString("BIN_DETAILS[0].SAVE_CARD_ID");
        HandlerInternalDeleteBinUser deleteBinUser = new HandlerInternalDeleteBinUser(user.ssoToken(), cardToBeDeleted);
        JsonPath delBinResp = deleteBinUser.execute().jsonPath();
        int noOfCardsDeleted = Integer.parseInt(delBinResp.getString("NUMBER_OF_RECORDS"));

        System.out.println("No of cards deleted " + noOfCardsDeleted);
        if (ppblSize > 0 && noOfCardsDeleted == 0) {
          DeleteCardInCardCenter deleteCardInCardCenter = new DeleteCardInCardCenter(user, cardToBeDeleted, ppblVaultMerchant.getId());
          JsonPath deletedCardResponse = deleteCardInCardCenter.execute().jsonPath();
          String actual = deletedCardResponse.getString("message");
          String[] expected = {"SUCCESS", "card not exist"};
          softly.assertThat(Arrays.asList(expected).contains(actual));
          softly.assertAll();
          System.out.println(cardToBeDeleted + " Deleted in card center");

          if ("Access Denied : Client Id cannot be null or empty in header.".equals(actual)) {
            break; // exit if access is denied
          }
        }

        binInfoResponsePPBL = binInfoPpbl.execute().jsonPath();
        int newPpblSize = Integer.parseInt(binInfoResponsePPBL.getString("SIZE"));
        if (newPpblSize == ppblSize) {
          break; // exit if size does not decrease
        }
        ppblSize = newPpblSize;
        String saveCardId = binInfoResponsePPBL.getString("BIN_DETAILS[0].SAVE_CARD_ID");
        System.out.println("Save card ID is " + saveCardId);

        if (ppblSize != 0 && noOfCardsDeleted != 0 && saveCardId.equalsIgnoreCase(cardToBeDeleted)) {
          DeleteCardInCardCenter deleteCardInCardCenter = new DeleteCardInCardCenter(user, cardToBeDeleted, ppblVaultMerchant.getId());
          JsonPath deletedCardResponse = deleteCardInCardCenter.execute().jsonPath();
          String actual = deletedCardResponse.getString("message");
          String[] expected = {"SUCCESS", "card not exist"};
          softly.assertThat(Arrays.asList(expected).contains(actual));
          softly.assertAll();
          System.out.println(cardToBeDeleted + " Deleted in card center");

          if ("Access Denied : Client Id cannot be null or empty in header.".equals(actual)) {
            break; // exit if access is denied
          }
        }

        System.out.println(cardToBeDeleted + " Deleted");
      }
      binInfoResponsePPBL = binInfoPpbl.execute().jsonPath();
      ppblSize = Integer.parseInt(binInfoResponsePPBL.getString("SIZE"));
      softly = new SoftAssertions();
      softly.assertThat(ppblSize).isEqualTo(0);
      softly.assertAll();
      System.out.println("PPBL Size after deletion is " + ppblSize);
    }
  }
}
