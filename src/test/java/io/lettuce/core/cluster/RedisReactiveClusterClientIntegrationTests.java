package io.lettuce.core.cluster;

import static io.lettuce.TestTags.INTEGRATION_TEST;
import static io.lettuce.core.cluster.ClusterTestUtil.getOwnPartition;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import reactor.test.StepVerifier;
import io.lettuce.core.TestSupport;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.reactive.RedisAdvancedClusterReactiveCommands;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.cluster.models.partitions.RedisClusterNode;
import io.lettuce.test.LettuceExtension;

/**
 * @author Mark Paluch
 */
@SuppressWarnings("unchecked")
@Tag(INTEGRATION_TEST)
@ExtendWith(LettuceExtension.class)
class RedisReactiveClusterClientIntegrationTests extends TestSupport {

    private final RedisAdvancedClusterCommands<String, String> sync;

    private final RedisAdvancedClusterReactiveCommands<String, String> reactive;

    @Inject
    RedisReactiveClusterClientIntegrationTests(StatefulRedisClusterConnection<String, String> connection) {
        this.sync = connection.sync();
        this.reactive = connection.reactive();
    }

    @Test
    void testClusterCommandRedirection() {

        // Command on node within the default connection
        StepVerifier.create(reactive.set(ClusterTestSettings.KEY_B, "myValue1")).expectNext("OK").verifyComplete();

        // gets redirection to node 3
        StepVerifier.create(reactive.set(ClusterTestSettings.KEY_A, "myValue1")).expectNext("OK").verifyComplete();
    }

    @Test
    void getKeysInSlot() {

        sync.flushall();

        sync.set(ClusterTestSettings.KEY_A, value);
        sync.set(ClusterTestSettings.KEY_B, value);

        StepVerifier.create(reactive.clusterGetKeysInSlot(ClusterTestSettings.SLOT_A, 10)).expectNext(ClusterTestSettings.KEY_A)
                .verifyComplete();
        StepVerifier.create(reactive.clusterGetKeysInSlot(ClusterTestSettings.SLOT_B, 10)).expectNext(ClusterTestSettings.KEY_B)
                .verifyComplete();
    }

    @Test
    void countKeysInSlot() {

        sync.flushall();

        sync.set(ClusterTestSettings.KEY_A, value);
        sync.set(ClusterTestSettings.KEY_B, value);

        StepVerifier.create(reactive.clusterCountKeysInSlot(ClusterTestSettings.SLOT_A)).expectNext(1L).verifyComplete();
        StepVerifier.create(reactive.clusterCountKeysInSlot(ClusterTestSettings.SLOT_B)).expectNext(1L).verifyComplete();

        int slotZZZ = SlotHash.getSlot("ZZZ".getBytes());
        StepVerifier.create(reactive.clusterCountKeysInSlot(slotZZZ)).expectNext(0L).verifyComplete();
    }

    @Test
    void testClusterCountFailureReports() {
        RedisClusterNode ownPartition = getOwnPartition(sync);
        StepVerifier.create(reactive.clusterCountFailureReports(ownPartition.getNodeId())).consumeNextWith(actual -> {
            assertThat(actual).isGreaterThanOrEqualTo(0);
        }).verifyComplete();
    }

    @Test
    void testClusterKeyslot() {
        StepVerifier.create(reactive.clusterKeyslot(ClusterTestSettings.KEY_A)).expectNext((long) ClusterTestSettings.SLOT_A)
                .verifyComplete();
        assertThat(SlotHash.getSlot(ClusterTestSettings.KEY_A)).isEqualTo(ClusterTestSettings.SLOT_A);
    }

    @Test
    void testClusterSaveconfig() {
        StepVerifier.create(reactive.clusterSaveconfig()).expectNext("OK").verifyComplete();
    }

    @Test
    void testClusterSetConfigEpoch() {
        StepVerifier.create(reactive.clusterSetConfigEpoch(1L)).consumeErrorWith(e -> {
            assertThat(e).hasMessageContaining("ERR The user can assign a config epoch only");
        }).verify();
    }

}
