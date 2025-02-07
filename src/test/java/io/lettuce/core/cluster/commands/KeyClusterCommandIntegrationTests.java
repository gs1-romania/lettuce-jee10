package io.lettuce.core.cluster.commands;

import static io.lettuce.TestTags.INTEGRATION_TEST;
import static org.assertj.core.api.Assertions.*;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import io.lettuce.core.TestSupport;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.ClusterTestUtil;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.test.LettuceExtension;
import io.lettuce.test.condition.EnabledOnCommand;

/**
 * Integration tests for {@link io.lettuce.core.api.sync.RedisKeyCommands} using Redis Cluster.
 *
 * @author Mark Paluch
 */
@Tag(INTEGRATION_TEST)
@ExtendWith(LettuceExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KeyClusterCommandIntegrationTests extends TestSupport {

    private final StatefulRedisClusterConnection<String, String> clusterConnection;

    private final RedisCommands<String, String> redis;

    @Inject
    KeyClusterCommandIntegrationTests(StatefulRedisClusterConnection<String, String> clusterConnection) {
        this.clusterConnection = clusterConnection;
        this.redis = ClusterTestUtil.redisCommandsOverCluster(clusterConnection);
    }

    @BeforeEach
    void setUp() {
        this.redis.flushall();
    }

    @Test
    void del() {

        redis.set(key, "value");
        redis.set("a", "value");
        redis.set("b", "value");

        assertThat(redis.del(key, "a", "b")).isEqualTo(3);
        assertThat(redis.exists(key)).isEqualTo(0);
        assertThat(redis.exists("a")).isEqualTo(0);
        assertThat(redis.exists("b")).isEqualTo(0);
    }

    @Test
    void exists() {

        assertThat(redis.exists(key, "a", "b")).isEqualTo(0);

        redis.set(key, "value");
        redis.set("a", "value");
        redis.set("b", "value");

        assertThat(redis.exists(key, "a", "b")).isEqualTo(3);
    }

    @Test
    @EnabledOnCommand("TOUCH")
    void touch() {

        redis.set(key, "value");
        redis.set("a", "value");
        redis.set("b", "value");

        assertThat(redis.touch(key, "a", "b")).isEqualTo(3);
        assertThat(redis.exists(key, "a", "b")).isEqualTo(3);
    }

    @Test
    @EnabledOnCommand("UNLINK")
    void unlink() {

        redis.set(key, "value");
        redis.set("a", "value");
        redis.set("b", "value");

        assertThat(redis.unlink(key, "a", "b")).isEqualTo(3);
        assertThat(redis.exists(key)).isEqualTo(0);
    }

}
