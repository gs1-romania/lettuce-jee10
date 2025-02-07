package io.lettuce.core;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.AsyncNodeSelection;
import io.lettuce.test.LettuceExtension;

import static io.lettuce.TestTags.INTEGRATION_TEST;

/**
 * @author Mark Paluch
 */
@Tag(INTEGRATION_TEST)
@ExtendWith(LettuceExtension.class)
class ConnectMethodsIntegrationTests {

    private final RedisClient redisClient;

    private final RedisClusterClient clusterClient;

    @Inject
    ConnectMethodsIntegrationTests(RedisClient redisClient, RedisClusterClient clusterClient) {
        this.redisClient = redisClient;
        this.clusterClient = clusterClient;
    }

    // Standalone
    @Test
    void standaloneSync() {
        redisClient.connect().close();
    }

    @Test
    void standaloneAsync() {
        redisClient.connect().async().getStatefulConnection().close();
    }

    @Test
    void standaloneReactive() {
        redisClient.connect().reactive().getStatefulConnection().close();
    }

    @Test
    void standaloneStateful() {
        redisClient.connect().close();
    }

    // PubSub
    @Test
    void pubsubSync() {
        redisClient.connectPubSub().close();
    }

    @Test
    void pubsubAsync() {
        redisClient.connectPubSub().close();
    }

    @Test
    void pubsubReactive() {
        redisClient.connectPubSub().close();
    }

    @Test
    void pubsubStateful() {
        redisClient.connectPubSub().close();
    }

    // Sentinel
    @Test
    void sentinelSync() {
        redisClient.connectSentinel().sync().getStatefulConnection().close();
    }

    @Test
    void sentinelAsync() {
        redisClient.connectSentinel().async().getStatefulConnection().close();
    }

    @Test
    void sentinelReactive() {
        redisClient.connectSentinel().reactive().getStatefulConnection().close();
    }

    @Test
    void sentinelStateful() {
        redisClient.connectSentinel().close();
    }

    // Cluster
    @Test
    void clusterSync() {
        clusterClient.connect().sync().getStatefulConnection().close();
    }

    @Test
    void clusterAsync() {
        clusterClient.connect().async().getStatefulConnection().close();
    }

    @Test
    void clusterReactive() {
        clusterClient.connect().reactive().getStatefulConnection().close();
    }

    @Test
    void clusterStateful() {
        clusterClient.connect().close();
    }

    @Test
    void clusterPubSubSync() {
        clusterClient.connectPubSub().sync().getStatefulConnection().close();
    }

    @Test
    void clusterPubSubAsync() {
        clusterClient.connectPubSub().async().getStatefulConnection().close();
    }

    @Test
    void clusterPubSubReactive() {
        clusterClient.connectPubSub().reactive().getStatefulConnection().close();
    }

    @Test
    void clusterPubSubStateful() {
        clusterClient.connectPubSub().close();
    }

    // Advanced Cluster
    @Test
    void advancedClusterSync() {
        StatefulRedisClusterConnection<String, String> statefulConnection = clusterClient.connect();
        RedisURI uri = clusterClient.getPartitions().getPartition(0).getUri();
        statefulConnection.getConnection(uri.getHost(), uri.getPort()).sync();
        statefulConnection.close();
    }

    @Test
    void advancedClusterAsync() {
        StatefulRedisClusterConnection<String, String> statefulConnection = clusterClient.connect();
        RedisURI uri = clusterClient.getPartitions().getPartition(0).getUri();
        statefulConnection.getConnection(uri.getHost(), uri.getPort()).sync();
        statefulConnection.close();
    }

    @Test
    void advancedClusterReactive() {
        StatefulRedisClusterConnection<String, String> statefulConnection = clusterClient.connect();
        RedisURI uri = clusterClient.getPartitions().getPartition(0).getUri();
        statefulConnection.getConnection(uri.getHost(), uri.getPort()).reactive();
        statefulConnection.close();
    }

    @Test
    void advancedClusterStateful() {
        clusterClient.connect().close();
    }

    // Cluster node selection
    @Test
    void nodeSelectionClusterAsync() {
        StatefulRedisClusterConnection<String, String> statefulConnection = clusterClient.connect();
        AsyncNodeSelection<String, String> masters = statefulConnection.async().masters();
        statefulConnection.close();
    }

}
