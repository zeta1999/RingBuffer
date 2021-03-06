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

package test.object;

import org.ringbuffer.object.RingBuffer;
import test.Profiler;

public class FastManyWritersContentionTest extends RingBufferTest {
    public static class Holder {
        public static final RingBuffer<Event> RING_BUFFER =
                RingBuffer.<Event>withCapacity(FAST_NOT_ONE_TO_ONE_SIZE)
                        .oneReader()
                        .manyWriters()
                        .withoutLocks()
                        .build();
    }

    public static void main(String[] args) {
        new FastManyWritersContentionTest().runBenchmark();
    }

    @Override
    protected long getSum() {
        return MANY_WRITERS_SUM;
    }

    @Override
    protected long testSum() {
        Profiler profiler = createThroughputProfiler(TOTAL_ELEMENTS);
        Writer.startGroupAsync(Holder.RING_BUFFER, profiler);
        return Reader.runAsync(TOTAL_ELEMENTS, Holder.RING_BUFFER, profiler);
    }
}
