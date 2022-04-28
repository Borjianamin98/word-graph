package ir.ac.sbu.keyword.extractor.service;

import ir.ac.sbu.keyword.extractor.config.ApplicationConfigs;
import ir.ac.sbu.keyword.extractor.config.ApplicationConfigs.YakeServiceConfigs;
import ir.ac.sbu.keyword.extractor.model.YakeRequestDto;
import ir.ac.sbu.keyword.extractor.model.YakeSingleResponseDto;
import java.io.IOException;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Component
public class YakeService {

    private static final String UNEXPECTED_ERROR_MESSAGE = "Unexpected Error";
    private static final String UNEXPECTED_EMPTY_BODY_RESPONSE_ERROR_MESSAGE =
            "Unexpected response because the body can not be empty";

    private final YakeServiceConfigs yakeServiceConfigs;
    private final RestTemplate client;

    public YakeService(ApplicationConfigs applicationConfigs) {
        this.yakeServiceConfigs = applicationConfigs.getYakeServiceConfigs();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(yakeServiceConfigs.getClientConnectTimeoutMillis());
        factory.setReadTimeout(yakeServiceConfigs.getClientReadTimeoutMillis());
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(yakeServiceConfigs.getBaseUri()));
        this.client = restTemplate;
    }

    public List<YakeSingleResponseDto> getKeywords(String content) throws IOException {
        try {
            HttpEntity<YakeRequestDto> postEntity = new HttpEntity<>(new YakeRequestDto(
                    yakeServiceConfigs.getLanguage(),
                    yakeServiceConfigs.getMaxNgramSize(),
                    yakeServiceConfigs.getMaxNumberOfKeywords(),
                    content));

            List<YakeSingleResponseDto> response = client.exchange("/", HttpMethod.POST, postEntity,
                    new ParameterizedTypeReference<List<YakeSingleResponseDto>>() {
                    }).getBody();
            if (response == null) {
                throw new AssertionError(UNEXPECTED_EMPTY_BODY_RESPONSE_ERROR_MESSAGE);
            }

            return response;
        } catch (ResourceAccessException e) {
            throw new IOException(e);
        } catch (RestClientException e) {
            throw new AssertionError(UNEXPECTED_ERROR_MESSAGE, e);
        }
    }
}
