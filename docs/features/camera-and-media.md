# Camera and media

Status: S/T; checkpoint device evidence H only. Reviewed 2026-10-13, app 1.10 at `0a365c1`. Reviewer needed: inspection-device operator.

## Purpose, entry points and restrictions

“Cámara” and “Ver fotos y videos” open full-window capture/gallery within an inspection. General albums are available in patio/transport Entrada/Salida. Damage and unsaved editing albums belong to patio inspections. Read-only/modification inspections can browse available media but cannot capture/delete. Salida damage albums are browse-only. Capture, print or deletion in progress blocks conflicting media actions.

## Behavior

| ID | Current visible behavior | Evidence |
| --- | --- | --- |
| MEDIA-001 | Camera opening starts in “Foto”, uses rear camera, flash off, tap-to-focus and pinch zoom. Opening/canceling before shutter creates no capture file. Camera denial preserves the inspection; “Permitir cámara” or “Abrir ajustes” supports retry. | S/T |
| MEDIA-002 | “Tomar foto” saves a processed photo in the public device gallery and associates it with its album. Interrupted publication can use “Reintentar guardado” on existing bytes or “Descartar captura”. Retry does not intentionally duplicate public publication. | S/T |
| MEDIA-003 | “Video” requires microphone permission and audio; denial leaves photos usable. Recording uses 720p when supported, otherwise 480p; unsupported devices retain photo mode. Maximum is 5 minutes and 200 MB (decimal). “Detener y guardar” finalizes a clip. | S/T |
| MEDIA-004 | Back during recording offers “Guardar”, “Descartar”, “Continuar”. Backgrounding/recreation stops/finalizes; recording never resumes automatically. Invalid/incomplete video is retained privately for retry/discard rather than presented as saved. Discard does not delete an earlier public copy. | S/T |
| MEDIA-005 | “Fotos y videos” shows an ordered mixed grid. Photos support swipe, pinch/pan, double-tap 1×/2× and maximum 4× zoom. Missing items retain their positions with “Foto no disponible” or “Video no disponible”. Back returns to grid then inspection; selection/grid position survives recreation. | S/T |
| MEDIA-006 | Videos have duration/play indicators, start paused and restore selection/playback position paused after recreation. Swiping away, going to grid or background releases playback while retaining position. | S/T |
| MEDIA-007 | “Borrar foto” / “Borrar video” requires confirmation that also removes the public device copy. “Cancelar” preserves it. Storage deletion failure retains the reference; if bytes were deleted but association saving failed, explicit retry completes metadata without deleting bytes twice. Deleting the last item leaves the empty gallery open. | S/T |
| MEDIA-008 | Clearing an inspection/form, removing a damage and removing a submitted draft preserve public media bytes. Videos are local: they do not affect photo counters or upload work; submitted videos remain available in the device gallery. | S/T |

## Persistence, failures and recovery

Public media survives removal of its form association; reopening business records does not reconstruct local associations automatically. Private capture recovery can restore an inspection and unsaved editing album. Unreadable recovery data is retained and can block capture instead of silently resetting state. The durability of the entire inspection across every lifecycle transition remains unverified; local host tests cover narrower media paths.

Current photo-folder import is ACCESS-005. **P:** future draft/queue retirement preserves public bytes and independent media bookkeeping, but need not reconstruct associations found only in abandoned drafts. It must audit recovery journals separately and prevent stale replay; [policy](../modernization/storage-version-policy.md).

## Acceptance examples

| ID | Given | When | Then | Verification |
| --- | --- | --- | --- | --- |
| MEDIA-001 | Open editable camera | Back before shutter | No new file or association | T: CameraCaptureFragmentTest/model |
| MEDIA-004 | Interrupted valid video | Save is retried | One public clip attached to current album | T: VideoCaptureCoordinatorTest |
| MEDIA-007 | Bytes deleted, association write fails | Retry metadata | No second byte deletion | T: GalleryDeletionModelTest |
| MEDIA-008 | Public video on a draft | Form/draft is cleared | Public clip retained | T/S; physical-device gap ACCEPT-04/07 |

## Evidence and questions

Source: [camera UI](../../app/src/main/java/com/juans/inspeccion/ui/media/camera/CameraCaptureFragment.java), [gallery UI](../../app/src/main/java/com/juans/inspeccion/ui/media/gallery/PhotoGalleryFragment.java), [album admission](../../app/src/main/java/com/juans/inspeccion/ui/media/gallery/GalleryAlbums.java), [Spanish camera labels](../../app/src/main/res/values/camera.xml), [gallery labels](../../app/src/main/res/values/gallery.xml). Tests and execution scope: [coverage](../testing/acceptance.md), [baseline](../testing/baseline.md). [Embedded camera history](../embedded-camera.md) contains earlier API 29/34 results; none were rerun here. Physical microphone quality, long recordings, OEM recovery/deletion permissions, reopening and upgrade retention need ACCEPT-04/07.
