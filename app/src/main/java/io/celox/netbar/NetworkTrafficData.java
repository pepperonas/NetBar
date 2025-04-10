/*
 * Copyright (C) 2025 Martin Pfeffer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.celox.netbar;

import java.util.Date;

/**
 * Repräsentiert einen einzelnen Datenpunkt der Netzwerkauslastung.
 */
public class NetworkTrafficData {
    private final long timestamp;
    private final long txBytes;
    private final long rxBytes;

    public NetworkTrafficData(long timestamp, long txBytes, long rxBytes) {
        this.timestamp = timestamp;
        this.txBytes = txBytes;
        this.rxBytes = rxBytes;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Date getDate() {
        return new Date(timestamp);
    }

    public long getTxBytes() {
        return txBytes;
    }

    public long getRxBytes() {
        return rxBytes;
    }
}
