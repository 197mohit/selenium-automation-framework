package scripts.api.mappingService.merchantInfo

import com.paytm.base.test.TestSetUp
import com.paytm.utils.merchant.util.DbQueriesUtil
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import io.restassured.specification.ResponseSpecification
import org.testng.Assert
import org.testng.SkipException
import org.testng.annotations.Test

import static com.paytm.LocalConfig.PGP_HOST
import static com.paytm.appconstants.Constants.MappingService.GET_MERCHANT_AGENTINFO
import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

class GetAgentInfoTest extends TestSetUp {

    private static final String ID = 'id'
    private static final String TYPE = 'type'
    private static final String DB_COLUMN_CHILD_MID_PLACEHOLDER = 'child_mid'
    private static final String DB_COLUMN_PARENT_MID_PLACEHOLDER = 'parent_mid'
    private static final String DB_COLUMN_AGENT_ID_PLACEHOLDER = 'agent_id'

    private static final String XML_OPTIONS_RESPONSE = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<application xmlns="http://wadl.dev.java.net/2009/02">
    <doc xmlns:jersey="http://jersey.java.net/" jersey:generatedBy="Jersey: 2.22.1 2015-10-07 10:54:41"/>
    <grammars/>
    <resources base="http://pgp-automation.paytm.in/mapping-service/">
        <resource path="merchantAgent/get/agentInfo/{TYPE}/AGENT_ID">
            <method id="getAgentInfo" name="GET">
                <request>
                    <param xmlns:xs="http://www.w3.org/2001/XMLSchema" name="parentMid" style="query" type="xs:string"/>
                </request>
                <response>
                    <representation mediaType="application/json"/>
                </response>
            </method>
        </resource>
    </resources>
</application>
"""

    private enum Type {
        AGENTID("AGENT_ID"),
        CHILDMID("CHILD_MID"),
        PARENTMID("PARENT_MID")
        private final String type

        Type(String type) { this.type = type }

        @Override
        String toString() { return type }
    }

    private static final String FIND_MERCHANT_AGENT_INFO_BY_AGENTID = "SELECT id, parent_mid, child_mid, agent_id, status,extended_info FROM merchant_agent_mapping where agent_id  = ':agentId' and status = 1";

    private static final String FIND_MERCHANT_AGENT_INFO_BY_PARENTMID = "SELECT id, parent_mid, child_mid, agent_id, status,extended_info FROM merchant_agent_mapping  where parent_mid  = ':parentMid' and status = 1";

    private static final String FIND_MERCHANT_AGENT_INFO_BY_CHILDMID = "SELECT id, parent_mid, child_mid, agent_id, status,extended_info FROM merchant_agent_mapping where child_mid  = ':childMid' and status = 1";

    private static final String FIND_MERCHANT_INFO_WITH_STATUS_FALSE = "SELECT id, parent_mid, child_mid, agent_id, status, extended_info FROM merchant_agent_mapping WHERE status = 0;"

    private static final String FIND_MERCHANT_INFO_WITH_EXTENDED_INFO = "SELECT id, parent_mid, child_mid, agent_id, status, extended_info FROM merchant_agent_mapping WHERE NOT extended_info = 'null' "

    private static final String FIND_MERCHANT_INFO_WITHOUT_EXTENDED_INFO = "SELECT id, parent_mid, child_mid, agent_id, status, extended_info FROM merchant_agent_mapping WHERE extended_info = 'null' "

    private def req() {
        given(
                new RequestSpecBuilder()
                        .addRequestSpecification(reqSpec())
                        .setContentType(ContentType.JSON)
                        .addFilters([schemaFilter])
                        .setBaseUri(PGP_HOST)
                        .setBasePath(GET_MERCHANT_AGENTINFO)
                        .build()
        )
    }


    private final Filter schemaFilter = new Filter() {
        @Override
        Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            Response res = ctx.next(requestSpec, responseSpec)
            if (res.contentType == 'application/json') {
                responseSpec.spec(
                        new ResponseSpecBuilder()
                                .expectBody(matchesJsonSchemaInClasspath('json-schemas/mapping-service/merchant-agent-info-schema.json'))
                                .build()
                )
            }
            return res
        }
    }


    private static ResponseSpecification sucessResultInfo(String id, Type type) {
        List expectedRequestTypes = getExpectedAgentInfo(id, type)
        new ResponseSpecBuilder()
                .expectBody('id', hasItems(*(expectedRequestTypes.collect { it -> it.id as Integer }.asList())))
                .expectBody('parentMid', hasItems(*(expectedRequestTypes.collect { it -> it.parent_mid }.asList())))
                .expectBody('childMid', hasItems(*(expectedRequestTypes.collect { it -> it.child_mid }.asList())))
                .build()
    }


    private static Map<String, String> getDataFromDB() {
        DbQueriesUtil.selectFromPGPDB("SELECT id, parent_mid, child_mid, agent_id, status, extended_info FROM merchant_agent_mapping WHERE status = 0;").get(0)
    }

    private static def getExpectedAgentInfo(String id, Type agentType) {
        switch (agentType) {
            case Type.AGENTID:
                DbQueriesUtil.selectFromPGPDB(FIND_MERCHANT_AGENT_INFO_BY_AGENTID.replace(":agentId", id))
                break
            case Type.PARENTMID:
                DbQueriesUtil.selectFromPGPDB(FIND_MERCHANT_AGENT_INFO_BY_PARENTMID.replace(":parentMid", id))
                break
            case Type.CHILDMID:
                DbQueriesUtil.selectFromPGPDB(FIND_MERCHANT_AGENT_INFO_BY_CHILDMID.replace(":childMid", id))
                break
            default:
                throw new SkipException("agentType is Invalid");
        }
    }

    @Test
    void 'test with Parent type and Parent Mid to fetch Agent Info'() {
        Map<String, String> dataFromDB = getDataFromDB()
        String parentMid = dataFromDB.get(DB_COLUMN_PARENT_MID_PLACEHOLDER)
        req().pathParam(ID, parentMid)
                .pathParam(TYPE, Type.PARENTMID).get()
                .then()
                .spec(sucessResultInfo(parentMid, Type.PARENTMID))
    }


    @Test
    void 'test with Child type and child Mid to fetch Agent Info'() {
        Map<String, String> dataFromDB = getDataFromDB()
        String childMid = dataFromDB.get(DB_COLUMN_CHILD_MID_PLACEHOLDER)
        req().pathParam(ID, childMid)
                .pathParam(TYPE, Type.CHILDMID).get()
                .then()
                .spec(sucessResultInfo(childMid, Type.CHILDMID))
    }

    @Test
    void 'test with agent type and agentID to fetch Agent Info'() {
        Map<String, String> dataFromDB = getDataFromDB()
        String agentId = dataFromDB.get(DB_COLUMN_AGENT_ID_PLACEHOLDER)
        req().pathParam(ID, agentId)
                .pathParam(TYPE, Type.AGENTID).get()
                .then()
                .spec(sucessResultInfo(agentId, Type.AGENTID))
    }


    @Test
    void 'test with Parent type and agentID empty list should Return'() {
        Map<String, String> dataFromDB = getDataFromDB()
        String agentId = dataFromDB.get(DB_COLUMN_AGENT_ID_PLACEHOLDER)
        req().pathParam(ID, agentId)
                .pathParam(TYPE, Type.PARENTMID).get()
                .then()
                .body('', emptyIterable())
    }


    @Test
    void 'test with Child type and agentID empty list should Return'() {
        Map<String, String> dataFromDB = getDataFromDB()
        String agentId = dataFromDB.get(DB_COLUMN_AGENT_ID_PLACEHOLDER)
        req().pathParam(ID, agentId)
                .pathParam(TYPE, Type.CHILDMID).get()
                .then()
                .body('', emptyIterable())
    }


    @Test
    void 'test with Parent type and childMid empty list should Return'() {
        Map<String, String> dataFromDB = getDataFromDB()
        String childMid = dataFromDB.get(DB_COLUMN_CHILD_MID_PLACEHOLDER)
        req().pathParam(ID, childMid)
                .pathParam(TYPE, Type.PARENTMID).get()
                .then()
                .body('', emptyIterable())
    }


    @Test
    void 'test with agent type and childMid empty list should Return'() {
        Map<String, String> dataFromDB = getDataFromDB()
        String childMid = dataFromDB.get(DB_COLUMN_CHILD_MID_PLACEHOLDER)
        req().pathParam(ID, childMid)
                .pathParam(TYPE, Type.AGENTID).get()
                .then()
                .body('', emptyIterable())
    }

    @Test
    void 'test with agent type and ParentMid empty list should Return'() {
        Map<String, String> dataFromDB = getDataFromDB()
        String parentMid = dataFromDB.get(DB_COLUMN_PARENT_MID_PLACEHOLDER)
        req().pathParam(ID, parentMid)
                .pathParam(TYPE, Type.AGENTID).get()
                .then()
                .body('', emptyIterable())


    }

    @Test
    void 'test with child type and ParentMid empty list should Return'() {
        Map<String, String> dataFromDB = getDataFromDB()
        String parentMid = dataFromDB.get(DB_COLUMN_PARENT_MID_PLACEHOLDER)
        req().pathParam(ID, parentMid)
                .pathParam(TYPE, Type.CHILDMID).get()
                .then()
                .body('', emptyIterable())
    }

    @Test
    void 'test with child type and childMid when status is false the agent info should not get returned'() {
        Map<String, String> inactiveAgentInfo = DbQueriesUtil.selectFromPGPDB(FIND_MERCHANT_INFO_WITH_STATUS_FALSE).get(0)
        req().pathParam(ID, inactiveAgentInfo.get(DB_COLUMN_CHILD_MID_PLACEHOLDER))
                .pathParam(TYPE, Type.CHILDMID).get()
                .then()
                .body('', emptyIterable())
    }

    @Test
    void 'test with parent type and parentMid when status is false the agent info should not get returned'() {
        Map<String, String> inactiveAgentInfo = DbQueriesUtil.selectFromPGPDB(FIND_MERCHANT_INFO_WITH_STATUS_FALSE).get(0)
        req().pathParam(ID, inactiveAgentInfo.get(DB_COLUMN_PARENT_MID_PLACEHOLDER))
                .pathParam(TYPE, Type.PARENTMID).get()
                .then()
                .body('', emptyIterable())
    }

    @Test
    void 'test with agent type and agentId when status is false the agent info should not get returned'() {
        Map<String, String> inactiveAgentInfo = DbQueriesUtil.selectFromPGPDB(FIND_MERCHANT_INFO_WITH_STATUS_FALSE).get(0)
        req().pathParam(ID, inactiveAgentInfo.get(DB_COLUMN_AGENT_ID_PLACEHOLDER))
                .pathParam(TYPE, Type.AGENTID).get()
                .then()
                .body('', emptyIterable())
    }


    @Test
    void 'test with agent type and agentId when extendedInfo is null the agent info should returned'() {
        Map<String, String> extendedAgentInfoNull = DbQueriesUtil.selectFromPGPDB(FIND_MERCHANT_INFO_WITHOUT_EXTENDED_INFO).get(0)
        req().pathParam(ID, extendedAgentInfoNull.get(DB_COLUMN_AGENT_ID_PLACEHOLDER))
                .pathParam(TYPE, Type.AGENTID).get()
                .then()
                .body('extendedInfo', hasItem("NULL"));
    }


    @Test
    void 'test with child type and childMid when extendedIndfo is not null but JSON value the agent info should get returned'() {
        Map<String, String> extendedAgentInfo = DbQueriesUtil.selectFromPGPDB(FIND_MERCHANT_INFO_WITH_EXTENDED_INFO).get(0)
        req().pathParam(ID, extendedAgentInfo.get(DB_COLUMN_CHILD_MID_PLACEHOLDER))
                .pathParam(TYPE, Type.CHILDMID).get()
                .then()
                .body('extendedInfo', hasItem(extendedAgentInfo.get("extended_info")))
    }

    @Test
    void 'test with parent type and parentMid when extendedIndfo is not null the agent info should returned'() {
        Map<String, String> extendedAgentInfo = DbQueriesUtil.selectFromPGPDB(FIND_MERCHANT_INFO_WITH_EXTENDED_INFO).get(0)
        req().pathParam(ID, extendedAgentInfo.get(DB_COLUMN_PARENT_MID_PLACEHOLDER))
                .pathParam(TYPE, Type.PARENTMID).get()
                .then()
                .body('extendedInfo', hasItem(extendedAgentInfo.get("extended_info")))
    }

    @Test
    void 'test with agent type and agentId when extendedInfo is not null the agent info should returned'() {
        Map<String, String> extendedAgentInfo = DbQueriesUtil.selectFromPGPDB(FIND_MERCHANT_INFO_WITH_EXTENDED_INFO).get(0)
        req().pathParam(ID, extendedAgentInfo.get(DB_COLUMN_AGENT_ID_PLACEHOLDER))
                .pathParam(TYPE, Type.AGENTID).get()
                .then()
                .body('extendedInfo', hasItem(extendedAgentInfo.get("extended_info")))
    }

    @Test
    void 'test with options request type api should return all the details in xml'() {
        Map<String, String> dataFromDB = getDataFromDB()
        String parentMid = dataFromDB.get(DB_COLUMN_PARENT_MID_PLACEHOLDER)
        String resp = req().pathParam(ID, parentMid)
                .pathParam(TYPE, Type.AGENTID).options().then().extract().asString()
        Assert.assertEquals(resp, XML_OPTIONS_RESPONSE.replace("{TYPE}", parentMid))
    }
}


