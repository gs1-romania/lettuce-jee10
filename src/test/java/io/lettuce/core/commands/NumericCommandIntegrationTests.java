/*
 * Copyright 2011-Present, Redis Ltd. and Contributors
 * All rights reserved.
 *
 * Licensed under the MIT License.
 *
 * This file contains contributions from third-party contributors
 * licensed under the Apache License, Version 2.0 (the "License");
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
package io.lettuce.core.commands;

import static io.lettuce.TestTags.INTEGRATION_TEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import io.lettuce.core.TestSupport;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.test.LettuceExtension;

/**
 * @author Will Glozer
 * @author Mark Paluch
 */
@Tag(INTEGRATION_TEST)
@ExtendWith(LettuceExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NumericCommandIntegrationTests extends TestSupport {

    private final RedisCommands<String, String> redis;

    @Inject
    protected NumericCommandIntegrationTests(RedisCommands<String, String> redis) {
        this.redis = redis;
    }

    @BeforeEach
    void setUp() {
        this.redis.flushall();
    }

    @Test
    void decr() {
        assertThat((long) redis.decr(key)).isEqualTo(-1);
        assertThat((long) redis.decr(key)).isEqualTo(-2);
    }

    @Test
    void decrby() {
        assertThat(redis.decrby(key, 3)).isEqualTo(-3);
        assertThat(redis.decrby(key, 3)).isEqualTo(-6);
    }

    @Test
    void incr() {
        assertThat((long) redis.incr(key)).isEqualTo(1);
        assertThat((long) redis.incr(key)).isEqualTo(2);
    }

    @Test
    void incrby() {
        assertThat(redis.incrby(key, 3)).isEqualTo(3);
        assertThat(redis.incrby(key, 3)).isEqualTo(6);
    }

    @Test
    void incrbyfloat() {

        assertThat(redis.incrbyfloat(key, 3.0)).isEqualTo(3.0, offset(0.1));
        assertThat(redis.incrbyfloat(key, 0.2)).isEqualTo(3.2, offset(0.1));
    }

}
