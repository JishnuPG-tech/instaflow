PART 2

Instagram-First Architecture • Media Intelligence • Seal Adaptation Strategy • Download Workflow

13. ARCHITECTURE PHILOSOPHY

InstaFlow is NOT a new application.

InstaFlow is a specialized evolution of the Seal project.

The architecture should preserve every subsystem that already solves the problem well.

The architecture should only replace the parts where Instagram fundamentally differs from YouTube and generic media websites.

Think of the project as:

Seal

↓

Fork

↓

Instagram Specialization

↓

Production Polish

↓

InstaFlow

Never redesign simply because something "looks cleaner."

Redesign only when Instagram requires different behavior.

14. ARCHITECTURE LAYERS

The application consists of six major layers.

Presentation

↓

ViewModel

↓

Repository

↓

Download Engine

↓

youtubedl-android

↓

Instagram

Responsibilities

Presentation

• Compose UI

• Navigation

• State Rendering

• User Interaction

ViewModel

• Business State

• Events

• UI Logic

Repository

• Data Source

• Download Requests

• Cookie Management

• Metadata

Download Layer

• Queue

• WorkManager

• Foreground Service

• Download Scheduling

Extraction

• youtubedl-android

• yt-dlp

Media Source

• Instagram

15. INSTAGRAM-FIRST PRINCIPLE

Seal is video-first.

InstaFlow must become media-first.

Every Instagram URL should be treated as a Media Collection.

Never assume:

"This is a video."

Instead determine:

• Image

• Video

• Carousel

• Mixed Carousel

• Story

• Highlight

• Reel

• Profile Picture

• Future Instagram Media Types

Every media type must receive dedicated handling.

16. INSTAGRAM MEDIA MODEL

Every Instagram URL resolves into:

InstagramPost

├── Post Metadata

├── Caption

├── Author

├── Audio

├── Items[]

Items contain

MediaItem

├── IMAGE

├── VIDEO

├── Thumbnail

├── Resolution

├── Duration

├── File Size

├── Download Options

Never force Instagram media into YouTube-oriented abstractions.

17. SUPPORTED MEDIA TYPES

Mandatory

✓ Image Post

✓ Video Post

✓ Carousel

✓ Mixed Carousel

✓ Reel

✓ Story

✓ Highlight

✓ Profile Picture

✓ Caption

✓ Metadata

Future

• Collections

• Live Archive

• Broadcast Replay

Architecture should already be extensible.

18. SINGLE IMAGE WORKFLOW
Instagram URL

↓

Resolve

↓

Preview Image

↓

Resolution

↓

Download

Display

• Preview

• Resolution

• Dimensions

• File Size

• Download Button

19. SINGLE VIDEO WORKFLOW
Instagram URL

↓

Resolve

↓

Preview

↓

Choose Download Type

↓

Download

Download Types

Video + Audio

Video Only

Audio Only

Only display options that are technically supported.

Never show unavailable actions.

20. CAROUSEL WORKFLOW

This is the most important workflow in InstaFlow.

Instead of Seal's playlist concept:

Implement an Instagram Carousel Picker.

Example

Carousel

1 Image

2 Video

3 Image

4 Video

5 Image

Each card displays

Thumbnail

Media Type

Duration

Resolution

Size

Selection State

Users can

✓ Select Individual Items

✓ Select All

✓ Clear Selection

✓ Download Selected

✓ Download All

Never force the user to download the whole carousel.

21. MIXED MEDIA CAROUSEL

Support

Image

Video

Image

Video

Image

Every media item behaves independently.

Video items show

Duration

Resolution

Audio indicator

Image items show

Dimensions

Resolution

22. AUDIO OPTIONS

Whenever Instagram provides audio:

Offer

Download Video + Audio

Download Video Only

Download Audio Only

Never expose options that cannot actually be fulfilled.

23. STORIES

Support

Image Story

Video Story

Expired Story (when cookies allow)

Story Preview

Story Download

Story Metadata

24. HIGHLIGHTS

Display

Highlight Cover

Highlight Title

Contained Stories

Allow

Select Story

Download Story

Download Entire Highlight

25. PROFILE PICTURE

Support

Highest Resolution Available

Preview

Download

Copy URL

Share

26. CAPTIONS

Every supported media should expose

Caption

Copy Caption

Save Caption

Share Caption

Export Caption

27. METADATA

Support

Username

Display Name

Caption

Upload Date

Audio Title

Location

Hashtags

Media Count

Media Type

Never require downloading media to access metadata.

28. PREVIEW SYSTEM

Every resolved URL should open an Instagram Preview Screen before downloading.

Display

Preview

Swipe

Zoom Images

Play Videos

Metadata

Download Options

This minimizes accidental downloads.

29. DOWNLOAD OPTIONS

Instead of

Download

Offer intelligent actions.

Example

Single Image

Download

Single Video

Video + Audio

Video Only

Audio Only

Carousel

Download Selected

Download All

Story

Download

Highlight

Download Selected

Download All
30. COOKIE SUPPORT

Seal already contains mature cookie handling.

Reuse the implementation.

Do not rewrite.

Tasks

Improve UX

Improve documentation

Improve discoverability

Optionally hide advanced cookie management behind an Advanced Settings section.

31. INSTAGRAM URL VALIDATION

Accept only

instagram.com

www.instagram.com

m.instagram.com

Future additions must be deliberate.

Reject

Every other supported yt-dlp site.

Transform Seal into an Instagram-exclusive application.

32. SEAL ADAPTATION MATRIX

For every subsystem produce this table before implementation.

Subsystem	Seal	InstaFlow	Action
Download Queue	Keep	Same	Reuse
Room	Keep	Same	Reuse
WorkManager	Keep	Same	Reuse
Notification	Keep	Same	Reuse
Playlist	YouTube	Carousel	Replace
Format Picker	Video	Media Picker	Redesign
URL Validation	Generic	Instagram Only	Modify
Cookies	Existing	Existing	Improve UX
Settings	Generic	Instagram	Simplify
Branding	Seal	InstaFlow	Replace

Every work package must include this comparison.

33. MANDATORY INSTAGRAM COMPATIBILITY SPIKE

Before feature development begins, complete a dedicated engineering spike.

Test with real Instagram URLs.

Required validation:

✓ Single Image

✓ Single Video

✓ Carousel

✓ Mixed Carousel

✓ Reel

✓ Story

✓ Highlight

✓ Cookie Protected Content

✓ Large Carousel

✓ Deleted Content

✓ Invalid URLs

✓ Private Content

Document:

Expected Behaviour

Actual Behaviour

Limitations

Required Changes

Regression Risks

Do not continue until every media type has been evaluated.

END OF PART 2

Next, Part 3 will cover the complete engineering workflow, work package system, governance, AI execution rules, quality gates, and Principal Engineer review process.