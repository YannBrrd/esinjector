package com.yab.esipe;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class ESInjector {

    private static RestHighLevelClient client;

    private final static Logger logger = LoggerFactory.getLogger(ESInjector.class.getName());

    private ESInjector(String host, int port, String scheme) {
        client = connect(host, port, scheme);
    }

    private static RestHighLevelClient connect(String host, int port, String scheme) {

        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(host, port, scheme)
                ));
    }

    public static void main(String[] args) {
        ESInjector ei = new ESInjector("localhost", 9200, "http");

        ei.index("/Users/yann/Documents/projects/datagen/data.json", "logstash-bank", "customers");
        System.out.println("Indexing done");
        System.exit(1);
    }

    private boolean index(String fileName, String indexName, String typeName) {
        boolean res = false;
        ObjectMapper mapper = new ObjectMapper();

        File file = new File(fileName);

        if (file.exists()) {
            LineIterator it;
            try {
                it = FileUtils.lineIterator(file, "UTF-8");
                while (it.hasNext()) {
                    String line = it.nextLine();
                    JsonNode json = mapper.readTree(line);
                    IndexRequest request = new IndexRequest(indexName, typeName).source(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json), XContentType.JSON);
                    request.timeout(TimeValue.timeValueSeconds(2L));
                    IndexResponse indexResponse = client.index(request);
                    logger.trace(indexResponse.getId() + " " + indexResponse.status());
                }
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage());
            }
            res = true;
        }
        return res;
    }

    @Override
    protected void finalize() throws Throwable {
        client.close();
        super.finalize();
    }
}
