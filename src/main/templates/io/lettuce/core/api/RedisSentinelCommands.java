/*
 * Copyright 2017-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.lettuce.core;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

import io.lettuce.core.ClientListArgs;
import io.lettuce.core.KillArgs;
import io.lettuce.core.output.CommandOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.ProtocolKeyword;
import io.lettuce.core.sentinel.api.StatefulRedisSentinelConnection;

/**
 * ${intent} for Redis Sentinel.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 * @author Mark Paluch
 * @since 4.0
 */
public interface RedisSentinelCommands<K, V> {

    /**
     * Return the ip and port number of the master with that name.
     *
     * @param key the key.
     * @return SocketAddress.
     */
    SocketAddress getMasterAddrByName(K key);

    /**
     * Enumerates all the monitored masters and their states.
     *
     * @return Map&lt;K, V&gt;&gt;.
     */
    List<Map<K, V>> masters();

    /**
     * Show the state and info of the specified master.
     *
     * @param key the key.
     * @return Map&lt;K, V&gt;.
     */
    Map<K, V> master(K key);

    /**
     * Provides a list of replicas for the master with the specified name.
     *
     * @param key the key.
     * @return List&lt;Map&lt;K, V&gt;&gt;.
     * @deprecated since 6.2, use #replicas(Object) instead.
     */
    @Deprecated
    List<Map<K, V>> slaves(K key);

    /**
     * This command will reset all the masters with matching name.
     *
     * @param key the key.
     * @return Long.
     */
    Long reset(K key);

    /**
     * Provides a list of replicas for the master with the specified name.
     *
     * @param key the key.
     * @return List&lt;Map&lt;K, V&gt;&gt;.
     * @since 6.2
     */
    List<Map<K, V>> replicas(K key);

    /**
     * Perform a failover.
     *
     * @param key the master id.
     * @return String.
     */
    String failover(K key);

    /**
     * This command tells the Sentinel to start monitoring a new master with the specified name, ip, port, and quorum.
     *
     * @param key the key.
     * @param ip the IP address.
     * @param port the port.
     * @param quorum the quorum count.
     * @return String.
     */
    String monitor(K key, String ip, int port, int quorum);

    /**
     * Multiple option / value pairs can be specified (or none at all).
     *
     * @param key the key.
     * @param option the option.
     * @param value the value.
     * @return String simple-string-reply {@code OK} if {@code SET} was executed correctly.
     */
    String set(K key, String option, V value);

    /**
     * remove the specified master.
     *
     * @param key the key.
     * @return String.
     */
    String remove(K key);

    /**
     * Get the current connection name.
     *
     * @return K bulk-string-reply The connection name, or a null bulk reply if no name is set.
     */
    K clientGetname();

    /**
     * Set the current connection name.
     *
     * @param name the client name.
     * @return simple-string-reply {@code OK} if the connection name was successfully set.
     */
    String clientSetname(K name);

    /**
     * Assign various info attributes to the current connection.
     *
     * @param key the key.
     * @param value the value.
     * @return simple-string-reply {@code OK} if the connection name was successfully set.
     * @since 6.3
     */
    String clientSetinfo(String key, String value);

    /**
     * Kill the connection of a client identified by ip:port.
     *
     * @param addr ip:port.
     * @return String simple-string-reply {@code OK} if the connection exists and has been closed.
     */
    String clientKill(String addr);

    /**
     * Kill connections of clients which are filtered by {@code killArgs}.
     *
     * @param killArgs args for the kill operation.
     * @return Long integer-reply number of killed connections.
     */
    Long clientKill(KillArgs killArgs);

    /**
     * Stop processing commands from clients for some time.
     *
     * @param timeout the timeout value in milliseconds.
     * @return String simple-string-reply The command returns OK or an error if the timeout is invalid.
     */
    String clientPause(long timeout);

    /**
     * Get the list of client connections.
     *
     * @return String bulk-string-reply a unique string, formatted as follows: One client connection per line (separated by LF),
     *         each line is composed of a succession of property=value fields separated by a space character.
     */
    String clientList();

    /**
     * Get the list of client connections which are filtered by {@code clientListArgs}.
     *
     * @return String bulk-string-reply a unique string, formatted as follows: One client connection per line (separated by LF),
     *         each line is composed of a succession of property=value fields separated by a space character.
     * @since 6.3
     */
    String clientList(ClientListArgs clientListArgs);

    /**
     * Get the list of the current client connection.
     *
     * @return String bulk-string-reply a unique string, formatted as a succession of property=value fields separated by a space
     *         character.
     * @since 6.3
     */
    String clientInfo();

    /**
     * Get information and statistics about the server.
     *
     * @return String bulk-string-reply as a collection of text lines.
     */
    String info();

    /**
     * Get information and statistics about the server.
     *
     * @param section the section type: string.
     * @return String bulk-string-reply as a collection of text lines.
     */
    String info(String section);

    /**
     * Ping the server.
     *
     * @return String simple-string-reply.
     */
    String ping();

    /**
     * Dispatch a command to the Redis Server. Please note the command output type must fit to the command response.
     *
     * @param type the command, must not be {@code null}.
     * @param output the command output, must not be {@code null}.
     * @param <T> response type.
     * @return the command response.
     * @since 6.0.2
     */
    <T> T dispatch(ProtocolKeyword type, CommandOutput<K, V, T> output);

    /**
     * Dispatch a command to the Redis Server. Please note the command output type must fit to the command response.
     *
     * @param type the command, must not be {@code null}.
     * @param output the command output, must not be {@code null}.
     * @param args the command arguments, must not be {@code null}.
     * @param <T> response type.
     * @return the command response.
     * @since 6.0.2
     */
    <T> T dispatch(ProtocolKeyword type, CommandOutput<K, V, T> output, CommandArgs<K, V> args);

    /**
     *
     * @return {@code true} if the connection is open (connected and not closed).
     */
    boolean isOpen();

    /**
     *
     * @return the underlying connection.
     */
    StatefulRedisSentinelConnection<K, V> getStatefulConnection();

}
