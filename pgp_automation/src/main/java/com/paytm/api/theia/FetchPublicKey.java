package com.paytm.api.theia;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.theia.FetchPublicKeyRequestDTO;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import static com.paytm.LocalConfig.PGP_HOST;
import static com.paytm.appconstants.Constants.PGPAPIResourcePath.FETCH_PUBLIC_KEY;

/**
 * POST /theia/api/v1/fetchPublicKey — request body is {@link FetchPublicKeyRequestDTO}; ACCESS token from {@link com.paytm.CreateToken}.
 */
public class FetchPublicKey extends BaseApi {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final FetchPublicKeyRequestDTO requestDto;

    /**
     * Default: {@code mid} and {@code referenceId} in query and body.
     */
    public FetchPublicKey(String mid, String referenceId, String accessToken) {
        this(builder().mid(mid).referenceId(referenceId).accessToken(accessToken));
    }

    public static Builder builder() {
        return new Builder();
    }

    private FetchPublicKey(Builder b) {
        this.requestDto = b.buildRequestDto();
        setMethod(MethodType.POST);
        getRequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBaseUri(PGP_HOST)
                .setBasePath(FETCH_PUBLIC_KEY);
        if (b.includeQueryMid && b.mid != null) {
            getRequestSpecBuilder().addQueryParam("mid", b.mid);
        }
        if (b.includeQueryReferenceId && b.referenceId != null) {
            getRequestSpecBuilder().addQueryParam("referenceId", b.referenceId);
        }
        getRequestSpecBuilder().setBody(requestDto);
    }

    public FetchPublicKeyRequestDTO getRequestDto() {
        return requestDto;
    }

    public String getRequest() {
        try {
            return OBJECT_MAPPER.writeValueAsString(requestDto);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static final class Builder {
        private String mid;
        private String referenceId;
        private String accessToken = "";
        private boolean includeQueryMid = true;
        private boolean includeQueryReferenceId = true;
        private BodyMidStyle bodyMidStyle = BodyMidStyle.STRING;
        private boolean includeBodyReferenceId = true;

        private enum BodyMidStyle {
            STRING,
            JSON_NULL,
            OMIT
        }

        public Builder mid(String m) {
            this.mid = m;
            this.bodyMidStyle = BodyMidStyle.STRING;
            return this;
        }

        public Builder referenceId(String r) {
            this.referenceId = r;
            return this;
        }

        public Builder accessToken(String t) {
            this.accessToken = t != null ? t : "";
            return this;
        }

        public Builder omitQueryMid() {
            this.includeQueryMid = false;
            return this;
        }

        public Builder omitQueryReferenceId() {
            this.includeQueryReferenceId = false;
            return this;
        }

        /** JSON {@code "mid": null} in body (for validation tests). */
        public Builder bodyMidJsonNull() {
            this.bodyMidStyle = BodyMidStyle.JSON_NULL;
            return this;
        }

        /** Omit {@code mid} from body JSON. */
        public Builder omitBodyMid() {
            this.bodyMidStyle = BodyMidStyle.OMIT;
            return this;
        }

        public Builder omitBodyReferenceId() {
            this.includeBodyReferenceId = false;
            return this;
        }

        public FetchPublicKey build() {
            return new FetchPublicKey(this);
        }

        private FetchPublicKeyRequestDTO buildRequestDto() {
            FetchPublicKeyRequestDTO.Head head = new FetchPublicKeyRequestDTO.Head()
                    .setTokenType("ACCESS")
                    .setToken(accessToken);
            FetchPublicKeyRequestDTO.Body body = new FetchPublicKeyRequestDTO.Body()
                    .setMid(mid)
                    .setReferenceId(referenceId)
                    .setIncludeReferenceId(includeBodyReferenceId);
            switch (bodyMidStyle) {
                case JSON_NULL:
                    body.setMidBodyMode(FetchPublicKeyRequestDTO.Body.MidBodyMode.JSON_NULL);
                    break;
                case OMIT:
                    body.setMidBodyMode(FetchPublicKeyRequestDTO.Body.MidBodyMode.OMIT);
                    break;
                case STRING:
                default:
                    body.setMidBodyMode(FetchPublicKeyRequestDTO.Body.MidBodyMode.STRING);
                    break;
            }
            return new FetchPublicKeyRequestDTO().setHead(head).setBody(body);
        }
    }
}
