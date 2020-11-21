/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ringbuffer.marshalling;

public interface HeapRingBuffer extends AbstractHeapRingBuffer {
    /**
     * If the ring buffer supports multiple writers and is not lock-free, then external synchronization must be performed:
     *
     * <pre>{@code
     * synchronized (ringBuffer) {
     *     int offset = ringBuffer.next(...);
     *     // Write data
     *     ringBuffer.put(...);
     * }
     * }</pre>
     */
    int next(int size);

    /**
     * If the ring buffer is lock-free, then this method must not be called.
     */
    void advance(int offset);

    static HeapClearingRingBufferBuilder withCapacity(int capacity) {
        return new HeapClearingRingBufferBuilder(capacity);
    }
}
