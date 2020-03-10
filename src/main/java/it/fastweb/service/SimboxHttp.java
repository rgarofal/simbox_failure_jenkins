package it.fastweb.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimboxHttp {

	private static boolean config_test = true;
    private static final Logger log = LoggerFactory.getLogger(SimboxHttp.class);
    private static final String TMT_BASE_URL = "https://tmt.fastweb.it/api/ticketSimboxFailure";
    //https://tmt.fastweb.it/api/ticketSimboxFailure?timestamp=1559779200&auth=2ba3b7d0f9381da4187c304483af4e88
    //https://webapp-test.fastweb.it/tmt/api/ticketSimboxFailure?timestamp=1559779200&auth=2ba3b7d0f9381da4187c304483af4e88
    private static final String TMT_BASE_URL_TEST = "https://webapp-test.fastweb.it/tmt/api/ticketSimboxFailure";
    private static final String TMT_SECRET_KEY = "P181080-TMT-Tool";

    
    public boolean sendTicket(String fileTmt) throws Exception {

        //SSLContext sslContext = SSLContextBuilder.create().useProtocol("TLSv1.2").build();

        try (CloseableHttpClient httpclient = HttpClients.custom().useSystemProperties().build()) {

            String auth = "";

            Date date = Calendar.getInstance().getTime();
            DateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String timestamp = sdf.format(date);
            auth = DigestUtils.md5Hex(timestamp + TMT_SECRET_KEY);
            String url = null;
            if (config_test) {
            	log.info("Session HTTP TEST");
            	url = TMT_BASE_URL_TEST + "?timestamp=" + timestamp + "&auth=" + auth;
            } else {
            	log.info("Session HTTP Produzione");
            	url = TMT_BASE_URL + "?timestamp=" + timestamp + "&auth=" + auth;
            }
            

           
            HttpHost proxy = new HttpHost("ap028rco", 808, "https");
            RequestConfig config = RequestConfig.custom()
                    .setProxy(proxy)
                    .build();

            HttpUriRequest request = RequestBuilder
                    .post(url)
                    .setEntity(new StringEntity(fileTmt, ContentType.TEXT_PLAIN))
                    .build();

            System.setProperty("https.protocols", "TLSv1.2");

            log.info("Executing request " + request.getRequestLine());

            final boolean[] statoResponse = {false};

            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    statoResponse[0] = true;
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    statoResponse[0] = false;
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
            httpclient.execute(request, responseHandler);

            return statoResponse[0];
        }
    }
}
