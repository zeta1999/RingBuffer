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

package org.ringbuffer.object;

import jdk.internal.vm.annotation.Contended;
import org.ringbuffer.concurrent.AtomicArray;
import org.ringbuffer.concurrent.AtomicBooleanArray;
import org.ringbuffer.concurrent.AtomicInt;
import org.ringbuffer.lang.Lang;
import org.ringbuffer.wait.BusyWaitStrategy;

@Contended
class FastAtomicWritePrefilledRingBuffer<T> extends FastPrefilledRingBuffer<T> {
    private static final long WRITE_POSITION = Lang.objectFieldOffset(FastAtomicWritePrefilledRingBuffer.class, "writePosition");

    private final int capacityMinusOne;
    private final T[] buffer;
    private final boolean[] positionNotModified;
    private final BusyWaitStrategy readBusyWaitStrategy;

    @Contended
    private int readPosition;
    @Contended
    private int writePosition;

    FastAtomicWritePrefilledRingBuffer(PrefilledRingBufferBuilder<T> builder) {
        capacityMinusOne = builder.getCapacityMinusOne();
        buffer = builder.getBuffer();
        positionNotModified = builder.getPositionNotModified();
        readBusyWaitStrategy = builder.getReadBusyWaitStrategy();
    }

    @Override
    public int getCapacity() {
        return buffer.length;
    }

    @Override
    public int nextKey() {
        return AtomicInt.getAndIncrementVolatile(this, WRITE_POSITION) & capacityMinusOne;
    }

    @Override
    public T next(int key) {
        return AtomicArray.getPlain(buffer, key);
    }

    @Override
    public void put(int key) {
        AtomicBooleanArray.setRelease(positionNotModified, key, false);
    }

    @Override
    public T take() {
        return take(readBusyWaitStrategy);
    }

    @Override
    public T take(BusyWaitStrategy busyWaitStrategy) {
        int readPosition = this.readPosition++ & capacityMinusOne;
        busyWaitStrategy.reset();
        while (AtomicBooleanArray.getAcquire(positionNotModified, readPosition)) {
            busyWaitStrategy.tick();
        }
        AtomicBooleanArray.setPlain(positionNotModified, readPosition, true);
        return AtomicArray.getPlain(buffer, readPosition);
    }
}
