package io.lettuce.core.commands;

import static io.lettuce.TestTags.INTEGRATION_TEST;
import static io.lettuce.test.settings.TestSettings.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.util.Arrays;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.lettuce.core.MigrateArgs;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.TestSupport;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.test.CanConnect;
import io.lettuce.test.LettuceExtension;
import io.lettuce.test.Wait;
import io.lettuce.test.settings.TestSettings;

/**
 * @author Will Glozer
 * @author Mark Paluch
 */
@Tag(INTEGRATION_TEST)
@ExtendWith(LettuceExtension.class)
class RunOnlyOnceServerCommandIntegrationTests extends TestSupport {

    private final RedisClient client;

    private final StatefulRedisConnection<String, String> connection;

    private final RedisCommands<String, String> redis;

    @Inject
    RunOnlyOnceServerCommandIntegrationTests(RedisClient client, StatefulRedisConnection<String, String> connection) {

        this.client = client;
        this.connection = connection;
        this.redis = connection.sync();
    }

    /**
     * Executed in order: 1 this test causes a stop of the redis. This means, you cannot repeat the test without restarting your
     * redis.
     */
    @Test
    @Disabled
    void debugSegfault() {

        assumeTrue(CanConnect.to(host(), port(1)));

        final RedisAsyncCommands<String, String> commands = client.connect(RedisURI.Builder.redis(host(), port(1)).build())
                .async();
        try {
            commands.debugSegfault();

            Wait.untilTrue(() -> !commands.getStatefulConnection().isOpen()).waitOrTimeout();
            assertThat(commands.getStatefulConnection().isOpen()).isFalse();
        } finally {
            commands.getStatefulConnection().close();
        }
    }

    /**
     * Executed in order: 2
     */
    @Test
    void migrate() {

        assumeTrue(CanConnect.to(host(), port(2)));

        redis.set(key, value);

        String result = redis.migrate("localhost", TestSettings.port(2), key, 0, 10);
        assertThat(result).isEqualTo("OK");
    }

    /**
     * Executed in order: 3
     */
    @Test
    void migrateCopyReplace() {

        assumeTrue(CanConnect.to(host(), port(2)));

        redis.set(key, value);
        redis.set("key1", value);
        redis.set("key2", value);

        String result = redis.migrate("localhost", TestSettings.port(2), 0, 10, MigrateArgs.Builder.keys(key).copy().replace());
        assertThat(result).isEqualTo("OK");

        result = redis.migrate("localhost", TestSettings.port(2), 0, 10,
                MigrateArgs.Builder.keys(Arrays.asList("key1", "key2")).replace());
        assertThat(result).isEqualTo("OK");
    }

    /**
     * Executed in order: 4 this test causes a stop of the redis. This means, you cannot repeat the test without restarting your
     * redis.
     */
    @Test
    void shutdown() {

        assumeTrue(CanConnect.to(host(), port(2)));

        final RedisAsyncCommands<String, String> commands = client.connect(RedisURI.Builder.redis(host(), port(2)).build())
                .async();
        try {

            commands.shutdown(true);
            commands.shutdown(false);
            Wait.untilTrue(() -> !commands.getStatefulConnection().isOpen()).waitOrTimeout();

            assertThat(commands.getStatefulConnection().isOpen()).isFalse();

        } finally {
            commands.getStatefulConnection().close();
        }
    }

}
