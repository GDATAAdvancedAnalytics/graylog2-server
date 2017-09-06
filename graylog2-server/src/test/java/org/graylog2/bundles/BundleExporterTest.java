package org.graylog2.bundles;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.graylog2.alerts.types.DummyAlertCondition;
import org.graylog2.dashboards.DashboardService;
import org.graylog2.dashboards.widgets.DashboardWidgetCreator;
import org.graylog2.grok.GrokPatternService;
import org.graylog2.inputs.InputService;
import org.graylog2.lookup.db.DBCacheService;
import org.graylog2.lookup.db.DBDataAdapterService;
import org.graylog2.lookup.db.DBLookupTableService;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.streams.OutputService;
import org.graylog2.streams.StreamImpl;
import org.graylog2.streams.StreamService;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TODO: Documentation
 */
public class BundleExporterTest {

    @Mock
    private InputService inputService;
    @Mock
    private StreamService streamService;
    @Mock
    private OutputService outputService;
    @Mock
    private DashboardService dashboardService;
    @Mock
    private DashboardWidgetCreator dashboardWidgetCreator;
    @Mock
    private DBLookupTableService dbLookupTableService;
    @Mock
    private DBCacheService dbCacheService;
    @Mock
    private DBDataAdapterService dbDataAdapterService;
    @Mock
    private GrokPatternService grokPatternService;
    @Mock
    private ObjectMapper objectMapper;


    @Test
    public void configurationBundleIncludesAlertConditions() throws Exception {
        final StreamService streamService = mock(StreamService.class);
        final org.graylog2.plugin.streams.Stream stream = mock(org.graylog2.plugin.streams.Stream.class);


        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("backlog", 5);
        parameterMap.put("repeat_notifications", false);
        parameterMap.put("grace", 1);
        parameterMap.put("threshold_type", "MORE");
        parameterMap.put("threshold", 50);
        parameterMap.put("time", 5);


        final String alertConditionTitle = "Best Alert Condition Ever";
        final AlertCondition dummyAlertCondition = new DummyAlertCondition(stream, "a", DateTime.now(), "matthi", parameterMap, alertConditionTitle);

        final ArrayList<AlertCondition> alertConditions = new ArrayList<>();
        alertConditions.add(dummyAlertCondition);

        when(streamService.getAlertConditions(stream)).thenReturn(alertConditions);
        when(streamService.load(stream.getId())).thenReturn(stream);

        ExportBundle exportBundle = new ExportBundle();
        final HashSet<String> streamIdsToBeExported = new HashSet<>();
        streamIdsToBeExported.add(stream.getId());
        exportBundle.setStreams(streamIdsToBeExported);

        final BundleExporter bundleExporter = new BundleExporter(inputService, streamService, outputService, dashboardService, dashboardWidgetCreator, dbLookupTableService, dbCacheService, dbDataAdapterService, grokPatternService, objectMapper);
        final ConfigurationBundle export = bundleExporter.export(exportBundle);

        final org.graylog2.bundles.AlertCondition exportedAlertCondition = export.getStreams().iterator().next().getAlertConditions().get(0);
        Assert.assertTrue(exportedAlertCondition.getTitle().equals(alertConditionTitle));
    }
}
