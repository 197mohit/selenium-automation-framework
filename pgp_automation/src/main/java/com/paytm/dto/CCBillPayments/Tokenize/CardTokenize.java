package com.paytm.dto.CCBillPayments.Tokenize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.paytm.base.test.User;
import com.paytm.dto.CCBillPayments.FetchBin.Head;
import io.restassured.path.json.JsonPath;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardTokenize {
        Head HeadObject;
        Body BodyObject;

        public Head getHead() {
            return HeadObject;
        }

        public Body getBody() {
            return BodyObject;
        }

        public void setHead(Head headObject) {
            this.HeadObject = headObject;
        }

        public void setBody(Body bodyObject) {
            this.BodyObject = bodyObject;
        }

      public enum TokenizeType
      {
            ccNumber,
            savedCardId,
            creditCardId;
            public static String TokenizeTypeString(TokenizeType Type){
                return Type.toString();
            }
        }
    }


