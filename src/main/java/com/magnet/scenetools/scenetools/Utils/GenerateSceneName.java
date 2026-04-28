package com.magnet.scenetools.scenetools.Utils;

public class GenerateSceneName {

	public static String generateMovieSceneName(String title, String year, String resolution, String streamingService,
			String ripType, String audioFormat, String videoCodec, String group) {
		return joinSegments(toSceneSegment(title), toSceneSegment(year), toSceneSegment(resolution),
				normalizeStreamingService(streamingService), toSceneSegment(ripType), normalizeAudioFormat(audioFormat),
				normalizeVideoCodec(videoCodec)) + "-" + toSceneSegment(group);
	}

	public static String generateShowSceneName(String title, String season, int episode, String resolution,
			String streamingService, String ripType, String audioFormat, String videoCodec, String group) {
		return joinSegments(toSceneSegment(title), formatSeasonEpisode(season, episode), toSceneSegment(resolution),
				normalizeStreamingService(streamingService), toSceneSegment(ripType), normalizeAudioFormat(audioFormat),
				normalizeVideoCodec(videoCodec)) + "-" + toSceneSegment(group);
	}

	private static String formatSeasonEpisode(String season, int episode) {
		int seasonNumber = Integer.parseInt(season.trim());
		return String.format("S%02dE%02d", seasonNumber, episode);
	}

	private static String normalizeStreamingService(String value) {
		return toSceneSegment(value).toUpperCase();
	}

	private static String normalizeAudioFormat(String value) {
		String normalized = toSceneSegment(value).toUpperCase();

		return switch (normalized) {
		case "AAC.2.0" -> "AAC2.0";
		case "DD+.5.1" -> "DDP5.1";
		case "DTS.5.1" -> "DTS5.1";
		default -> normalized;
		};
	}

	private static String normalizeVideoCodec(String value) {
		String normalized = toSceneSegment(value).toLowerCase();

		return switch (normalized) {
		case "x264" -> "H.264";
		case "x265" -> "H.265";
		default -> toSceneSegment(value).toUpperCase();
		};
	}

	private static String joinSegments(String... values) {
		StringBuilder builder = new StringBuilder();

		for (String value : values) {
			if (value == null || value.isBlank()) {
				continue;
			}

			if (!builder.isEmpty()) {
				builder.append('.');
			}

			builder.append(value);
		}

		return builder.toString();
	}

	private static String toSceneSegment(String value) {
		if (value == null) {
			return "";
		}

		return value.trim().replaceAll("\\s+", ".");
	}
}
