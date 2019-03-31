package com.reactlibrary;

import android.media.midi.MidiReceiver;
import android.util.Log;

import java.io.IOException;

/**
 * Convert stream of arbitrary MIDI bytes into discrete messages.
 *
 * Parses the incoming bytes and then posts individual messages to the receiver
 * specified in the constructor. Short messages of 1-3 bytes will be complete.
 * System Exclusive messages may be posted in pieces.
 *
 * Resolves Running Status and interleaved System Real-Time messages.
 */
public class KeyboardReceiver extends MidiReceiver {
    private static final long NANOS_PER_MILLISECOND = 1000000L;
    private static final long NANOS_PER_SECOND = NANOS_PER_MILLISECOND * 1000L;

    private byte[] mBuffer = new byte[3];
    private int mCount;
    private byte mRunningStatus;
    private int mNeeded;
    private boolean mInSysEx;
    private MsgManager msgM;
    private long mLastTimeStamp = 0;
    private long mStartTime;

    public KeyboardReceiver(MsgManager msgM) {
        mStartTime = System.nanoTime();
        this.msgM = msgM;
    }

    /*
     * @see android.midi.MidiReceiver#onSend(byte[], int, int, long)
     */
    @Override
    public void onSend(byte[] data, int offset, int count, long timestamp)
            throws IOException {
        int sysExStartOffset = (mInSysEx ? offset : -1);

        for (int i = 0; i < count; i++) {
            final byte currentByte = data[offset];
            final int currentInt = currentByte & 0xFF;
            if (currentInt >= 0x80) { // status byte?
                if (currentInt < 0xF0) { // channel message?
                    mRunningStatus = currentByte;
                    mCount = 1;
                    mNeeded = MidiConstants.getBytesPerMessage(currentByte) - 1;
                } else if (currentInt < 0xF8) { // system common?
                    if (currentInt == 0xF0 /* SysEx Start */) {
                        // Log.i(TAG, "SysEx Start");
                        mInSysEx = true;
                        sysExStartOffset = offset;
                    } else if (currentInt == 0xF7 /* SysEx End */) {
                        // Log.i(TAG, "SysEx End");
                        if (mInSysEx) {
//                            mReceiver.send(data, sysExStartOffset,
//                                    offset - sysExStartOffset + 1, timestamp);
                            msgM.emitMsg(
                                    convertToMessage(data, sysExStartOffset,
                                            (offset - sysExStartOffset + 1),
                                            timestamp));
                            mInSysEx = false;
                            sysExStartOffset = -1;
                        }
                    } else {
                        mBuffer[0] = currentByte;
                        mRunningStatus = 0;
                        mCount = 1;
                        mNeeded = MidiConstants.getBytesPerMessage(currentByte) - 1;
                    }
                } else { // real-time?
                    // Single byte message interleaved with other data.
                    if (mInSysEx) {
//                        msgM.emitMsg("RT mInSysEx: " + data + ", " + sysExStartOffset + ", "
//                                + (offset - sysExStartOffset) + ", " + timestamp);
                        msgM.emitMsg(convertToMessage(data, sysExStartOffset, (offset - sysExStartOffset), timestamp));
                        sysExStartOffset = offset + 1;
                    }
//                    mReceiver.send(data, offset, 1, timestamp);
//                    msgM.emitMsg("RT: " + data + ", " + offset + ", "
//                            + (1) + ", " + timestamp);
                }
            } else { // data byte
                if (!mInSysEx) {
                    mBuffer[mCount++] = currentByte;
                    if (--mNeeded == 0) {
                        if (mRunningStatus != 0) {
                            mBuffer[0] = mRunningStatus;
                        }
//                        mReceiver.send(mBuffer, 0, mCount, timestamp);
//                        msgM.emitMsg("Data byte: " + mBuffer + ", " + 0 + ", "
//                                + mCount + ", " + timestamp);
                        Log.i("KBreceiver", "note..");
                        msgM.emitMsg(convertToMessage(mBuffer, 0, mCount, timestamp));

                        mNeeded = MidiConstants.getBytesPerMessage(mBuffer[0]) - 1;
                        mCount = 1;
                    }
                }
            }
            ++offset;
        }

        // send any accumulatedSysEx data
        if (sysExStartOffset >= 0 && sysExStartOffset < offset) {
//            mReceiver.send(data, sysExStartOffset,
//                    offset - sysExStartOffset, timestamp);
        }
    }

    private String convertToMessage(byte[] data, int offset, int count, long timestamp) {
        StringBuilder sb = new StringBuilder();
        if (timestamp == 0) {
            sb.append(String.format("-----0----: "));
        } else {
            long monoTime = timestamp - mStartTime;
            long delayTimeNanos = timestamp - System.nanoTime();
            int delayTimeMillis = (int)(delayTimeNanos / NANOS_PER_MILLISECOND);
            double seconds = (double) monoTime / NANOS_PER_SECOND;
            // Mark timestamps that are out of order.
//            sb.append((timestamp < mLastTimeStamp) ? "*" : " ");
//            mLastTimeStamp = timestamp;
//            sb.append(String.format("%10.3f (%2d): ", seconds, delayTimeMillis));
        }
//        sb.append(MidiPrinter.formatBytes(data, offset, count));
//        sb.append(": ");
        sb.append(MidiPrinter.formatMessage(data, offset, count));
        String text = sb.toString();
        return text;
    }

}

