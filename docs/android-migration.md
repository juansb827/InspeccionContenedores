# Android migration

Compatibility begins at the last released commit, `d9c52d3`. Storage introduced
by unreleased commits is deliberately not imported. Application ID and the
release signing identity must stay the same for an in-place upgrade.

## Released serialization identifiers

The unmodified released model sources were compiled with `javac --release 8`
and inspected with `serialver`, without launching the application:

| Class | serialVersionUID |
| --- | --- |
| Album | 4164294007417506670 |
| Inspeccion | 7140579573600875404 |
| Danio | 779705593577192224 |
| DaniosManager | 6522051388807633138 |

The changed models explicitly retain these identifiers. Tests read serialized
draft and upload-queue fixtures generated using the released model sources.
The existing `listaPendientes` and `fotosPendientes` filenames are retained.
Corrupt stores fail visibly and cannot be replaced by empty stores.

## Storage and recovery

New captures are staged privately, processed without deleting originals, and
published into the shared photo library. Gallery photos survive routine form
clearing, successful uploads, and uninstall. Explicit photo deletion is the
only normal operation that removes a gallery photo.

Legacy import selects the released `Inspecciones` directory through the system
folder picker. Import keeps the source files, tracks copied references, and is
safe to repeat after interruption. Missing photos or revoked folder access do
not discard the draft. Import records must remain intact for retry.

No real camera, printer, production database, or FTP server is used by the JVM
tests. Device-only acceptance checks will be recorded after implementation.
