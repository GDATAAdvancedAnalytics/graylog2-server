package org.graylog2.bundles;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.graylog2.streams.StreamService;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TODO: Documentation
 */
public class BundleExporterTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private InputService inputService;
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
    public void configurationBundleIncludesAlertCondition() throws Exception {
        final StreamService streamService = mock(StreamService.class);
        final org.graylog2.plugin.streams.Stream stream = mock(org.graylog2.plugin.streams.Stream.class);


        Map<String, Object> alertConditionParameterMap = new HashMap<>();
        alertConditionParameterMap.put("backlog", 5);
        alertConditionParameterMap.put("repeat_notifications", false);
        alertConditionParameterMap.put("grace", 1);
        alertConditionParameterMap.put("threshold_type", "MORE");
        alertConditionParameterMap.put("threshold", 50);
        alertConditionParameterMap.put("time", 5);


        final String alertConditionTitle = "Best Alert Condition Ever";
        final AlertCondition dummyAlertCondition = new DummyAlertCondition(stream, "Some Id", DateTime.now(), "Somebody", alertConditionParameterMap, alertConditionTitle);

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
        Assert.assertEquals(exportedAlertCondition.getTitle(), alertConditionTitle);
        Assert.assertEquals(exportedAlertCondition.getType(), dummyAlertCondition.getType());
        Assert.assertEquals(exportedAlertCondition.getCreatorUserId(), dummyAlertCondition.getCreatorUserId());
        Assert.assertEquals(exportedAlertCondition.getId(), dummyAlertCondition.getId());
        Assert.assertEquals(exportedAlertCondition.getCreatedAt(), new Date(dummyAlertCondition.getCreatedAt().getMillis()));
        Assert.assertEquals(exportedAlertCondition.getParameters().get("backlog"), 5);
        Assert.assertEquals(exportedAlertCondition.getParameters().get("repeat_notifications"), false);
        Assert.assertEquals(exportedAlertCondition.getParameters().get("grace"), 1);
        Assert.assertEquals(exportedAlertCondition.getParameters().get("threshold_type"), "MORE");
        Assert.assertEquals(exportedAlertCondition.getParameters().get("threshold"), 50);
        Assert.assertEquals(exportedAlertCondition.getParameters().get("time"), 5);
    }
}
