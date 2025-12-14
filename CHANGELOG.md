# Changelog

All notable changes to this project will be documented in this file.

## [1.2.0] - 2025-12-14
### Added
- Streaming support for Chat Completions using Server-Sent Events (SSE)
- New `stream()` method on ChatCompletionRequestBuilder for enabling streaming responses
- `StreamingChatCompletionExample` demonstrating real-time streaming output

### Changed
- Updated api-base dependency to 2.2.0 (adds streaming infrastructure)

## [1.1.2] - 2025-06-18
### Fixed
- Added missing main project to Maven Central repository

## [1.1.1] - 2025-06-18
### Added
- Token counting functionality via `tokenCounter()` method in OpenAIClient
- Preferred model selection support in audio transcription settings

### Changed
- Default transcription response format changed from VERBOSE_JSON to JSON for better compatibility
- Audio service now uses getter methods instead of direct field access for better encapsulation
- Simplified audio transcription example by removing timestamp granularities

### Fixed
- Improved field access patterns in TranscriptionSettings for better code quality

## [1.1.0] - 2025-06-07
### Added
- Optional audio service module providing helpers for chunking audio and merging transcriptions.

## [1.0.0] - 2025-06-04
### Changed
- Initial open source release (general availability) of OpenAI4J.

