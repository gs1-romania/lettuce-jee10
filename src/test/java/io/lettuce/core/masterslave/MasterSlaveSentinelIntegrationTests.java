package io.lettuce.core.masterslave;

import static io.lettuce.TestTags.INTEGRATION_TEST;
import static io.lettuce.core.masterslave.MasterSlaveIntegrationTests.slaveCall;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.lettuce.core.*;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.sentinel.SentinelTestSettings;
import io.lettuce.test.LettuceExtension;
import io.lettuce.test.ReflectionTestUtils;
import io.lettuce.test.settings.TestSettings;
import io.netty.channel.group.ChannelGroup;

/**
 * @author Mark Paluch
 */
@Tag(INTEGRATION_TEST)
@ExtendWith(LettuceExtension.class)
class MasterSlaveSentinelIntegrationTests extends TestSupport {

    private final Pattern pattern = Pattern.compile("role:(\\w+)");

    private final RedisClient redisClient;

    @Inject
    MasterSlaveSentinelIntegrationTests(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Test
    void testMasterSlaveSentinelBasic() {

        RedisURI uri = RedisURI.create(
                "redis-sentinel://127.0.0.1:21379,127.0.0.1:22379,127.0.0.1:26379?sentinelMasterId=mymaster&timeout=5s");
        StatefulRedisMasterSlaveConnection<String, String> connection = MasterSlave.connect(redisClient, StringCodec.UTF8, uri);

        connection.setReadFrom(ReadFrom.UPSTREAM);
        String server = slaveCall(connection);
        assertThatServerIs(server, "master");

        connection.close();
    }

    @Test
    void masterSlaveConnectionShouldSetClientName() {

        RedisURI redisURI = RedisURI.Builder.sentinel(TestSettings.host(), SentinelTestSettings.MASTER_ID)
                .withClientName("my-client").build();

        StatefulRedisMasterSlaveConnection<String, String> connection = MasterSlave.connect(redisClient, StringCodec.UTF8,
                redisURI);

        assertThat(connection.sync().clientGetname()).isEqualTo(redisURI.getClientName());
        connection.sync().quit();
        assertThat(connection.sync().clientGetname()).isEqualTo(redisURI.getClientName());

        connection.close();
    }

    @Test
    void testMasterSlaveSentinelWithTwoUnavailableSentinels() {

        RedisURI uri = RedisURI.create(
                "redis-sentinel://127.0.0.1:21379,127.0.0.1:22379,127.0.0.1:26379?sentinelMasterId=mymaster&timeout=5s");
        StatefulRedisMasterSlaveConnection<String, String> connection = MasterSlave.connect(redisClient, StringCodec.UTF8, uri);

        connection.setReadFrom(ReadFrom.UPSTREAM);
        String server = connection.sync().info("replication");
        assertThatServerIs(server, "master");

        connection.close();
    }

    @Test
    void testMasterSlaveSentinelWithUnavailableSentinels() {

        RedisURI uri = RedisURI.create("redis-sentinel://127.0.0.1:21379,127.0.0.1:21379?sentinelMasterId=mymaster&timeout=5s");

        try {
            MasterSlave.connect(redisClient, StringCodec.UTF8, uri);
            fail("Missing RedisConnectionException");
        } catch (RedisConnectionException e) {
            assertThat(e.getCause()).hasRootCauseInstanceOf(IOException.class);
        }
    }

    @Test
    void testMasterSlaveSentinelConnectionCount() {

        ChannelGroup channels = (ChannelGroup) ReflectionTestUtils.getField(redisClient, "channels");
        int count = channels.size();

        StatefulRedisMasterSlaveConnection<String, String> connection = MasterSlave.connect(redisClient, StringCodec.UTF8,
                SentinelTestSettings.SENTINEL_URI);

        connection.sync().ping();
        connection.setReadFrom(ReadFrom.REPLICA);
        slaveCall(connection);

        assertThat(channels.size()).isEqualTo(count + 2 /* connections */ + 1 /* sentinel connections */);

        connection.close();
    }

    @Test
    void testMasterSlaveSentinelClosesSentinelConnections() {

        ChannelGroup channels = (ChannelGroup) ReflectionTestUtils.getField(redisClient, "channels");
        int count = channels.size();

        StatefulRedisMasterSlaveConnection<String, String> connection = MasterSlave.connect(redisClient, StringCodec.UTF8,
                SentinelTestSettings.SENTINEL_URI);

        connection.sync().ping();
        connection.setReadFrom(ReadFrom.REPLICA);
        slaveCall(connection);
        connection.close();

        assertThat(channels.size()).isEqualTo(count);
    }

    private void assertThatServerIs(String server, String expectation) {
        Matcher matcher = pattern.matcher(server);

        assertThat(matcher.find()).isTrue();
        assertThat(matcher.group(1)).isEqualTo(expectation);
    }

}
