package fi.vm.yti.datamodel.api.service;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import fi.vm.yti.datamodel.api.config.ApplicationProperties;
import static org.junit.Assert.fail;

/**
 * @author amiika
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationProperties.class, ModelManager.class })
@ActiveProfiles("local")
public class FrameManagerTest {

    private static final Logger logger = LoggerFactory.getLogger(FrameManagerTest.class.getName());
    @Autowired
    private ApplicationProperties config;
    @Autowired
    private ModelManager modelManager;

    @Ignore
    @Test
    public void testGetCachedClassVisualizationFrame() {
        logger.info("cacheClassVisualizationFrame");
        String id = "http://ex.com/id:2";
        Resource dataFile = new ClassPathResource("test-model.ttl");

        try {
            Model model = ModelFactory.createDefaultModel();
            model.read(dataFile.getFile().getAbsolutePath());
            logger.debug(dataFile.getFile().getAbsolutePath());

            //  verify(client).indices().create(new CreateIndexRequest(FrameManager.ELASTIC_INDEX_MODEL), RequestOptions.DEFAULT);
            //   when(client.prepareIndex(any(), any(), any())).thenReturn(irb);
            //   when(irb.execute()).thenReturn(mock(ActionFuture.class));

            String jsonLDString = modelManager.writeModelToJSONLDString(model);

            //FIXME: @ActiveProfiles("local") Not working!?!?!?
            logger.debug(config.getElasticHttpPort());
            logger.debug(config.getElasticHost());

            //    RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
            //			new HttpHost(config.getElasticHost(), Integer.parseInt(config.getElasticHttpPort()),"http")
            //  		new HttpHost("127.0.0.1", 9200, "http")
            //  		));

            // FrameManager instance = new FrameManager(client, null);

            // instance.initCache();

            logger.debug("Cache init success");

            //instance.cacheClassVisualizationFrame(id, jsonLDString);

            // String frameJson = instance.getCachedClassVisualizationFrame(id, null);

            //  verify(client).prepareIndex(FrameManager.ELASTIC_INDEX_MODEL, "doc", "http%3A%2F%2Fex.com%2Fid%3A2");
            //   ArgumentCaptor<String> sourceCaptor = ArgumentCaptor.forClass(String.class);
            //  verify(irb).setSource(sourceCaptor.capture());
            // String frameJson = sourceCaptor.getValue();

            //  assertThat(frameJson, isJson());

            // FIXME: Not working ....
            //assertThat(frameJson, hasJsonPath("$['@graph'][0]['@id']", equalTo("testaa:Test")));
            //assertThat(frameJson, hasJsonPath("$['@graph'][0]['property']['predicate']", equalTo("testaa:test-property")));

            // assertEquals(instance.removeCachedResource(id).getResult(),DocWriteResponse.Result.DELETED);

        } catch (Exception ex) {
            logger.warn("Error: ", ex);
            fail("Exception was thrown:" + ex.getMessage());
        }

    }

}
