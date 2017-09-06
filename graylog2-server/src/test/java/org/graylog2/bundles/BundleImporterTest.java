package org.graylog2.bundles;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.graylog2.alerts.AbstractAlertCondition;
import org.graylog2.dashboards.DashboardService;
import org.graylog2.dashboards.widgets.DashboardWidgetCreator;
import org.graylog2.events.ClusterEventBus;
import org.graylog2.grok.GrokPatternService;
import org.graylog2.indexer.IndexSetRegistry;
import org.graylog2.indexer.TestIndexSet;
import org.graylog2.indexer.indexset.IndexSetConfig;
import org.graylog2.inputs.InputService;
import org.graylog2.inputs.converters.ConverterFactory;
import org.graylog2.inputs.extractors.ExtractorFactory;
import org.graylog2.lookup.db.DBCacheService;
import org.graylog2.lookup.db.DBDataAdapterService;
import org.graylog2.lookup.db.DBLookupTableService;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.shared.inputs.InputLauncher;
import org.graylog2.shared.inputs.InputRegistry;
import org.graylog2.shared.inputs.MessageInputFactory;
import org.graylog2.streams.OutputService;
import org.graylog2.streams.StreamRuleService;
import org.graylog2.streams.StreamService;
import org.graylog2.timeranges.TimeRangeFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TODO: Documentation
 */
public class BundleImporterTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private InputService inputService;
    @Mock
    private InputRegistry inputRegistry;
    @Mock
    private ExtractorFactory extractorFactory;
    @Mock
    private ConverterFactory converterFactory;
    @Mock
    private StreamService streamService;
    @Mock
    private StreamRuleService streamRuleService;
    @Mock
    private OutputService outputService;
    @Mock
    private DashboardService dashboardService;
    @Mock
    private DashboardWidgetCreator dashboardWidgetCreator;
    @Mock
    private ServerStatus serverStatus;
    @Mock
    private MessageInputFactory messageInputFactory;
    @Mock
    private InputLauncher inputLauncher;
    @Mock
    private GrokPatternService grokPatternService;
    @Mock
    private DBLookupTableService dbLookupTableService;
    @Mock
    private DBCacheService dbCacheService;
    @Mock
    private DBDataAdapterService dbDataAdapterService;
    @Mock
    private TimeRangeFactory timeRangeFactory;
    @Mock
    private ClusterEventBus clusterBus;
    @Mock
    private ObjectMapper objectMapper;

    @Test
    public void createdStreamsContainAlertConditions() throws Exception {

        IndexSetRegistry indexSetRegistry = mock(IndexSetRegistry.class);
        IndexSetConfig indexSetConfig = mock(IndexSetConfig.class);
        when(indexSetConfig.id()).thenReturn("defaultIndexSetId");
        when(indexSetRegistry.getDefault()).thenReturn(new TestIndexSet(indexSetConfig));

        StreamService streamService = mock(StreamService.class);

        final List<org.graylog2.alerts.AbstractAlertCondition> createdAlertCondition = new ArrayList<>();
        doAnswer(invocationOnMock -> createdAlertCondition.add(invocationOnMock.getArgument(1))).when(streamService).updateAlertCondition(any(), any());

        BundleImporter bundleImporter = new BundleImporter(
                inputService,
                inputRegistry,
                extractorFactory,
                converterFactory,
                streamService,
                streamRuleService,
                indexSetRegistry,
                outputService,
                dashboardService,
                dashboardWidgetCreator,
                serverStatus,
                messageInputFactory,
                inputLauncher,
                grokPatternService,
                dbLookupTableService,
                dbCacheService,
                dbDataAdapterService,
                timeRangeFactory,
                clusterBus,
                objectMapper
        );

        // bundle to import
        final ConfigurationBundle bundle = new ConfigurationBundle();
        final Set<Stream> streams = new HashSet<>();
        final Stream stream = new Stream();
        final ArrayList<AlertCondition> alertConditions = new ArrayList<>();
        final AlertCondition alertCondition1 = new AlertCondition();

        alertCondition1.setCreatedAt(Date.from(Instant.now()));
        alertCondition1.setType("dummy");
        alertCondition1.setCreatorUserId("Someone");
        alertCondition1.setId("abc3");
        alertCondition1.setTitle("The Best Alert Condition Ever");

        final HashMap<String, Object> alertConditionParameterMap = new HashMap<>();
        alertConditionParameterMap.put("backlog", 5);
        alertConditionParameterMap.put("repeat_notifications", false);
        alertConditionParameterMap.put("grace", 1);
        alertConditionParameterMap.put("threshold_type", "MORE");
        alertConditionParameterMap.put("threshold", 50);
        alertConditionParameterMap.put("time", 5);
        alertCondition1.setParameters(alertConditionParameterMap);

        alertConditions.add(alertCondition1);
        final AlertCondition alertCondition2 = new AlertCondition();

        alertCondition2.setCreatedAt(Date.from(Instant.now()));
        alertCondition2.setType("dummy");
        alertCondition2.setCreatorUserId("Someone");
        alertCondition2.setId("abc3");
        alertCondition2.setTitle("The Best Alert Condition Ever");

        final HashMap<String, Object> alertConditionParameterMap2 = new HashMap<>();
        alertConditionParameterMap2.put("backlog", 5);
        alertConditionParameterMap2.put("repeat_notifications", false);
        alertConditionParameterMap2.put("grace", 1);
        alertConditionParameterMap2.put("threshold_type", "MORE");
        alertConditionParameterMap2.put("threshold", 50);
        alertConditionParameterMap2.put("time", 5);
        alertCondition2.setParameters(alertConditionParameterMap2);

        alertConditions.add(alertCondition2);
        stream.setAlertConditions(alertConditions);
        stream.setTitle("StreamTitle");
        stream.setDescription("StreamDescription");
        stream.setDisabled(false);
        stream.setDefaultStream(false);
        stream.setId(ObjectId.get().toHexString());

        streams.add(stream);
        bundle.setStreams(streams);
        bundle.setId("bundleId");
        bundle.setCategory("bundleCategory");
        bundle.setName("bundleName");
        bundle.setDescription("A test bundle.");

        // import
        bundleImporter.runImport(bundle, "Username");

        // ???
        org.graylog2.plugin.alarms.AlertCondition abstractAlertCondition1 = createdAlertCondition.get(0);
        Assert.assertNotNull(abstractAlertCondition1);
        Assert.assertEquals(alertCondition1.getTitle(), abstractAlertCondition1.getTitle());
        Assert.assertEquals(alertCondition1.getId(), abstractAlertCondition1.getId());
        Assert.assertEquals(alertCondition1.getCreatedAt(), new Date(abstractAlertCondition1.getCreatedAt().getMillis()));
        Assert.assertEquals(alertCondition1.getCreatorUserId(), abstractAlertCondition1.getCreatorUserId());
        Assert.assertEquals(alertCondition1.getType(), abstractAlertCondition1.getType());
        Assert.assertEquals(alertCondition1.getParameters(), abstractAlertCondition1.getParameters());

        org.graylog2.plugin.alarms.AlertCondition abstractAlertCondition2 = createdAlertCondition.get(1);
        Assert.assertNotNull(abstractAlertCondition2);
        Assert.assertEquals(alertCondition2.getTitle(), abstractAlertCondition2.getTitle());
        Assert.assertEquals(alertCondition2.getId(), abstractAlertCondition2.getId());
        Assert.assertEquals(alertCondition2.getCreatedAt(), new Date(abstractAlertCondition2.getCreatedAt().getMillis()));
        Assert.assertEquals(alertCondition2.getCreatorUserId(), abstractAlertCondition2.getCreatorUserId());
        Assert.assertEquals(alertCondition2.getType(), abstractAlertCondition2.getType());
        Assert.assertEquals(alertCondition2.getParameters(), abstractAlertCondition2.getParameters());

        // profit
    }
}
