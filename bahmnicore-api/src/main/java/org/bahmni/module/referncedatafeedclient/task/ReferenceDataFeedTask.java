package org.bahmni.module.referncedatafeedclient.task;

import org.bahmni.module.referncedatafeedclient.client.AtomFeedProcessor;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;

public class ReferenceDataFeedTask extends AbstractTask {

    @Override
    public void execute() {
        AtomFeedProcessor atomFeedProcessor = Context.getRegisteredComponent("referenceDataFeedProcessor", AtomFeedProcessor.class);
        atomFeedProcessor.processFeed();
    }
}
