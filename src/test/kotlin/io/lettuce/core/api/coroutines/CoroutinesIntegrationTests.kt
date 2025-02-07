package io.lettuce.core.api.coroutines

import io.lettuce.TestTags
import io.lettuce.core.RedisClient
import io.lettuce.core.TestSupport
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import io.lettuce.core.cluster.RedisClusterClient
import io.lettuce.core.cluster.api.coroutines
import io.lettuce.core.sentinel.SentinelTestSettings
import io.lettuce.core.sentinel.api.coroutines
import io.lettuce.test.LettuceExtension
import io.lettuce.test.condition.EnabledOnCommand
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import jakarta.inject.Inject

/**
 * Integration tests for Kotlin Coroutine extensions.
 *
 * @author Mark Paluch
 */
@Tag(TestTags.INTEGRATION_TEST)
@ExtendWith(LettuceExtension::class)
class CoroutinesIntegrationTests : TestSupport() {

    @Test
    @Inject
    internal fun shouldInvokeCoroutineCorrectlyForStandalone(connection: StatefulRedisConnection<String, String>) {

        runBlocking {

            val api = connection.coroutines()
            api.set("key", "value")

            assertThat(api.get("key")).isEqualTo("value")
        }
    }

    @Test
    @Inject
    internal fun shouldInvokeCoroutineCorrectlyForCluster(client: RedisClusterClient) {

        val connection = client.connect();
        runBlocking {

            val api = connection.coroutines()
            api.set("key", "value")

            assertThat(api.get("key")).isEqualTo("value")
        }

        connection.close();
    }

    @Test
    @EnabledOnCommand("EXPIRETIME") // Redis 7.0
    @Inject
    internal fun shouldInvokeCoroutineCorrectlyForSentinel(client: RedisClient) {

        val connection = client.connectSentinel(SentinelTestSettings.SENTINEL_URI)

        runBlocking {

            val api = connection.coroutines()

            assertThat(api.master(SentinelTestSettings.MASTER_ID)).isNotEmpty
            assertThat(api.replicas(SentinelTestSettings.MASTER_ID)).isNotEmpty
        }

        connection.close()
    }
}
