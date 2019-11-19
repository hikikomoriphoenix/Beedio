/*
 *     LM videodownloader is a browser app for android, made to easily
 *     download videos.
 *     Copyright (C) 2018 Loremar Marabillas
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package marabillas.loremar.lmvideodownloader.browsing_feature;

import java.text.DecimalFormat;
import java.util.Locale;

import wseemann.media.FFmpegMediaMetadataRetriever;

public final class VideoDetailsFetcher {
    private FFmpegMediaMetadataRetriever metadataRetriever = new FFmpegMediaMetadataRetriever();

    public void fetchDetails(final String url, final FetchDetailsListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    metadataRetriever.setDataSource(url);
                    String filename = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_FILENAME);
                    String title = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_TITLE);
                    String vcodec = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_CODEC);
                    String acodec = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_AUDIO_CODEC);
                    String duration = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
                    String filesize = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_FILESIZE);
                    String width = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                    String height = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                    String bitrate = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VARIANT_BITRATE);
                    String framerate = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_FRAMERATE);
                    String encoder = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ENCODER);
                    String encodedBy = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ENCODED_BY);
                    String date = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DATE);
                    String creationTime = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_CREATION_TIME);
                    String artist = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
                    String album = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM);
                    String albumArtist = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM_ARTIST);
                    String track = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_TRACK);
                    String genre = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_GENRE);
                    String composer = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_COMPOSER);
                    String performaer = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_PERFORMER);
                    String copyright = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_COPYRIGHT);
                    String publisher = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_PUBLISHER);
                    String language = metadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_LANGUAGE);

                    StringBuilder sb = new StringBuilder();
                    if (filename != null) sb.append("\nFile: ").append(filename);
                    if (title != null) sb.append("\nTitle: ").append(title);
                    sb.append("\nVideo Codec: ").append((vcodec != null) ? vcodec : "none");
                    sb.append("\nAudio Codec: ").append((acodec != null) ? acodec : "none");
                    String durationFormatted = formatDuration(duration);
                    if (durationFormatted != null)
                        sb.append("\nDuration: ").append(durationFormatted);
                    String filesizeFormatted = formatFilesize(filesize);
                    if (filesizeFormatted != null)
                        sb.append("\nFilesize: ").append(filesizeFormatted);
                    if (width != null) sb.append("\nWidth: ").append(width);
                    if (height != null) sb.append("\nHeight: ").append(height);
                    if (bitrate != null) sb.append("\nBitrate: ").append(bitrate);
                    if (framerate != null)
                        sb.append("\nFramerate: ").append(framerate).append(" fps");
                    if (encoder != null) sb.append("\nEncoder: ").append(encoder);
                    if (encodedBy != null) sb.append("\nEncoded By: ").append(encodedBy);
                    if (date != null) sb.append("\nDate: ").append(date);
                    if (creationTime != null) sb.append("\nCreation Time: ").append(creationTime);
                    if (artist != null) sb.append("\nArtist: ").append(artist);
                    if (album != null) sb.append("\nAlbum: ").append(album);
                    if (albumArtist != null) sb.append("\nAlbum Artist: ").append(albumArtist);
                    if (track != null) sb.append("\nTrack: ").append(track);
                    if (genre != null) sb.append("\nGenre: ").append(genre);
                    if (composer != null) sb.append("\nComposer: ").append(composer);
                    if (performaer != null) sb.append("\nPerformer: ").append(performaer);
                    if (copyright != null) sb.append("\nCopyright: ").append(copyright);
                    if (publisher != null) sb.append("\nPublisher: ").append(publisher);
                    if (language != null) sb.append("\nLanguage: ").append(language);

                    if (sb.length() > 0) {
                        sb.deleteCharAt(0);
                    }

                    listener.onFetched(sb.toString());
                } catch (IllegalArgumentException e) {
                    listener.onUnFetched(e.getMessage());
                }
            }
        }).start();
    }

    private String formatDuration(String duration) {
        try {
            long totalSecs = Long.parseLong(duration) / 1000;
            long mils = totalSecs % 1000;
            long s = totalSecs % 60;
            long m = (totalSecs / 60) % 60;
            long h = (totalSecs / (60 * 60)) % 24;
            return String.format(Locale.US, "%02d:%02d:%02d.%d", h, m, s, mils);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatFilesize(String filesize) {
        try {
            long size = Long.parseLong(filesize);
            if (size <= 0) return "0";
            final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups))
                    + " " + units[digitGroups];
        } catch (NumberFormatException e) {
            return null;
        }
    }

    void close() {
        metadataRetriever.release();
    }

    interface FetchDetailsListener {
        void onUnFetched(String message);

        void onFetched(String details);
    }
}
