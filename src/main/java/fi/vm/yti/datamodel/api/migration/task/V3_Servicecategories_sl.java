package fi.vm.yti.datamodel.api.migration.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.vm.yti.datamodel.api.service.GraphManager;
import fi.vm.yti.migration.MigrationTask;

@Component
public class V3_Servicecategories_sl implements MigrationTask {

    private static final Logger logger = LoggerFactory.getLogger(V3_Servicecategories_sl.class.getName());
    private final GraphManager graphManager;

    @Autowired
    V3_Servicecategories_sl(GraphManager graphManager) {
        this.graphManager = graphManager;
    }

    @Override
    public void migrate() {
        logger.debug("Service categories update");
        graphManager.initServiceCategories();

        if (!graphManager.testDefaultGraph()) {
            throw new RuntimeException("Failed to create default graph");
        }
    }
}
