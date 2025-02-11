package io.lettuce.core.cluster;

import static io.lettuce.TestTags.INTEGRATION_TEST;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import reactor.test.StepVerifier;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.reactive.RedisClusterReactiveCommands;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import io.lettuce.core.cluster.models.partitions.RedisClusterNode;
import io.lettuce.core.cluster.models.slots.ClusterSlotRange;
import io.lettuce.core.cluster.models.slots.ClusterSlotsParser;
import io.lettuce.test.LettuceExtension;

/**
 * @author Mark Paluch
 */
@Tag(INTEGRATION_TEST)
@ExtendWith(LettuceExtension.class)
class ClusterReactiveCommandIntegrationTests {

    private final RedisClusterClient clusterClient;

    private final RedisClusterReactiveCommands<String, String> reactive;

    private final RedisClusterCommands<String, String> sync;

    @Inject
    ClusterReactiveCommandIntegrationTests(RedisClusterClient clusterClient,
            StatefulRedisClusterConnection<String, String> connection) {
        this.clusterClient = clusterClient;

        this.reactive = connection.reactive();
        this.sync = connection.sync();
    }

    @Test
    void testClusterBumpEpoch() {
        StepVerifier.create(reactive.clusterBumpepoch())
                .consumeNextWith(actual -> assertThat(actual).matches("(BUMPED|STILL).*")).verifyComplete();
    }

    @Test
    void testClusterInfo() {

        StepVerifier.create(reactive.clusterInfo()).consumeNextWith(actual -> {
            assertThat(actual).contains("cluster_known_nodes:");
            assertThat(actual).contains("cluster_slots_fail:0");
            assertThat(actual).contains("cluster_state:");
        }).verifyComplete();
    }

    @Test
    void testClusterNodes() {

        StepVerifier.create(reactive.clusterNodes()).consumeNextWith(actual -> {
            assertThat(actual).contains("connected");
            assertThat(actual).contains("master");
            assertThat(actual).contains("myself");
        }).verifyComplete();
    }

    @Test
    void testAsking() {
        StepVerifier.create(reactive.asking()).expectNext("OK").verifyComplete();
    }

    @Test
    void testClusterSlots() {

        List<Object> reply = reactive.clusterSlots().collectList().block();
        assertThat(reply.size()).isGreaterThan(1);

        List<ClusterSlotRange> parse = ClusterSlotsParser.parse(reply);
        assertThat(parse).hasSize(2);

        ClusterSlotRange clusterSlotRange = parse.get(0);
        assertThat(clusterSlotRange.getFrom()).isEqualTo(0);
        assertThat(clusterSlotRange.getTo()).isEqualTo(11999);

        assertThat(clusterSlotRange.toString()).contains(ClusterSlotRange.class.getSimpleName());
    }

    @Test
    void clusterSlaves() {

        RedisClusterNode master = clusterClient.getPartitions().stream().filter(it -> it.is(RedisClusterNode.NodeFlag.UPSTREAM))
                .findFirst().get();

        List<String> result = reactive.clusterSlaves(master.getNodeId()).collectList().block();

        assertThat(result.size()).isGreaterThan(0);
    }

    @Test
    void testClusterLinks() {
        List<Map<String, Object>> values = reactive.clusterLinks().block();
        for (Map<String, Object> value : values) {
            assertThat(value).containsKeys("direction", "node", "create-time", "events", "send-buffer-allocated",
                    "send-buffer-used");
        }
    }

}
