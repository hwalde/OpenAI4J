package de.entwicklertraining.openai4j.audio.service;

import be.tarsos.dsp.*;
import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class to split an audio file into compressed chunks (e.g., MP3) that do not exceed a given file size.
 *
 * <p>
 * Requirements:
 * 1. Detect silence to avoid breaking sentences/words in the middle (where possible).
 * 2. File size must NOT exceed the specified max chunk size (e.g., 24.9 MB).
 * 3. Uses JAVE2 (FFmpeg-based) for encoding, not LameOnJ.
 * 4. Supports custom compression settings (bitrate, etc.).
 * 5. Works with common voice/audio formats: mp3, mp4, mpeg, mpga, m4a, ogg, webm ...
 * 6. Chunks must be "hearably" good for voice content.
 * 7. Chunk boundaries should snap to silence if possible, otherwise force-cut to avoid exceeding size.
 *
 * Usage example:
 * <pre>
 *   AudioChunker chunker = new AudioChunker(24.9, 128, -70.0);
 *   chunker.chunkAudio("input.m4a", "outputChunks");
 * </pre>
 */
public class AudioChunker {

    /**
     * The maximum size (in MB) allowed for each compressed chunk.
     */
    private double maxChunkSizeMB;

    /**
     * The bitrate (in kbps) used for compression (e.g., MP3).
     */
    private int desiredBitrateKbps;

    /**
     * Default silence threshold in dB.
     */
    private double silenceThreshold;

    /**
     * Constructor with user-defined settings.
     *
     * @param maxChunkSizeMB   Maximum compressed chunk size in MB (e.g., 24.9).
     * @param desiredBitrateKbps Bitrate in kbps to be used for compression (e.g., 128).
     * @param silenceThreshold Silence threshold in dB (e.g., -70.0).
     */
    public AudioChunker(double maxChunkSizeMB, int desiredBitrateKbps, double silenceThreshold) {
        this.maxChunkSizeMB = maxChunkSizeMB;
        this.desiredBitrateKbps = desiredBitrateKbps;
        this.silenceThreshold = silenceThreshold;
    }

    /**
     * Allows updating the maximum chunk size in MB at runtime.
     * @param maxChunkSizeMB New max size in MB.
     */
    public void setMaxChunkSizeMB(double maxChunkSizeMB) {
        this.maxChunkSizeMB = maxChunkSizeMB;
    }

    /**
     * Allows updating the desired compression bitrate (kbps) at runtime.
     * @param desiredBitrateKbps New bitrate in kbps.
     */
    public void setDesiredBitrateKbps(int desiredBitrateKbps) {
        this.desiredBitrateKbps = desiredBitrateKbps;
    }

    /**
     * Allows updating the silence threshold at runtime.
     * @param silenceThreshold New threshold in dB.
     */
    public void setSilenceThreshold(double silenceThreshold) {
        this.silenceThreshold = silenceThreshold;
    }

    /**
     * Main method to chunk the given audio file into smaller compressed chunks.
     * Each chunk will not exceed the specified {@link #maxChunkSizeMB} in compressed form.
     * Also tries to snap chunk boundaries to silence to avoid breaking sentences/words,
     * but if none found near the time limit, a forced cut will happen.
     *
     * @param inputAudioPath   Path to the input audio file.
     * @param outputFolderPath Folder where chunks will be saved.
     * @throws IOException                   If file operations fail.
     * @throws UnsupportedAudioFileException If the input format is not supported.
     */
    public void chunkAudio(String inputAudioPath, String outputFolderPath)
            throws IOException, UnsupportedAudioFileException {
        File inputFile = new File(inputAudioPath);
        File outputDir = new File(outputFolderPath);
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("Could not create output folder: " + outputFolderPath);
        }

        // Collect all detected silence positions:
        List<Double> silencePositions = detectSilencePositions(inputFile);

        // Determine total duration of the input
        double totalDurationSec = getAudioDurationSeconds(inputFile);

        // We sort the list of silence positions to be sure they're in ascending order
        Collections.sort(silencePositions);

        // We always add the end of the file as a "silence" marker so we can finalize the last chunk
        if (silencePositions.isEmpty() || silencePositions.get(silencePositions.size() - 1) < totalDurationSec) {
            silencePositions.add(totalDurationSec);
        }

        // We'll chunk from 0.0 to next boundary
        double lastCutTime = 0.0;
        int chunkIndex = 0;

        while (lastCutTime < totalDurationSec) {

            // 1) We guess the chunk duration from user-specified bitrate + max MB limit
            //    approx: size(MB) = (bitrate(kbps)/8) * duration(s) / (1024*1024)
            //    => duration(s) = size(MB)*1024*1024*8 / (bitrate(kbps)*1024) [when 1 kbps ~ 1024 bps]
            double approximateMaxDuration = approximateMaxDurationSeconds();

            // 2) Proposed boundary for the chunk: (forcedCutTime)
            double forcedCutTime = lastCutTime + approximateMaxDuration;
            if (forcedCutTime > totalDurationSec) {
                forcedCutTime = totalDurationSec;
            }

            // 3) Find a silence near 'forcedCutTime' so we can snap to silence
            //    We'll allow some tolerance window, e.g. +/- 3 seconds around forcedCutTime
            //    If none is found in that window, we must forcibly cut.
            double chunkEnd = findSilenceNear(silencePositions, forcedCutTime, lastCutTime, totalDurationSec);

            if (chunkEnd <= lastCutTime) {
                // Edge case: we can't proceed further, break
                break;
            }

            // 4) Actually compress & save the chunk
            saveCompressedChunk(
                    inputFile,
                    lastCutTime,
                    chunkEnd,
                    outputDir,
                    chunkIndex
            );

            chunkIndex++;
            lastCutTime = chunkEnd;

            // If we reached or exceeded total duration, exit loop
            if (lastCutTime >= totalDurationSec - 0.001) {
                break;
            }
        }
    }

    /**
     * Detects silence positions within the file using TarsosDSP's SilenceDetector.
     * @param inputFile Audio file to analyze.
     * @return List of timestamps (in seconds) where silence is detected.
     * @throws IOException If reading fails.
     * @throws UnsupportedAudioFileException If audio format not supported.
     */
    private List<Double> detectSilencePositions(File inputFile) throws IOException, UnsupportedAudioFileException {
        List<Double> silencePositions = new ArrayList<>();
        // Buffer/chunk sizes for Tarsos reading
        int bufferSize = 2048;
        int overlap = 1024;

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(inputFile, bufferSize, overlap);
        SilenceDetector silenceDetector = new SilenceDetector(silenceThreshold, true);

        dispatcher.addAudioProcessor(silenceDetector);
        dispatcher.addAudioProcessor(new AudioProcessor() {
            @Override
            public boolean process(AudioEvent audioEvent) {
                double spl = silenceDetector.currentSPL();
                if (spl < silenceThreshold) {
                    // Mark start of silence
                    silencePositions.add(audioEvent.getTimeStamp());
                }
                return true;
            }
            @Override
            public void processingFinished() {
                // No-op
            }
        });

        dispatcher.run();
        return silencePositions;
    }

    /**
     * Returns the total duration in seconds of the given audio file (via standard JavaSound).
     * @param inputFile Audio file
     * @return Duration in seconds
     * @throws IOException If reading fails
     * @throws UnsupportedAudioFileException If format not supported
     */
    private double getAudioDurationSeconds(File inputFile) throws IOException, UnsupportedAudioFileException {
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(inputFile)) {
            AudioFormat format = ais.getFormat();
            long frameLength = ais.getFrameLength();
            double frameRate = format.getFrameRate();
            return frameLength / frameRate;
        }
    }

    /**
     * Computes the approximate max chunk duration in seconds, based on the user-chosen
     * {@link #maxChunkSizeMB} and {@link #desiredBitrateKbps}.
     *
     * This is an *estimate*, so we still enforce the file-size check after encoding.
     */
    private double approximateMaxDurationSeconds() {
        // size(MB) -> bytes
        double maxBytes = maxChunkSizeMB * 1024 * 1024;
        // bits
        double bitsTotal = maxBytes * 8.0;
        // bitrate in bits/s (1 kbps ~ 1024 bits/s)
        double bitrateInBitsPerSec = desiredBitrateKbps * 1024.0;
        return bitsTotal / bitrateInBitsPerSec;
    }

    /**
     * Attempts to find a silence time near forcedCutTime. If none is found in a small window,
     * we forcibly return forcedCutTime to avoid exceeding the chunk size.
     *
     * @param silencePositions sorted list of silence timestamps
     * @param forcedCutTime the "ideal" cut time
     * @param startOfChunk the chunk's start time
     * @param totalDuration total track length
     * @return time in seconds to cut
     */
    private double findSilenceNear(List<Double> silencePositions, double forcedCutTime,
                                   double startOfChunk, double totalDuration) {
        // If forcedCutTime is near the end, just return it
        if (forcedCutTime >= totalDuration) {
            return totalDuration;
        }

        // For example, let's allow ±3 seconds window
        double tolerance = 3.0;
        double minAllowed = Math.max(startOfChunk + 0.01, forcedCutTime - tolerance);
        double maxAllowed = Math.min(forcedCutTime + tolerance, totalDuration);

        // Among the silencePositions, we look for a silence within [minAllowed, maxAllowed]
        // We'll prefer the one that is just before forcedCutTime (to keep chunk shorter)
        double bestCandidate = forcedCutTime;
        for (Double sp : silencePositions) {
            if (sp >= minAllowed && sp <= maxAllowed) {
                if (sp <= forcedCutTime) {
                    // This is a silence before or exactly at forcedCutTime
                    bestCandidate = sp;
                }
                // If we find a silence slightly after forcedCutTime, we only accept it
                // if we haven't found anything closer. We do not want to exceed forcedCutTime
                // by too much, or else we risk exceeding the size limit. So we don't pick
                // a silence that is after forcedCutTime.
            }
        }

        return bestCandidate;
    }

    /**
     * Saves a compressed chunk (using JAVE2 / FFmpeg) from startTimeSec to endTimeSec.
     * If the resulting file is still too big, we attempt a fallback approach:
     *  - We shorten the chunk in 1-second decrements until it fits or no time left.
     *
     * @param inputFile   The original file
     * @param startTimeSec Start of chunk (seconds)
     * @param endTimeSec   End of chunk (seconds)
     * @param outputDir    Output folder
     * @param chunkIndex   For naming
     * @throws IOException if something fails
     */
    private void saveCompressedChunk(File inputFile,
                                     double startTimeSec,
                                     double endTimeSec,
                                     File outputDir,
                                     int chunkIndex) throws IOException {
        double chunkDurationSec = endTimeSec - startTimeSec;
        if (chunkDurationSec <= 0.0) {
            return; // No valid chunk
        }

        // We'll try compressing once. If the file is bigger than the limit, we shrink duration.
        // Because it's expensive to re-encode multiple times, we do coarse fallback.
        float offset = (float) startTimeSec;
        float duration = (float) chunkDurationSec;

        File outFile = new File(outputDir, String.format("chunk_%03d.mp3", chunkIndex));

        // We'll do a small loop in case the result is unexpectedly large
        int fallbackRetries = 10;
        while (fallbackRetries-- > 0) {
            encodePartial(inputFile, outFile, offset, duration);

            // Check file size
            double fileSizeMB = outFile.length() / (1024.0 * 1024.0);
            if (fileSizeMB <= maxChunkSizeMB + 0.0001) {
                System.out.printf("Created chunk #%d: start=%.2fs, end=%.2fs, size=%.2f MB -> %s%n",
                        chunkIndex, startTimeSec, (startTimeSec + duration), fileSizeMB, outFile.getName());
                return; // success
            }

            // If we are here, it means the chunk is still too large. We'll shrink duration by 1 second.
            // This is a fallback approach to avoid huge loops.
            duration -= 1.0f;
            if (duration <= 1.0f) {
                // If it's smaller than 1 second left, we just keep it
                // to avoid infinite loop. We'll accept the chunk.
                System.out.printf(
                        "Warning: chunk is still big (%.2f MB). " +
                                "Forcing final anyway: start=%.2fs, end=%.2fs -> %s%n",
                        fileSizeMB, startTimeSec, (startTimeSec + duration), outFile.getName());
                break;
            }
        }
    }

    /**
     * Helper method to invoke JAVE2 (FFmpeg) partial encoding with the current settings.
     * @param inputFile The original audio file
     * @param outputFile The chunk file
     * @param offsetSec Start time
     * @param durationSec Duration
     * @throws IOException If encoding fails
     */
    private void encodePartial(File inputFile,
                               File outputFile,
                               float offsetSec,
                               float durationSec) throws IOException {
        // Overwrite if exists
        if (outputFile.exists() && !outputFile.delete()) {
            throw new IOException("Cannot delete old chunk: " + outputFile.getAbsolutePath());
        }

        // Setup audio attributes for voice
        AudioAttributes audio = new AudioAttributes();
        // Use FFmpeg's libmp3lame for output format "mp3"
        audio.setCodec("libmp3lame");
        // setBitRate in bps (so e.g. 128 kbps -> 128000)
        audio.setBitRate(desiredBitrateKbps * 1000);
        // 2 channels for stereo (often fine for voice); or 1 channel for mono if desired
        audio.setChannels(2);
        // sample rate
        audio.setSamplingRate(44100);

        // Build the encoding attributes
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setOutputFormat("mp3");
        attrs.setAudioAttributes(audio);
        // partial encode
        attrs.setOffset(offsetSec);
        attrs.setDuration(durationSec);

        Encoder encoder = new Encoder();
        try {
            encoder.encode(new MultimediaObject(inputFile), outputFile, attrs);
        } catch (EncoderException e) {
            throw new IOException("Encoding failed: " + e.getMessage(), e);
        }
    }

    /**
     * Quick test CLI.
     * Usage: java AudioChunker inputAudioPath outputFolderPath [maxMB=24.9] [bitrateKbps=128] [silenceDb=-70.0]
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java AudioChunker <inputAudioPath> <outputFolderPath> [maxMB=24.9] [bitrateKbps=128] [silenceDb=-70.0]");
            System.exit(1);
        }

        String inputAudio = args[0];
        String outputFolder = args[1];

        double maxMB = (args.length >= 3) ? Double.parseDouble(args[2]) : 24.9;
        int bitrate = (args.length >= 4) ? Integer.parseInt(args[3]) : 128;
        double silenceDb = (args.length >= 5) ? Double.parseDouble(args[4]) : -70.0;

        AudioChunker chunker = new AudioChunker(maxMB, bitrate, silenceDb);

        try {
            chunker.chunkAudio(inputAudio, outputFolder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
